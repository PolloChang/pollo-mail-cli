---
name: graalvm-tracing
description: >
  Automate the generation of GraalVM reachability metadata (reflect-config.json, resource-config.json) 
  using the native-image-agent. Use this skill when the user wants to "prepare for native image", 
  "collect reflection metadata", "generate reflect-config.json", or "run graalvm agent".
---

# GraalVM Tracing — Native Image Metadata Collector

You are a GraalVM expert helping the user generate dynamic reachability metadata for Java applications (especially Spring Boot 3 AOT or Picocli CLI tools) so they can be compiled into a Native Image without reflection or resource errors.

## Context
GraalVM requires explicit configuration for classes accessed via reflection, JNI, or dynamic proxies. The best way to generate this is by running the application on a standard JVM with the `native-image-agent` attached.

## Step 1: Locate the Build Tool and Application Jar
Determine if the project uses Gradle or Maven. Find how to run the application (e.g., via `bootRun`, `run`, or an executable jar).
If using Spring Boot, the `bootRun` task can often be configured, but it's much safer to build the JAR first and run the JAR directly with the agent.

```bash
./gradlew bootJar
```
Then locate the jar in `build/libs/`.

## Step 2: Attach the Native Image Agent
Construct the java command to run the jar with the tracing agent.
The output directory must be the standard `src/main/resources/META-INF/native-image/` directory so the GraalVM compiler automatically picks it up later.

```bash
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar build/libs/<your-app>.jar [arguments]
```

## Step 3: Instruct the User to Perform Operations
Metadata is only collected for code paths that are actually executed.
1. Run the command using a terminal tool or instruct the user to run it.
2. If it's a CLI tool, tell the user to run *every major command* (e.g., setup, send, fetch) sequentially with the agent attached. The agent supports appending to existing config files via `config-merge-dir`.

**Better yet, automate the tracing:**
Create a short bash script that runs the agent against all core commands of the application, using `config-merge-dir` for subsequent runs.

```bash
#!/bin/bash
JAR="build/libs/myapp.jar"
OUT_DIR="src/main/resources/META-INF/native-image"

echo "Tracing Setup..."
java -agentlib:native-image-agent=config-output-dir=$OUT_DIR -jar $JAR setup

echo "Tracing Fetch..."
java -agentlib:native-image-agent=config-merge-dir=$OUT_DIR -jar $JAR fetch -n 1
```

## Step 4: Verify Metadata
Once the runs complete, verify that files like `reflect-config.json`, `resource-config.json`, and `proxy-config.json` have been generated in `src/main/resources/META-INF/native-image/`.

Inform the user that they can now safely run the native image compilation (e.g., `./gradlew nativeCompile`).
