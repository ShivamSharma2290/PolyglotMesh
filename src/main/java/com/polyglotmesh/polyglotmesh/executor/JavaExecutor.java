package com.polyglotmesh.polyglotmesh.executor;

import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JavaExecutor implements CodeExecutor {

    @Override
    public CodeExecutionResponse execute(String code, String input) {

        long startTime = System.currentTimeMillis();

        Path tempDirectory = null;

        try {
            tempDirectory = Files.createTempDirectory("polyglotmesh-java");

            Path javaFile = tempDirectory.resolve("Main.java");

            Files.writeString(javaFile, code);

            Process compileProcess = new ProcessBuilder(
                    "javac",
                    javaFile.toString()
            )
                    .redirectErrorStream(true)
                    .start();

            String compileOutput = readOutput(compileProcess);

            int compileExitCode = compileProcess.waitFor();

            if (compileExitCode != 0) {

                return new CodeExecutionResponse(
                        "java",
                        "COMPILE_ERROR",
                        "",
                        compileOutput,
                        System.currentTimeMillis() - startTime
                );
            }

            Process runProcess = new ProcessBuilder(
                    "java",
                    "-cp",
                    tempDirectory.toString(),
                    "Main"
            )
                    .redirectErrorStream(true)
                    .start();

            if (input != null && !input.isBlank()) {
                try (BufferedWriter writer =
                             new BufferedWriter(
                                     new OutputStreamWriter(
                                             runProcess.getOutputStream()))) {

                    writer.write(input);
                    writer.newLine();
                }
            }

            String output = readOutput(runProcess).trim();

            int exitCode = runProcess.waitFor();

            if (exitCode != 0) {

                return new CodeExecutionResponse(
                        "java",
                        "RUNTIME_ERROR",
                        "",
                        output,
                        System.currentTimeMillis() - startTime
                );
            }

            return new CodeExecutionResponse(
                    "java",
                    "SUCCESS",
                    output,
                    "",
                    System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {

            return new CodeExecutionResponse(
                    "java",
                    "ERROR",
                    "",
                    e.getMessage(),
                    System.currentTimeMillis() - startTime
            );

        } finally {

            if (tempDirectory != null) {
                deleteDirectory(tempDirectory);
            }
        }
    }

    private String readOutput(Process process) throws IOException {

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        return output.toString();
    }

    private void deleteDirectory(Path directory) {

        try {

            Files.walk(directory)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {

                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }

                    });

        } catch (IOException ignored) {
        }
    }
}