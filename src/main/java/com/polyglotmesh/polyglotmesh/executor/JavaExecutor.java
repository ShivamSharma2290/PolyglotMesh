package com.polyglotmesh.polyglotmesh.executor;

import com.polyglotmesh.polyglotmesh.config.ExecutionConfig;
import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class JavaExecutor implements CodeExecutor {

    private final ExecutionConfig executionConfig;

    public JavaExecutor(ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }

    @Override
    public CodeExecutionResponse execute(String code, String input) {

        long startTime = System.currentTimeMillis();

        Path tempDirectory = null;

        try {

            tempDirectory = Files.createTempDirectory("polyglotmesh-java");

            Path javaFile = tempDirectory.resolve("Main.java");

            Files.writeString(javaFile, code);

            // Compile
            Process compileProcess = new ProcessBuilder(
                    "javac",
                    javaFile.toString()
            )
                    .redirectErrorStream(true)
                    .start();

            String compileOutput = readStream(
                    compileProcess.getInputStream()
            );

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

            // Run
            Process runProcess = new ProcessBuilder(
                    "java",
                    "-cp",
                    tempDirectory.toString(),
                    "Main"
            )
                    .redirectErrorStream(true)
                    .start();

            // Send input
            if (input != null && !input.isBlank()) {

                try (BufferedWriter writer =
                             new BufferedWriter(
                                     new OutputStreamWriter(
                                             runProcess.getOutputStream(),
                                             StandardCharsets.UTF_8))) {

                    writer.write(input);
                    writer.newLine();
                }
            }

            // Read output in a separate thread
            OutputCollector outputCollector =
                    new OutputCollector(
                            runProcess.getInputStream(),
                            executionConfig.getMaxOutputSize()
                    );

            Thread outputThread = new Thread(outputCollector);
            outputThread.start();

            // Wait for execution timeout
            boolean finished = runProcess.waitFor(
                    executionConfig.getMaxExecutionTime(),
                    TimeUnit.MILLISECONDS
            );

            if (!finished) {

                runProcess.destroyForcibly();

                outputThread.join(1000);

                return new CodeExecutionResponse(
                        "java",
                        "TIMEOUT",
                        "",
                        "Execution timed out after "
                                + executionConfig.getMaxExecutionTime()
                                + " ms",
                        System.currentTimeMillis() - startTime
                );
            }

            outputThread.join(1000);

            String output = outputCollector.getOutput();

            // Check output limit
            if (outputCollector.isLimitExceeded()) {

                return new CodeExecutionResponse(
                        "java",
                        "OUTPUT_LIMIT",
                        "",
                        "Output exceeded maximum limit of "
                                + executionConfig.getMaxOutputSize()
                                + " bytes",
                        System.currentTimeMillis() - startTime
                );
            }

            if (runProcess.exitValue() != 0) {

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
                    output.trim(),
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

    private String readStream(InputStream inputStream)
            throws IOException {

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line)
                        .append(System.lineSeparator());
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

    // Collects process output without allowing unlimited memory usage
    private static class OutputCollector implements Runnable {

        private final InputStream inputStream;
        private final int maxOutputSize;

        private final ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        private boolean limitExceeded = false;

        OutputCollector(
                InputStream inputStream,
                int maxOutputSize) {

            this.inputStream = inputStream;
            this.maxOutputSize = maxOutputSize;
        }

        @Override
        public void run() {

            try {

                byte[] buffer = new byte[1024];

                int totalBytes = 0;

                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    totalBytes += bytesRead;

                    if (totalBytes > maxOutputSize) {

                        limitExceeded = true;

                        inputStream.close();

                        break;
                    }

                    outputStream.write(
                            buffer,
                            0,
                            bytesRead
                    );
                }

            } catch (IOException ignored) {
            }
        }

        String getOutput() {

            return outputStream.toString(
                    StandardCharsets.UTF_8
            );
        }

        boolean isLimitExceeded() {

            return limitExceeded;
        }
    }
}