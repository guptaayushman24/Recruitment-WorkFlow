package com.example.developmentcodeeditor.service;

public interface DockerRunService {
    /**
     * Builds the Dockerfile found in projectDir, starts a Postgres container and the built image on a
     * private network, and returns the host port the app is reachable on.
     */
    int buildAndRun(String runId, String projectDir, Integer questionId) throws Exception;

    /** Removes the containers, network and image created for runId. */
    void teardown(String runId);
}
