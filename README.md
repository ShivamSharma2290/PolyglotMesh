###### \# PolyglotMesh

###### \### GraalVM Multi-Language Runtime Sandbox

###### 

###### \## Project Overview

###### 

###### PolyglotMesh is a Java-based web application designed to provide a unified interface for executing code written in multiple programming languages. It uses Spring Boot and GraalVM-related technologies to support multi-language code execution.

###### 

###### \## Objectives

###### 

###### \- Execute code in Java, Python, and JavaScript.

###### \- Provide a web-based code editor.

###### \- Accept user input and display program output.

###### \- Display execution status, errors, and execution time.

###### \- Integrate a frontend with a Spring Boot backend.

###### 

###### \## Technologies Used

###### 

###### \- Java

###### \- Spring Boot

###### \- GraalVM Polyglot

###### \- Python

###### \- JavaScript

###### \- HTML5

###### \- CSS3

###### \- Maven Wrapper

###### 

###### \## Features

###### 

###### \- Programming language selection.

###### \- Code editor with sample programs.

###### \- Input area for program input.

###### \- Code execution through a REST API.

###### \- Output and error display.

###### \- Execution status and execution-time display.

###### \- Clear editor and output controls.

###### \- Theme toggle.

###### 

###### \## System Architecture

###### 

###### 1\. \*\*Frontend:\*\* HTML, CSS, and JavaScript provide the user interface.

###### 2\. \*\*Controllers:\*\* Spring Boot REST endpoints receive execution requests.

###### 3\. \*\*Service Layer:\*\* Routes requests to the appropriate executor.

###### 4\. \*\*Execution Layer:\*\* Handles Java, Python, and JavaScript code execution.

###### 5\. \*\*Response:\*\* Returns execution status, output, errors, and execution time.

###### 

###### \## API Endpoints

###### 

###### | Method | Endpoint | Description |

###### |---|---|---|

###### | POST | `/api/code/execute` | Executes submitted code |

###### | POST | `/api/execute` | Alternative execution endpoint |

###### | GET | `/api/health` | Checks backend health |

###### 

###### \## How to Run

###### 

###### \### Prerequisites

###### 

###### \- Compatible Java Development Kit.

###### \- Required GraalVM runtime and language dependencies.

###### \- Git.

###### \- IntelliJ IDEA or another Java IDE.

###### 

###### \### Steps

###### 

###### 1\. Clone or download the repository.

###### 2\. Open the project in your IDE.

###### 3\. Open Git Bash or a terminal in the project root.

###### 4\. Run the application:

###### 

###### ```bash

###### ./mvnw spring-boot:run

###### ```

###### 

###### On Windows, you can also run:

###### 

###### ```bash

###### ./mvnw.cmd spring-boot:run

###### ```

###### 

###### 5\. Open `http://localhost:8080/` in your browser.

###### 6\. Select a language, enter code, and click \*\*Run Code\*\*.

###### 

###### \## Project Structure

###### 

###### ```text

###### PolyglotMesh/

###### ├── src/

###### │   └── main/

###### │       ├── java/

###### │       │   └── com/polyglotmesh/polyglotmesh/

###### │       │       ├── config/

###### │       │       ├── controller/

###### │       │       ├── dto/

###### │       │       ├── executor/

###### │       │       └── service/

###### │       └── resources/

###### │           └── static/

###### │               ├── index.html

###### │               ├── script.js

###### │               └── style.css

###### ├── mvnw

###### ├── mvnw.cmd

###### ├── pom.xml

###### └── README.md

###### ```

###### 

###### \## Expected Outcome

###### 

###### PolyglotMesh aims to provide a single web interface for submitting code in supported languages and viewing the execution results returned by the backend.

###### 

###### \## Future Enhancements

###### 

###### \- Improve sandbox isolation and resource-limit enforcement.

###### \- Add additional programming languages.

###### \- Improve error reporting and diagnostics.

###### \- Add execution history and more code examples.

###### \- Strengthen security and input validation.

###### 

###### \## Project Information

###### 

###### \- \*\*Project Name:\*\* PolyglotMesh

###### \- \*\*Project Type:\*\* Web Application

###### \- \*\*Domain:\*\* Multi-Language Code Execution

###### \- \*\*Backend:\*\* Java and Spring Boot

###### \- \*\*Frontend:\*\* HTML, CSS, and JavaScript

###### 

