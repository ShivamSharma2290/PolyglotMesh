package com.polyglotmesh.polyglotmesh.executor;

import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@Component
public class JavaScriptExecutor implements CodeExecutor {

    @Override
    public CodeExecutionResponse execute(String code, String input) {

        long startTime = System.currentTimeMillis();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        PrintStream originalOut = System.out;

        try {

            System.setOut(printStream);

            try (Context context = Context.newBuilder("js")
                    .allowAllAccess(false)
                    .build()) {

                context.eval("js", code);
            }

            long executionTime =
                    System.currentTimeMillis() - startTime;

            String output =
                    outputStream.toString().trim();

            return new CodeExecutionResponse(
                    "javascript",
                    "SUCCESS",
                    output,
                    null,
                    executionTime
            );

        } catch (Exception e) {

            long executionTime =
                    System.currentTimeMillis() - startTime;

            return new CodeExecutionResponse(
                    "javascript",
                    "ERROR",
                    outputStream.toString().trim(),
                    e.getMessage(),
                    executionTime
            );

        } finally {

            System.setOut(originalOut);
            printStream.close();
        }
    }
}