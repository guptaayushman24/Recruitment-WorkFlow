# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Build/run from `codeeditor/` (the Maven project root, one level below the git repo root):

```bash
./mvnw compile                  # compile
./mvnw clean compile            # clean rebuild (use if you hit stale-class errors)
./mvnw spring-boot:run          # run the app locally (port 8087)
./mvnw test                     # run all tests
./mvnw test -Dtest=CodeeditorApplicationTests   # run a single test class
```

The app requires a local PostgreSQL instance (`localhost:5432/postgres`, user `postgres`, password `root`) and a local Redis instance (`localhost:6379`, no password) — see `src/main/resources/application.yml`. Schema/seed SQL lives in `src/main/resources/db/` and is not auto-applied by Spring; run it manually against Postgres.

`spring-boot-devtools` is on the classpath for auto-restart on recompile, but it only triggers on an actual classfile change — an IDE with "build automatically" enabled, or a manual `./mvnw compile`, is required to see it fire.

## Architecture

Spring Boot 4.1.1 / Java 21 backend for a coding-interview platform. Two distinct feature areas live side by side in the same package tree (`com.example.codeeditor`):

**1. Code execution (`controller/RunCode.java` → `serviceimpl/ValidateRunCodeServiceImpl.java`)**
Handles `POST /runcode`: validates the submitted language against `constant/Constant.java`'s allowed-extensions set, writes the source to `generated-code/<extension>/<name>.<extension>` (`sourceCodeFilePath`), shells out to compile it (`javac` for `.java`, `g++` for `.cpp`), then shells out again to run the compiled artifact — `java -cp <folder> <ClassName>` for Java, the built binary directly for C++ — piping `programmingInput` to the process's stdin and capturing stdout as the response. Only `.java` and `.cpp` currently have compile+run logic wired up, even though `Constant` lists ~20 language extensions as "allowed" (`.py`, `.js`, etc. pass validation but hit the "execution not supported" branch).
Known constraint: each run reuses a fixed filename per language, so concurrent requests for the same language will clobber each other's source/binary on disk — there's no per-request isolation (e.g. UUID subfolder) yet.

**2. Coding round flow (`controller/RunCode.java` → `serviceimpl/StartCodingRoundServiceImpl.java` → `repository/StartCodingInterviewRepository.java`)**
Handles `GET /startCodingRound`: picks a random question ID (via `maxiMumQuestions()` bound), fetches the question and its sample test cases from Postgres via `JdbcTemplate` (queries centralized in `sql/SQL.java`, schema `recruitment_workflow`), and caches each test case's input/output pair into Redis (`RedisTemplate<Object,Object>` bean named `sampleTestCaseOutput`, configured in `config/RedisConfig.java` with JSON serialization) before returning the question.
`config/ClientConfig.java` also declares a `CodingInterviewService` HTTP client bean (Spring 7 `HttpServiceProxyFactory`/`RestClient`) pointed at an external `vercel.app` base URL — this appears to be for a separate/future integration and isn't wired into the current controller flow.

### Data model

- `coding_round_questions` (`id`, `question_title`, `question_description`)
- `coding_round_questions_test_case` (`id`, `crq_id` FK → `coding_round_questions.id`, `input` JSONB, `output` JSONB) — JSONB is used specifically because a test case's input/output can be an int, string, 1D array, or 2D array depending on the question.

### DTO naming convention

`requestdto/` and `responsedto/` packages separate inbound request bodies from outbound response bodies (e.g. `RunCodeRequestDTO` vs `RunCodeResponseDTO`), even when a type like `SampleCodingTestCaseDTO` is used purely internally between the repository and Redis layer.
