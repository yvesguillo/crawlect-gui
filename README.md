# Crawlect-GUI

**Java Swing GUI for [*Crawlect*](https://github.com/yvesguillo/crawlect)**

![Crawlect-GUI](images/crawlect-gui.avif)

## Why Crawlect-GUI?

Enhance Crawlect’s accessibility by providing a GUI that simplifies trying, checking, and editing analysis runs.

> *Crawlect-GUI* is a study project initiated by [*Yves Guillo*](https://yvesguillo.ch) & [*Alexandre Jenzer*](https://github.com/Alex141298), supervised by *Aïcha Rizzotti-Kaddouri* during [*He-Arc*](https://www.he-arc.ch/en/)'s *CAS-IDD*'s *GUI* module (2025).

## Use cases

- **Learning & Exploration**  
  *A student wants to understand the structure and logic of an unfamiliar codebase.*  
  Launch Crawlect-GUI, select the project folder, and generate a Markdown summary to explore files, syntax, and logic flow.

- **Legacy Code Analysis**  
  *A developer inherits a large, outdated codebase with no clear documentation.*  
  Launch Crawlect-GUI to generate an overview of the structure, identify obsolete files, and spot inconsistencies.

- **Onboarding New Team Members**  
  *A new hire needs to get familiar with the company’s software projects.*  
  Explore multiple codebases, browse generated Markdown overviews, and customize outputs to match learning style, reducing ramp-up time.

- **Troubleshooting & Debugging**  
  *A team member needs to check if ignored files or wrong extensions are causing issues.*  
  Quickly rerun the analysis with different `.crawlectignore` or flag parameters, tweaking depth or file types to isolate the problem, all from a single window.

- **Comparative Analysis**  
  *A lead developer wants to compare two branches of a repo or two folders for changes in structure.*  
  Open two tabs in Crawlect-GUI, run separate analyses, and export the outputs for side-by-side comparison, making it easy to detect file additions or refactoring.  
  Main features:

- **Refactoring Preparation**  
  *A tech lead wants to clean up a monolithic module before a big refactoring.*  
  Run Crawlect-GUI with different scopes and parameters to isolate the most complex or deeply nested parts, using the generated overview to plan restructuring.

- **Documentation Assistance**  
  *A tech writer needs to update the architecture documentation of a project.*  
  Launch Crawlect-GUI to generate a Markdown snapshot of the codebase and integrate it directly into writing tools.

## Overview

### Technologies

- **Frontend**: Java Swing with [`FlatLaf`](https://github.com/JFormDesigner/FlatLaf)
- **Backend**: Python Crawlect CLI app via [`Runtime`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/Runtime.html#getRuntime()) class.
- **Installer**: via [`jpackage`](https://docs.oracle.com/en/java/javase/24/docs/specs/man/jpackage.html).

## Features

Crawlect-GUI provides a user-friendly graphical interface to run and customize [Crawlect](https://github.com/yvesguillo/crawlect) analyses with ease. It is designed to simplify exploration, documentation, comparison, and onboarding tasks across codebases.

### Project Selection & Execution

| issue | Description | Value | Complexity |
|:-:|:--|:-:|:-:|
| #1 | **Select folder to crawl** using a file picker (`JFileChooser`) | 5 | 2 |
| #2 | **Configure output file name and destination** | 4 | 2 |
| #3 | **Trigger Crawlect execution** as a subprocess | 5 | 3 |
| #12 | **Set CLI parameters** `--llm-model`, `--api-key`… | 3 | 2 |
| #4 | **Toggle flag parameters** such as `--depth`, `--gitignore`… | 4 | 3 |
| #13 | **Dropdowns or inputs** for custom filters or scoped depth | 3 | 2 |
| #5 | **Parse available parameters from ~`crawlect --help`~ `crawlect -clischem`** to dynamically populate the GUI (e.g. [cli-schema.json](cli-schema.json)) | 4 | 4 |


### Interface & Display

| issue | Description | Value | Complexity |
|:-:|:--|:-:|:-:|
| #14 | **Multi-tab interface** to run and compare multiple analyses | 5 | 4 |
| #15 | **Independent per-tab inputs and outputs** | 5 | 3 |
| #11 | **Toggle embedded console** to show/hide command output | 3 | 2 |
| #6 | **Real-time stdout/stderr display** in an embedded console | 4 | 4 |
| #16 | **Color-coded logs and warning/error highlighting** | 4 | 4 |
| #17 | **Preview Markdown output** in-console or open in external viewer | 4 | 3 |
| #18 | **Minimal/advanced toggle** to simplify UI when needed | 3 | 2 |
| #7 | **FlatLaf integration** for a modern, consistent UI | 3 | 1 |
| #19 | **Light/dark theme toggle** for user comfort | 3 | 1 |


### Configuration & Presets

| issue | Description | Value | Complexity |
|:-:|:--|:-:|:-:|
| #20 | **Save/load analysis presets** for quick reuse | 4 | 3 |
| #29 | **Sync settings across tabs** (optional) | 2 | 3 |
| #21 | **Persistent settings storage** using JSON or Properties:<br>  • Global application settings<br>  • Per-path defaults and configurations| 4 | 3 |
| #28 | **Export presets** for documentation workflows | 3 | 2 |
| #22 | **Quick rerun button** to repeat previous analysis per tab | 3 | 2 |


### `.crawlectignore` & Filtering

| issue | Description | Value | Complexity |
|:-:|:--|:-:|:-:|
| #23 | **Embedded `.crawlectignore` editor** (multi-line text area, per tab) | 4 | 3 |
| #24 | **Custom file filters** (e.g. show `.bak`, `.tmp`…) | 3 | 3 |
| #25 | **Optional file type and size filters** for targeted analysis | 3 | 3 |


### Export & Output

| issue | Description | Value | Complexity |
|:-:|:--|:-:|:-:|
| #8 | **Export Markdown results** per tab | 4 | 2 |
| #9 | **Open output in external viewer** directly from the GUI | 4 | 2 |
| #10 | **Custom output path and filename** configuration | 4 | 2 |


### Runtime & Installation Logic

| issue | Description | Value | Complexity |
|:-:|:--|:-:|:-:|
| #26 | **Check for Python installation** (`python --version`) on first launch | 3 | 3 |
| #27 | **Prompt user or guide to install Python** if not found | 3 | 2 |
| #30 | **Automatically create a local Python virtual environment** | 4 | 3 |
| #31 | **Install Crawlect in `venv`** via `pip install crawlect` | 4 | 3 |
| #32 | **Run Crawlect from within the `venv`**, keeping the system clean | 4 | 3 |
| #33 | **Distribute app as a native installer** using `jpackage` or Inno Setup | 3 | 4 |
| #34 | **Optionally bundle the Java Runtime Environment (JRE)** | 2 | 4 |
| #35 | **First-run logic** sets up Python, `venv`, and Crawlect automatically | 4 | 4 |



## **GUI module assignment project scope**
**(Prototype to be delivere on the 17<sup>th</sup> of April 2025)**

This prototype lays the groundwork for a scalable, user-friendly interface to *Crawlect*, ready to grow with future needs.

### Scope

1. **Assume all dependencies are installed.**
      - Don’t deal with venv, pip, or installers yet.
      - Just call crawlect as a subprocess.
      - If crawlect fails (e.g., not found), catch the error and show it inside an embedded console area
        (JTextArea, read-only; (Optionally) add contextual coloring to the output for improved readability).
      - Centralize the subprocess logic in `CrawlectExecutor` helper class.
2. **Call `crawlect --help` to introspect parameters.**
    - `crawlect --help` returns *argparse* structured info.
    - Parse with regex or line-based logic.
    - Autogenerate checkboxes, sliders, or fields for CLI flags.
    - Centralize the help parsing logic in `CrawlectHelpParser` class.
3. **Execute Crawlect based on user selections.**
    - Let the user:
      - Select a directory (JFileChooser).
      - Set output path.
      - Choose a few basic flags manually (for now, hardcode them or provide text input).
    - Build the command line.
    - Run crawlect with the provided args.
    - Pipe the output to the embedded console in real time.
    - Centralize the command builder logic in `CrawlectCommandBuilder` class.

### Structure

```text
src/
├─ gui/
│  ├─ MainWindow.java               # JFrame + layout
│  └─ ConsolePanel.java             # JTextArea wrapper
├─ logic/
│  ├─ CliOption.java                # Standard Python Argpars converter
│  ├─ CliSchemaParser.java          # Translate Crawlect CLI options JSON
│  ├─ PythonRunner.java             # Runs commands, handles stdout/stderr
│  └─ CrawlectCommandBuilder.java   # Builds final CLI command
└─ CrawlectGui.java                 # Main class
```

#### Related issues

| issue | Description |
|:-:|:--|
| #1 | Select folder to crawl |
| #2 | Configure output file name and destination |
| #3 | Trigger Crawlect execution |
| #4 | Toggle flag parameters |
| #5 | Parse available parameters from ~`crawlect --help`~ `crawlect -clischem` |
| #6 | Real-time stdout/stderr display |
| #7 | FlatLaf integration |
| #16 | Color-coded logs and warning/error highlighting |

> Additional features from the full specifications (tabs, presets, theming, packaging…) are intentionally excluded from the prototype to focus on establishing a solid and testable base architecture.

### Planning

| Laps | Description | Issues and tasks |
|:-:|:--|:--|
| Mar 22–26 | Infra & GUI Layout | #7, structure |
| Mar 27–31 | Folder/Output/Execution | #1, #2, #3 |
| Apr 01–04 | Param Parsing & Toggling | #5, #4 |
| Apr 05–06 | Output & Logging | #6, #16 |
| Apr 07–10 | Polish & Buffer Phase | final tests |
| Apr 10–17 | Presentation material | communication |

## Getting Started

1. Require:
  - [*Maven v3*](https://maven.apache.org/download.cgi)
  - [*Python v3*](https://www.python.org/downloads/)
  - [*Java v21*](https://www.oracle.com/ch-de/java/technologies/downloads/#java21)
  - [*Crawlect v1.0.5*](https://pypi.org/project/Crawlect/) or later.
    ```bash
    pip install crawlect
    ```

2. Clone repository:
```bash
git clone https://github.com/yvesguillo/crawlect-gui.git
```

3. Build / Rebuild:
```bash
mvn clean compile
```

4. Run:
```bash
mvn exec:java
```

OR specify annother entry point than the one set in the `pom.xml`.
```bash
mvn exec:java -Dexec.mainClass="ch.yvesguillo.AnnotherMainClass"
```

## Roadmap & Crazy Ideas

- Parameter autofill based on `--help` parsing.
- Presets (light `.json` configs per folder and/or global).
- Embedded `.crawlectignore` editor.
- Auto-setup venv and pip if Crawlect not found.
- Tabbed interface with progress bars.
- Theme toggle (FlatLaf dark/light and/or OS setting level).
- Output preview inside the GUI (Markdown).

Crawlect-GUI is designed to evolve alongside its users' needs, with a foundation ready for future enhancements.

## References and thanks

(TBD)

If you find Crawlect-GUI useful, **give it a ☆** to support the project!  
[![GitHub Repo stars](https://img.shields.io/github/stars/yvesguillo/crawlect-gui?style=social)](#)
