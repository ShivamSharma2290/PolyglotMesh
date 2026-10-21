
/* =========================================================
   POLYGLOTMESH FRONTEND
   Backend API Integration
========================================================= */

const API_URL = "http://localhost:8080/api/code/execute";

document.addEventListener("DOMContentLoaded", () => {
    const languageSelect = document.getElementById("languageSelect");
    const languageBadge = document.getElementById("languageBadge");
    const codeEditor = document.getElementById("codeEditor");
    const inputEditor = document.getElementById("inputEditor");
    const runButton = document.getElementById("runButton");
    const clearButton = document.getElementById("clearButton");
    const outputConsole = document.getElementById("outputConsole");
    const executionStatus = document.getElementById("executionStatus");
    const executionTime = document.getElementById("executionTime");
    const lineNumbers = document.getElementById("lineNumbers");
    const themeButton = document.getElementById("themeButton");

    const requiredElements = {
        languageSelect,
        codeEditor,
        runButton,
        clearButton,
        outputConsole
    };

    const missingElements = Object.entries(requiredElements)
        .filter(([, element]) => !element)
        .map(([name]) => name);

    if (missingElements.length > 0) {
        console.error(
            "PolyglotMesh: Missing HTML elements:",
            missingElements.join(", ")
        );
        return;
    }

    const defaultCode = {
        python: 'print("Hello, World!")',
        javascript: 'console.log("Hello, World!");',
        java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`
    };

    function getLanguage() {
        return languageSelect.value.trim().toLowerCase();
    }

    function updateLanguageBadge() {
        if (languageBadge) {
            languageBadge.textContent = getLanguage();
        }
    }

    function updateLineNumbers() {
        if (!lineNumbers) return;

        const totalLines = codeEditor.value.split("\n").length;

        lineNumbers.textContent = Array.from(
            { length: totalLines },
            (_, index) => index + 1
        ).join("\n");
    }

    function setStatus(message, type = "idle") {
        if (!executionStatus) return;

        executionStatus.textContent = message;
        executionStatus.className = `execution-status ${type}`;
    }

    function clearOutput() {
        outputConsole.replaceChildren();

        const placeholder = document.createElement("div");
        placeholder.className = "empty-output";
        placeholder.textContent = "Output will appear here...";

        outputConsole.appendChild(placeholder);

        if (executionTime) {
            executionTime.textContent = "0 ms";
        }

        setStatus("Ready", "idle");
    }

    function showOutput(value, className = "") {
        outputConsole.replaceChildren();

        const output = document.createElement("pre");
        output.className = className;
        output.textContent =
            value === null || value === undefined
                ? ""
                : String(value);

        outputConsole.appendChild(output);
    }

    function updateEditorForLanguage() {
        const language = getLanguage();

        if (!Object.prototype.hasOwnProperty.call(defaultCode, language)) {
            showOutput(
                `Unsupported language: ${language}`,
                "error-output"
            );
            setStatus("Unsupported language", "error");
            return;
        }

        codeEditor.value = defaultCode[language];

        updateLanguageBadge();
        updateLineNumbers();
        clearOutput();
    }

    async function executeCode() {
        const code = codeEditor.value;
        const language = getLanguage();
        const input = inputEditor ? inputEditor.value : "";

        if (!code.trim()) {
            showOutput(
                "Please enter some code before running.",
                "error-output"
            );
            setStatus("No code", "error");
            return;
        }

        if (!Object.prototype.hasOwnProperty.call(defaultCode, language)) {
            showOutput(
                `Unsupported language: ${language}`,
                "error-output"
            );
            setStatus("Unsupported language", "error");
            return;
        }

        const previousButtonContent = runButton.innerHTML;
        const startTime = performance.now();

        runButton.disabled = true;
        runButton.textContent = "Running...";

        setStatus("Executing...", "running");
        showOutput("Executing your code...");

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({
                    language: language,
                    code: code,
                    input: input
                })
            });

            const elapsedTime = Math.round(
                performance.now() - startTime
            );

            const responseText = await response.text();

            let result;

            try {
                result = JSON.parse(responseText);
            } catch {
                throw new Error(
                    `Invalid server response (HTTP ${response.status}). ` +
                    "Check the Spring Boot controller and server logs."
                );
            }

            if (!response.ok) {
                throw new Error(
                    result.message ||
                    result.error ||
                    `HTTP ${response.status}: Request failed.`
                );
            }

            // These fields match CodeExecutionResponse.
            const status = String(result.status || "").toUpperCase();
            const output = result.output ?? "";
            const error = result.error ?? "";

            if (executionTime) {
                executionTime.textContent =
                    `${result.executionTime ?? elapsedTime} ms`;
            }

            if (status === "SUCCESS") {
                showOutput(
                    output || "Program executed successfully. No output.",
                    "success-output"
                );
                setStatus("Success", "success");
            } else {
                // Handles TIMEOUT, OUTPUT_LIMIT and other failure statuses.
                const errorMessage =
                    error ||
                    output ||
                    `Execution failed. Status: ${status || "UNKNOWN"}`;

                showOutput(errorMessage, "error-output");
                setStatus(
                    status === "TIMEOUT"
                        ? "Timed out"
                        : status === "OUTPUT_LIMIT"
                            ? "Output limit exceeded"
                            : "Failed",
                    "error"
                );
            }
        } catch (error) {
            console.error("PolyglotMesh execution error:", error);

            const message = error instanceof Error
                ? error.message
                : String(error);

            showOutput(
                `Unable to execute code.\n\n${message}\n\n` +
                "Verify that Spring Boot is running on port 8080, " +
                "the API endpoint is correct, and CORS is configured " +
                "if the frontend uses a different origin.",
                "error-output"
            );

            setStatus("Error", "error");

            if (executionTime) {
                executionTime.textContent =
                    `${Math.round(performance.now() - startTime)} ms`;
            }
        } finally {
            runButton.disabled = false;
            runButton.innerHTML = previousButtonContent;
        }
    }

    runButton.addEventListener("click", executeCode);

    clearButton.addEventListener("click", () => {
        codeEditor.value = "";
        updateLineNumbers();
        clearOutput();
        codeEditor.focus();
    });

    languageSelect.addEventListener(
        "change",
        updateEditorForLanguage
    );

    codeEditor.addEventListener("input", updateLineNumbers);

    codeEditor.addEventListener("scroll", () => {
        if (lineNumbers) {
            lineNumbers.scrollTop = codeEditor.scrollTop;
        }
    });

    codeEditor.addEventListener("keydown", event => {
        if (event.key === "Tab") {
            event.preventDefault();

            const start = codeEditor.selectionStart;
            const end = codeEditor.selectionEnd;

            codeEditor.setRangeText("    ", start, end, "end");
            updateLineNumbers();
        }

        if (
                    event.key === "Enter" &&
                    (event.ctrlKey || event.metaKey)
                ) {
                    event.preventDefault();
                    executeCode();
                }
            });


/* =========================
   DARK / LIGHT THEME
========================= */


function applyTheme(theme) {
    const selectedTheme = theme === "light" ? "light" : "dark";

    document.documentElement.setAttribute("data-theme", selectedTheme);
    document.body.setAttribute("data-theme", selectedTheme);

    if (themeButton) {
        const isDark = selectedTheme === "dark";
        const icon = themeButton.querySelector("i");

        if (icon) {
            icon.className = isDark
                ? "bi bi-sun-fill"
                : "bi bi-moon-stars-fill";
        }

        themeButton.setAttribute(
            "aria-label",
            isDark ? "Switch to light theme" : "Switch to dark theme"
        );
    }
}

let savedTheme = "dark";

try {
    savedTheme = localStorage.getItem("polyglotmesh-theme") || "dark";
} catch (error) {
    console.warn("Could not load saved theme.");
}

applyTheme(savedTheme);

if (themeButton) {
    themeButton.addEventListener("click", () => {
        const current =
            document.documentElement.getAttribute("data-theme") || "dark";

        const next = current === "dark" ? "light" : "dark";

        applyTheme(next);

        try {
            localStorage.setItem("polyglotmesh-theme", next);
        } catch (error) {
            console.warn("Could not save theme.");
        }
    });
}



    updateLanguageBadge();
    updateLineNumbers();
    clearOutput();

    console.log("PolyglotMesh frontend initialized.");
});
