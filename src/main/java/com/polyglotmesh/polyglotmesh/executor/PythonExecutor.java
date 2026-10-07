package com.polyglotmesh.polyglotmesh.executor;

import com.polyglotmesh.polyglotmesh.config.ExecutionConfig;
import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
                new LimitedOutputStream(
                        executionConfig.getMaxOutputSize()
                );

        PrintStream printStream = new PrintStream(
                outputStream,
                true,
                StandardCharsets.UTF_8
        );

        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;

        try {

            System.setOut(printStream);

            if (input == null) {
                input = "";
            }

            System.setIn(
                    new ByteArrayInputStream(
                            input.getBytes(StandardCharsets.UTF_8)
                    )
            );

            try (Context context = Context.newBuilder("python")
                    .allowAllAccess(false)
                    .allowIO(IOAccess.NONE)
                    .build()) {

                context.eval("python", code);
            }

            if (outputStream.isLimitExceeded()) {

                return new CodeExecutionResponse(
                        "python",
                        "OUTPUT_LIMIT",
                        "",
                        "Output exceeded maximum limit of "
                                + executionConfig.getMaxOutputSize()
                                + " bytes",
                        System.currentTimeMillis() - startTime
                );
            }

            return new CodeExecutionResponse(
                    "python",
                    "SUCCESS",
                    outputStream.getOutput().trim(),
                    null,
                    System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {

            if (outputStream.isLimitExceeded()) {

                return new CodeExecutionResponse(
                        "python",
                        "OUTPUT_LIMIT",
                        "",
                        "Output exceeded maximum limit of "
                                + executionConfig.getMaxOutputSize()
                                + " bytes",
                        System.currentTimeMillis() - startTime
                );
            }

            return new CodeExecutionResponse(
                    "python",
                    "ERROR",
                    outputStream.getOutput().trim(),
                    e.getMessage(),
                    System.currentTimeMillis() - startTime
            );

        } finally {

            System.setOut(originalOut);
            System.setIn(originalIn);

            printStream.close();
        }
    }

    private static class LimitedOutputStream
            extends ByteArrayOutputStream {

        private final int maxSize;
        private boolean limitExceeded = false;

        public LimitedOutputStream(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public synchronized void write(
                int value) {

            if (count >= maxSize) {
                limitExceeded = true;
                return;
            }

            super.write(value);
        }

        @Override
        public synchronized void write(
                byte[] bytes,
                int offset,
                int length) {

            if (count >= maxSize) {
                limitExceeded = true;
                return;
            }

            int remaining = maxSize - count;

            if (length > remaining) {

                super.write(
                        bytes,
                        offset,
                        remaining
                );

                limitExceeded = true;

            } else {

                super.write(
                        bytes,
                        offset,
                        length
                );
            }
        }

        public boolean isLimitExceeded() {
            return limitExceeded;
        }

        public String getOutput() {

            return toString(
                    StandardCharsets.UTF_8
            );
        }
    }
}