```javascript
/* =========================================================
   POLYGLOTMESH FRONTEND
   Backend API Integration
========================================================= */

const API_URL = "http://localhost:8080/api/code/execute";


// =========================================================
// ELEMENTS
// =========================================================

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


// =========================================================
// DEFAULT CODE
// =========================================================

const defaultCode = {

    python:
`print("Hello PolyglotMesh")`,

    java:
`public class Main {

    public static void main(String[] args) {

        System.out.println("Hello PolyglotMesh");

    }
}`,

    javascript:
`console.log("Hello PolyglotMesh");`
};


// =========================================================
// LOAD DEFAULT CODE
// =========================================================

function loadDefaultCode() {

    const language = languageSelect.value;

    codeEditor.value = defaultCode[language];

    updateLanguageBadge();

    updateLineNumbers();
}


// =========================================================
// LANGUAGE CHANGE
// =========================================================

languageSelect.addEventListener(
    "change",
    function () {

        loadDefaultCode();

        clearOutput();

    }
);


// =========================================================
// UPDATE LANGUAGE BADGE
// =========================================================

function updateLanguageBadge() {

    const language =
        languageSelect.value;

    languageBadge.textContent =
        language.toUpperCase();
}


// =========================================================
// LINE NUMBERS
// =========================================================

function updateLineNumbers() {

    const lines =
        codeEditor.value.split("\n").length;

    let numbers = "";

    for (let i = 1; i <= lines; i++) {

        numbers += i + "\n";
    }

    lineNumbers.textContent =
        numbers;
}


codeEditor.addEventListener(
    "input",
    updateLineNumbers
);


// =========================================================
// TAB SUPPORT
// =========================================================

codeEditor.addEventListener(
    "keydown",
    function (event) {

        if (event.key === "Tab") {

            event.preventDefault();

            const start =
                codeEditor.selectionStart;

            const end =
                codeEditor.selectionEnd;

            codeEditor.value =
                codeEditor.value.substring(
                    0,
                    start
                ) +
                "    " +
                codeEditor.value.substring(
                    end
                );

            codeEditor.selectionStart =
                codeEditor.selectionEnd =
                    start + 4;

            updateLineNumbers();
        }
    }
);


// =========================================================
// RUN CODE
// =========================================================

runButton.addEventListener(
    "click",
    executeCode
);


async function executeCode() {

    const language =
        languageSelect.value;

    const code =
        codeEditor.value;

    const input =
        inputEditor.value;


    // Validate code

    if (!code.trim()) {

        showError(
            "Please enter some code before running."
        );

        return;
    }


    // Loading state

    setRunningState();


    try {

        const response =
            await fetch(
                API_URL,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        language: language,
                        code: code,
                        input: input
                    })
                }
            );


        if (!response.ok) {

            throw new Error(
                "Backend returned HTTP "
                + response.status
            );
        }


        const result =
            await response.json();


        displayResult(result);

        // Enable button again
        enableRunButton();


    } catch (error) {

        showError(
            "Unable to connect to the PolyglotMesh backend.\n\n"
            + error.message
        );

        // Enable button again
        enableRunButton();
    }
}


// =========================================================
// DISPLAY RESULT
// =========================================================

function displayResult(result) {

    const status =
        result.status;

    const output =
        result.output || "";

    const error =
        result.error || "";


    // Execution time

    executionTime.textContent =
        result.executionTime + " ms";


    // SUCCESS

    if (status === "SUCCESS") {

        setStatus(
            "SUCCESS",
            "success"
        );

        outputConsole.textContent =
            output || "Program executed successfully.";

        return;
    }


    // TIMEOUT

    if (status === "TIMEOUT") {

        setStatus(
            "TIMEOUT",
            "timeout"
        );

        outputConsole.textContent =
            error;

        return;
    }


    // OUTPUT LIMIT

    if (status === "OUTPUT_LIMIT") {

        setStatus(
            "OUTPUT LIMIT",
            "timeout"
        );

        outputConsole.textContent =
            error;

        return;
    }


    // COMPILE ERROR

    if (status === "COMPILE_ERROR") {

        setStatus(
            "COMPILE ERROR",
            "error"
        );

        outputConsole.textContent =
            error;

        return;
    }


    // RUNTIME ERROR

    if (status === "RUNTIME_ERROR") {

        setStatus(
            "RUNTIME ERROR",
            "error"
        );

        outputConsole.textContent =
            error;

        return;
    }


    // GENERAL ERROR

    setStatus(
        "ERROR",
        "error"
    );

    outputConsole.textContent =
        error || output;
}


// =========================================================
// RUNNING STATE
// =========================================================

function setRunningState() {

    runButton.disabled = true;

    runButton.innerHTML =
        `<i class="bi bi-arrow-repeat spin"></i>
         Running...`;

    setStatus(
        "RUNNING",
        "idle"
    );

    executionTime.textContent =
        "--";

    outputConsole.textContent =
        "Executing your code...";
}


// =========================================================
// SET STATUS
// =========================================================

function setStatus(
    text,
    type
) {

    executionStatus.className =
        "execution-status " + type;

    executionStatus.innerHTML =
        `<span class="status-dot"></span>
         ${text}`;
}


// =========================================================
// ERROR DISPLAY
// =========================================================

function showError(message) {

    setStatus(
        "ERROR",
        "error"
    );

    executionTime.textContent =
        "--";

    outputConsole.textContent =
        message;
}


// =========================================================
// CLEAR
// =========================================================

clearButton.addEventListener(
    "click",
    clearAll
);


function clearAll() {

    codeEditor.value = "";

    inputEditor.value = "";

    clearOutput();

    updateLineNumbers();
}


function clearOutput() {

    outputConsole.innerHTML = `
        <div class="empty-output">

            <i class="bi bi-terminal"></i>

            <h6>No output yet</h6>

            <p>
                Run your code to see the result here.
            </p>

        </div>
    `;

    executionTime.textContent =
        "--";

    setStatus(
        "Ready",
        "idle"
    );
}


// =========================================================
// ENABLE RUN BUTTON
// =========================================================

function enableRunButton() {

    runButton.disabled = false;

    runButton.innerHTML =
        `<i class="bi bi-play-fill"></i>
         Run Code`;
}


// =========================================================
// KEYBOARD SHORTCUT
// Ctrl + Enter → Run
// =========================================================

document.addEventListener(
    "keydown",
    function (event) {

        if (
            event.ctrlKey &&
            event.key === "Enter"
        ) {

            event.preventDefault();

            if (!runButton.disabled) {

                executeCode();

            }
        }
    }
);


// =========================================================
// THEME BUTTON
// =========================================================

themeButton.addEventListener(
    "click",
    function () {

        document.body.classList.toggle(
            "light-mode"
        );

        const icon =
            themeButton.querySelector("i");

        if (
            document.body.classList.contains(
                "light-mode"
            )
        ) {

            icon.className =
                "bi bi-sun-fill";

        } else {

            icon.className =
                "bi bi-moon-stars-fill";
        }
    }
);


// =========================================================
// ADD SPIN ANIMATION
// =========================================================

const style =
    document.createElement("style");

style.textContent = `
    .spin {
        animation:
            spinAnimation
            1s linear infinite;
    }

    @keyframes spinAnimation {

        from {
            transform: rotate(0deg);
        }

        to {
            transform: rotate(360deg);
        }
    }

    button:disabled {
        opacity: 0.7;
        cursor: not-allowed;
    }
`;

document.head.appendChild(style);


// =========================================================
// INITIALIZE
// =========================================================

loadDefaultCode();
```
