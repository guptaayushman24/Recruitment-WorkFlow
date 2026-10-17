package com.example.developmentcodeeditor.serviceimpl;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.developmentcodeeditor.repository.DevelopmentQuestionRepository;
import com.example.developmentcodeeditor.service.DockerRunService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DockerRunServiceImpl implements DockerRunService {

    // Create the client
    HttpClient httpClient = HttpClient.newHttpClient();

    private static final String DEFAULT_DOCKERFILE = """
            FROM maven:3.9-eclipse-temurin-21 AS build
            WORKDIR /app
            COPY pom.xml .
            COPY src ./src
            RUN mvn -DskipTests package

            FROM eclipse-temurin:21-jre
            WORKDIR /app
            COPY --from=build /app/target/*.jar app.jar
            EXPOSE %d
            ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=%<d --spring.datasource.username=$DB_USERNAME --spring.datasource.password=$DB_PASSWORD"]
            """;

    private final DockerClient dockerClient;
    private final DevelopmentQuestionRepository developmentQuestionRepository;

    @Value("${docker.app-port}")
    private int appPort;
    @Value("${docker.db-name}")
    private String dbName;
    @Value("${docker.db-user}")
    private String dbUser;
    @Value("${docker.db-password}")
    private String dbPassword;

    @Override
    public int buildAndRun(String runId, String projectDir,Integer questionId) throws Exception {
        String network = "judge-" + runId;
        String dbName_ = "db-" + runId;
        String appName = "app-" + runId;
        String image = "submission-" + runId;

        Path dir = Paths.get(projectDir);
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("Project folder not found: " + dir);
        }
        ensureDockerfile(dir);

        // Re-running the same project replaces the previous run
        teardown(runId);

        // 1. isolated network
        dockerClient.createNetworkCmd().withName(network).exec();

        // 2. Postgres on that network (docker-java does not auto-pull like `docker run`)
        pullIfMissing("postgres:16");
        CreateContainerResponse db = dockerClient.createContainerCmd("postgres:16")
                .withName(dbName_)
                .withEnv("POSTGRES_DB=" + dbName, "POSTGRES_USER=" + dbUser, "POSTGRES_PASSWORD=" + dbPassword)
                .withHostConfig(HostConfig.newHostConfig().withNetworkMode(network))
                .exec();
        dockerClient.startContainerCmd(db.getId()).exec();

        // 3. wait until Postgres accepts connections
        waitForPostgres(db.getId());

        // 4. build the image from the saved project folder
        java.util.Deque<String> buildLog = new java.util.concurrent.ConcurrentLinkedDeque<>();
        String imageId;
        try {
            imageId = dockerClient.buildImageCmd(new File(projectDir))
                    .withTags(java.util.Set.of(image))
                    .exec(new BuildImageResultCallback() {
                        @Override
                        public void onNext(com.github.dockerjava.api.model.BuildResponseItem item) {
                            if (item.getStream() != null && !item.getStream().isBlank()) {
                                buildLog.add(item.getStream().stripTrailing());
                                if (buildLog.size() > 25) {
                                    buildLog.poll();
                                }
                            }
                            super.onNext(item);
                        }
                    })
                    .awaitImageId();
        } catch (RuntimeException e) {
            throw new IllegalStateException(e.getMessage() + "\n" + String.join("\n", buildLog), e);
        }
        log.info("Built image {} ({})", image, imageId);

        // 5. run the app, reaching the db by container name; publish app port on a random host port
        String jdbcUrl = "jdbc:postgresql://" + dbName_ + ":5432/" + dbName;
        ExposedPort exposed = ExposedPort.tcp(appPort);
        CreateContainerResponse app = dockerClient.createContainerCmd(image)
                .withName(appName)
                .withEnv("DB_URL=" + jdbcUrl, "DB_USERNAME=" + dbUser, "DB_PASSWORD=" + dbPassword,
                        // Spring Boot reads these directly, so projects work without any datasource properties
                        "SPRING_DATASOURCE_URL=" + jdbcUrl,
                        "SPRING_DATASOURCE_USERNAME=" + dbUser,
                        "SPRING_DATASOURCE_PASSWORD=" + dbPassword)
                .withExposedPorts(exposed)
                .withHostConfig(HostConfig.newHostConfig()
                        .withNetworkMode(network)
                        .withPortBindings(new PortBinding(Ports.Binding.empty(), exposed)))
                .exec();
        dockerClient.startContainerCmd(app.getId()).exec();
        
              
        Ports.Binding[] bindings = dockerClient.inspectContainerCmd(app.getId()).exec()
                .getNetworkSettings().getPorts().getBindings().get(exposed);
        int hostPort = Integer.parseInt(bindings[0].getHostPortSpec());
        log.info("App {} reachable on http://localhost:{}", appName, hostPort);
        failIfExited(app.getId(), appName);

        // Call the endpoint the question asks for and store what the user's API returned
        callAndStoreResponse(hostPort, questionId);

        return hostPort;
    }

    /** Calls the question's expected endpoint on the running app and saves the response in development_user_response. */
    private void callAndStoreResponse(int hostPort, Integer questionId) throws InterruptedException {
        if (questionId == null) {
            log.info("No questionId sent, skipping the API call");
            return;
        }
        String endpoint = developmentQuestionRepository.fetchExpectedEndpoint(questionId);
        if (endpoint == null) {
            throw new IllegalStateException("No development_question found with id " + questionId);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + hostPort + endpoint))
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(5))
                .GET()
                .build();

        // The app may still be starting, so retry the connection for a while
        String lastError = null;
        for (int i = 0; i < 20; i++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("GET {} -> {}", endpoint, response.statusCode());
                developmentQuestionRepository.saveUserResponse(questionId, response.body(), response.statusCode(), endpoint);
                return;
            } catch (java.io.IOException e) {
                lastError = e.getMessage();
                Thread.sleep(1000);
            }
        }
        log.warn("GET {} failed: {}", endpoint, lastError);
        developmentQuestionRepository.saveUserResponse(questionId, "No response: " + lastError, null, endpoint);
    }

    /**
     * Writes a default Maven/Spring Boot Dockerfile when the project does not ship
     * its own.
     */
    private void ensureDockerfile(Path dir) throws java.io.IOException {
        Path dockerfile = dir.resolve("Dockerfile");
        if (Files.exists(dockerfile)) {
            return;
        }
        if (!Files.exists(dir.resolve("pom.xml"))) {
            throw new IllegalStateException("No Dockerfile or pom.xml found in " + dir);
        }
        Files.writeString(dockerfile, DEFAULT_DOCKERFILE.formatted(appPort));
        log.info("No Dockerfile in {}, wrote default one", dir);
    }

    /**
     * Gives the app a few seconds to start; if the container has already exited,
     * report its logs.
     */
    private void failIfExited(String containerId, String name) throws Exception {
        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            if (!Boolean.TRUE.equals(dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning())) {
                StringBuilder logs = new StringBuilder();
                dockerClient.logContainerCmd(containerId).withStdOut(true).withStdErr(true).withTail(40)
                        .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                            @Override
                            public void onNext(com.github.dockerjava.api.model.Frame f) {
                                logs.append(new String(f.getPayload()));
                            }
                        }).awaitCompletion(10, TimeUnit.SECONDS);
                String tail = logs.toString().lines()
                        .filter(l -> l.contains("Caused by") || l.contains("ERROR") || l.contains("APPLICATION FAILED"))
                        .limit(8).collect(java.util.stream.Collectors.joining("\n"));
                throw new IllegalStateException("Container " + name + " exited right after start:\n"
                        + (tail.isBlank() ? logs : tail));
            }
        }
    }

    private void pullIfMissing(String imageName) throws InterruptedException {
        try {
            dockerClient.inspectImageCmd(imageName).exec();
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            log.info("Pulling {}", imageName);
            dockerClient.pullImageCmd(imageName)
                    .exec(new com.github.dockerjava.api.command.PullImageResultCallback())
                    .awaitCompletion(10, TimeUnit.MINUTES);
        }
    }

    private void waitForPostgres(String containerId) throws Exception {
        for (int i = 0; i < 30; i++) {
            String execId = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true).withAttachStderr(true)
                    .withCmd("pg_isready", "-U", dbUser, "-d", dbName)
                    .exec().getId();
            dockerClient.execStartCmd(execId)
                    .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>())
                    .awaitCompletion(10, TimeUnit.SECONDS);
            Long exitCode = dockerClient.inspectExecCmd(execId).exec().getExitCodeLong();
            if (exitCode != null && exitCode == 0) {
                return;
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("Postgres did not become ready in time");
    }

    @Override
    public void teardown(String runId) {
        removeQuietly(
                () -> dockerClient.removeContainerCmd("app-" + runId).withForce(true).withRemoveVolumes(true).exec());
        removeQuietly(
                () -> dockerClient.removeContainerCmd("db-" + runId).withForce(true).withRemoveVolumes(true).exec());
        removeQuietly(() -> dockerClient.removeNetworkCmd("judge-" + runId).exec());
        removeQuietly(() -> dockerClient.removeImageCmd("submission-" + runId).withForce(true).exec());
    }

    private void removeQuietly(Runnable r) {
        try {
            r.run();
        } catch (Exception ignored) {
            // resource did not exist
        }
    }
}
