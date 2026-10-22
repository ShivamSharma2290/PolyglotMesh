
/* =========================================================
   POLYGLOTMESH - FRONTEND JAVASCRIPT
========================================================= */

const API_URL = "http://localhost:8080/api/code/execute";

document.addEventListener("DOMContentLoaded", () => {
    const $ = (id) => document.getElementById(id);

    const languageSelect = $("languageSelect");
    const languageBadge = $("languageBadge");
    const codeEditor = $("codeEditor");
    const inputEditor = $("inputEditor");
    const runButton = $("runButton");
    const clearButton = $("clearButton");
    const outputConsole = $("outputConsole");
    const executionStatus = $("executionStatus");
    const executionTime = $("executionTime");
    const lineNumbers = $("lineNumbers");

    const themeToggle = $("themeToggle");
    const themeIcon = $("themeIcon");
    const themeLabel = $("themeLabel");

    const defaultCode = {
        python: 'print("Hello, World!")',
        javascript: 'console.log("Hello, World!");',
        java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`
    };

    // Stop early if required HTML elements are missing.
    const required = {
        languageSelect,
        codeEditor,
        inputEditor,
        runButton,
        clearButton,
        outputConsole,
        executionStatus,
        executionTime,
        lineNumbers,
        themeToggle
    };

    const missing = Object.entries(required)
        .filter(([, element]) => !element)
        .map(([name]) => name);

    if (missing.length) {
        console.error("PolyglotMesh: Missing HTML elements:", missing);
        return;
    }

    /* =========================
       DARK / LIGHT THEME
    ========================= */

    function applyTheme(theme, save = true) {
        const selectedTheme = theme === "light" ? "light" : "dark";

        // CSS variables change automatically through this attribute.
        document.documentElement.setAttribute("data-theme", selectedTheme);

        const isDark = selectedTheme === "dark";

        if (themeIcon) {
            themeIcon.className = isDark
                ? "bi bi-sun-fill"
                : "bi bi-moon-stars-fill";
        }

        if (themeLabel) {
            themeLabel.textContent = isDark ? "Light Mode" : "Dark Mode";
        }

        themeToggle.setAttribute(
            "aria-label",
            isDark ? "Switch to light theme" : "Switch to dark theme"
        );

        // Keep the browser's toolbar color in sync.
        const themeColor = document.querySelector('meta[name="theme-color"]');
        if (themeColor) {
            themeColor.setAttribute(
                "content",
                isDark ? "#080b14" : "#f3f5fb"
            );
        }

        if (save) {
            try {
                localStorage.setItem("polyglotmesh-theme", selectedTheme);
            } catch (error) {
                console.warn("Theme preference could not be saved.", error);
            }
        }
    }

    let savedTheme = "dark";

    try {
        savedTheme = localStorage.getItem("polyglotmesh-theme") || "dark";
    } catch (error) {
        console.warn("Saved theme could not be read.", error);
    }

    applyTheme(savedTheme, false);

    themeToggle.addEventListener("click", () => {
        const currentTheme =
            document.documentElement.getAttribute("data-theme") || "dark";

        applyTheme(currentTheme === "dark" ? "light" : "dark");
    });

    /* =========================
       LANGUAGE AND EDITOR
    ========================= */

    function getLanguage() {
        return languageSelect.value.trim().toLowerCase();
    }

    function updateLanguageBadge() {
        if (languageBadge) {
            languageBadge.textContent = getLanguage().toUpperCase();
        }
    }

    function updateLineNumbers() {
        const totalLines = Math.max(1, codeEditor.value.split("\n").length);

        lineNumbers.textContent = Array.from(
            { length: totalLines },
            (_, index) => index + 1
        ).join("\n");

        lineNumbers.scrollTop = codeEditor.scrollTop;
    }

    function setStatus(message, type = "idle") {
        executionStatus.className = `execution-status ${type}`;
        executionStatus.replaceChildren();

        const dot = document.createElement("span");
        dot.className = "status-dot";
        executionStatus.appendChild(dot);
        executionStatus.appendChild(document.createTextNode(` ${message}`));
    }

    function showOutput(value, className = "") {
        outputConsole.replaceChildren();

        const output = document.createElement("pre");
        output.className = className;
        output.textContent = value == null ? "" : String(value);

        outputConsole.appendChild(output);
    }

    function clearOutput() {
        outputConsole.replaceChildren();

        const empty = document.createElement("div");
        empty.className = "empty-output";

        const icon = document.createElement("i");
        icon.className = "bi bi-terminal";

        const heading = document.createElement("h6");
        heading.textContent = "No output yet";

        const description = document.createElement("p");
        description.textContent = "Run your code to see the result here.";

        empty.append(icon, heading, description);
        outputConsole.appendChild(empty);

        executionTime.textContent = "--";
        setStatus("Ready", "idle");
    }

    function updateEditorForLanguage() {
        const language = getLanguage();

        codeEditor.value = defaultCode[language] || "";
        updateLanguageBadge();
        updateLineNumbers();
        clearOutput();
    }

    languageSelect.addEventListener("change", updateEditorForLanguage);
    codeEditor.addEventListener("input", updateLineNumbers);

    codeEditor.addEventListener("scroll", () => {
        lineNumbers.scrollTop = codeEditor.scrollTop;
    });

    // Allow indentation with Tab and execution with Ctrl+Enter.
    codeEditor.addEventListener("keydown", (event) => {
        if (event.key === "Tab") {
            event.preventDefault();

            const start = codeEditor.selectionStart;
            const end = codeEditor.selectionEnd;

            codeEditor.setRangeText("    ", start, end, "end");
            updateLineNumbers();
        }

        if (event.key === "Enter" && (event.ctrlKey || event.metaKey)) {
            event.preventDefault();
            executeCode();
        }
    });

    /* =========================
       RUN CODE
    ========================= */

    async function executeCode() {
        const code = codeEditor.value;
        const language = getLanguage();
        const input = inputEditor.value;

        if (!code.trim()) {
            showOutput("Please enter some code before running.", "error-output");
            setStatus("No code", "error");
            return;
        }

        if (!Object.prototype.hasOwnProperty.call(defaultCode, language)) {
            showOutput(`Unsupported language: ${language}`, "error-output");
            setStatus("Unsupported language", "error");
            return;
        }

        const originalButtonContent = runButton.innerHTML;
        const startTime = performance.now();

        runButton.disabled = true;
        runButton.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Running...';

        setStatus("Executing...", "running");
        showOutput("Executing your code...");

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({ language, code, input })
            });

            const elapsed = Math.round(performance.now() - startTime);
            const responseText = await response.text();

            let result;

            try {
                result = JSON.parse(responseText);
            } catch {
                throw new Error(
                    `Server returned an invalid response (HTTP ${response.status}).`
                );
            }

            if (!response.ok) {
                throw new Error(
                    result.message ||
                    result.error ||
                    `HTTP ${response.status}: Request failed.`
                );
            }

            const status = String(result.status || "").toUpperCase();
            const output = result.output ?? "";
            const error = result.error ?? "";

            executionTime.textContent =
                `${result.executionTime ?? elapsed} ms`;

            if (status === "SUCCESS") {
                showOutput(
                    output || "Program executed successfully. No output.",
                    "success-output"
                );
                setStatus("Success", "success");
            } else {
                showOutput(
                    error || output || `Execution failed. Status: ${status || "UNKNOWN"}`,
                    "error-output"
                );
                setStatus(
                    status === "TIMEOUT" ? "Timed out" :
                    status === "OUTPUT_LIMIT" ? "Output limit exceeded" :
                    "Failed",
                    "error"
                );
            }
        } catch (error) {
            console.error("PolyglotMesh execution error:", error);

            showOutput(
                "Unable to execute code.\n\n" +
                (error.message || String(error)) +
                "\n\nCheck that Spring Boot is running on port 8080, " +
                "the API endpoint is correct, and CORS is configured if needed.",
                "error-output"
            );

            setStatus("Error", "error");
            executionTime.textContent =
                `${Math.round(performance.now() - startTime)} ms`;
        } finally {
            runButton.disabled = false;
            runButton.innerHTML = originalButtonContent;
        }
    }

    runButton.addEventListener("click", executeCode);

    /* =========================
       CLEAR
    ========================= */

    clearButton.addEventListener("click", () => {
        codeEditor.value = "";
        inputEditor.value = "";
        updateLineNumbers();
        clearOutput();
        codeEditor.focus();
    });

    // Initial editor setup.
    updateLanguageBadge();
    updateLineNumbers();
    clearOutput();

    console.log("PolyglotMesh frontend initialized.");
});
