
package com.polyglotmesh.polyglotmesh.executor;

import com.polyglotmesh.polyglotmesh.config.ExecutionConfig;
import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class PythonExecutor implements CodeExecutor {

    private final ExecutionConfig executionConfig;

    public PythonExecutor(ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }

    @Override
    public CodeExecutionResponse execute(String code, String input) {

        long startTime = System.currentTimeMillis();

        LimitedOutputStream outputStream =
                new LimitedOutputStream(executionConfig.getMaxOutputSize());

        InputStream inputStream = new ByteArrayInputStream(
                (input == null ? "" : input).getBytes(StandardCharsets.UTF_8)
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            try (Context context = Context.newBuilder("python")
                    .in(inputStream)
                    .out(outputStream)
                    .err(outputStream)
                    .allowAllAccess(false)
                    .allowIO(IOAccess.NONE)
                    .build()) {

                context.eval("python", code);
            }
        });

        try {
            future.get(
                    executionConfig.getMaxExecutionTime(),
                    TimeUnit.MILLISECONDS
            );

            long executionTime = System.currentTimeMillis() - startTime;
            String output = outputStream.getOutput().trim();

            if (outputStream.isLimitExceeded()) {
                return new CodeExecutionResponse(
                        "python", "OUTPUT_LIMIT", output,
                        "Maximum output size exceeded", executionTime
                );
            }

            return new CodeExecutionResponse(
                    "python", "SUCCESS", output, null, executionTime
            );

        } catch (TimeoutException e) {
            future.cancel(true);

            return new CodeExecutionResponse(
                    "python", "TIMEOUT", outputStream.getOutput().trim(),
                    "Execution timed out after "
                            + executionConfig.getMaxExecutionTime() + " ms",
                    System.currentTimeMillis() - startTime
            );

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            String error = cause instanceof PolyglotException
                    ? cause.getMessage()
                    : cause.getMessage();

            return new CodeExecutionResponse(
                    "python", "ERROR", outputStream.getOutput().trim(),
                    error == null ? "Python execution failed" : error,
                    System.currentTimeMillis() - startTime
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return new CodeExecutionResponse(
                    "python", "ERROR", outputStream.getOutput().trim(),
                    "Execution interrupted",
                    System.currentTimeMillis() - startTime
            );

        } finally {
            future.cancel(true);
            executor.shutdownNow();

            try {
                inputStream.close();
            } catch (Exception ignored) {
                // Ignore input stream cleanup errors.
            }
        }
    }

    private static class LimitedOutputStream extends OutputStream {

        private final int maxSize;
        private final ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        private boolean limitExceeded = false;

        LimitedOutputStream(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public synchronized void write(int b) {
            if (buffer.size() < maxSize) {
                buffer.write(b);
            } else {
                limitExceeded = true;
            }
        }

        @Override
        public synchronized void write(byte[] bytes, int offset, int length) {
            int remaining = maxSize - buffer.size();

            if (remaining <= 0) {
                limitExceeded = true;
                return;
            }

            int bytesToWrite = Math.min(length, remaining);
            buffer.write(bytes, offset, bytesToWrite);

            if (bytesToWrite < length) {
                limitExceeded = true;
            }
        }

        public synchronized String getOutput() {
            return buffer.toString(StandardCharsets.UTF_8);
        }

        public synchronized boolean isLimitExceeded() {
            return limitExceeded;
        }
    }
}
