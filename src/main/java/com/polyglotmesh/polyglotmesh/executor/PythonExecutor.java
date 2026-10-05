package com.polyglotmesh.polyglotmesh.executor;

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

    @Override
    public CodeExecutionResponse execute(String code, String input) {

        long startTime = System.currentTimeMillis();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

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

            long executionTime = System.currentTimeMillis() - startTime;

            String output = outputStream
                    .toString(StandardCharsets.UTF_8)
                    .trim();

            return new CodeExecutionResponse(
                    "python",
                    "SUCCESS",
                    output,
                    null,
                    executionTime
            );

        } catch (Exception e) {

            long executionTime = System.currentTimeMillis() - startTime;

            return new CodeExecutionResponse(
                    "python",
                    "ERROR",
                    outputStream.toString(StandardCharsets.UTF_8).trim(),
                    e.getMessage(),
                    executionTime
            );

        } finally {

            System.setOut(originalOut);
            System.setIn(originalIn);

            printStream.close();
        }
    }
}