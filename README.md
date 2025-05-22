# Crawlect-GUI

**Java Swing GUI for [*Crawlect*](https://github.com/yvesguillo/crawlect)**

## Why Crawlect-GUI?

Enhance Crawlect’s accessibility by providing a GUI that simplifies trying, checking, and editing analysis runs.

> *Crawlect-GUI* is a study project initiated by [*Yves Guillo*](https://yvesguillo.ch) & [*Alexandre Jenzer*](https://github.com/Alex141298), supervised by *Aïcha Rizzotti-Kaddouri* during [*He-Arc*](https://www.he-arc.ch/en/)'s *CAS-IDD*'s *GUI* module (2025).

## Use cases

- **Learning & Exploration**  
  *A student wants to understand the structure and logic of an unfamiliar codebase.*  
  Launch Crawlect-GUI, select the project folder, and generate a Markdown summary to explore files, syntax, and logic flow.  
  Main features:  
    - Simple folder selection with `JFileChooser`
    - Output preview in embedded console or external viewer
    - FlatLaf visual theme for easy reading
    - Flag auto-fill or tooltips for helping discover CLI options

- **Legacy Code Analysis**  
  *A developer inherits a large, outdated codebase with no clear documentation.*  
  Launch Crawlect-GUI to generate an overview of the structure, identify obsolete files, and spot inconsistencies.  
  Main features:  
    - Custom file filters (e.g. show `.bak`, `.tmp`, or unused files)

- **Onboarding New Team Members**  
  *A new hire needs to get familiar with the company’s software projects.*  
  Explore multiple codebases, browse generated Markdown overviews, and customize outputs to match learning style, reducing ramp-up time.  
  Main features:  
    - Save/load analysis presets
    - FlatLaf light/dark theme toggle
    - Markdown export button to open/save results

- **Troubleshooting & Debugging**  
  *A team member needs to check if ignored files or wrong extensions are causing issues.*  
  Quickly rerun the analysis with different `.crawlectignore` or flag parameters, tweaking depth or file types to isolate the problem, all from a single window.  
  Main features:  
    - Embedded `.crawlectignore` editor (multi-line text area)
    - Flag toggles for `--depth`, `--gitignore`, etc.
    - Embedded console showing warnings/errors in real-time
    - Button to quickly rerun last analysis with changed settings

- **Comparative Analysis**  
  *A lead developer wants to compare two branches of a repo or two folders for changes in structure.*  
  Open two tabs in Crawlect-GUI, run separate analyses, and export the outputs for side-by-side comparison, making it easy to detect file additions or refactoring.  
  Main features:  
    - Multi-tab interface (per analysis)
    - Independent per-tab input parameters and output
    - Markdown output saving per tab
    - Optional: “Sync settings across tabs” toggle

- **Refactoring Preparation**  
  *A tech lead wants to clean up a monolithic module before a big refactoring.*  
  Run Crawlect-GUI with different scopes and parameters to isolate the most complex or deeply nested parts, using the generated overview to plan restructuring.  
  Main features:  
    - Dropdown to set max depth or scope
    - Filter view by file type or size (optional feature)
    - Export-to-Markdown with customized output path

- **Documentation Assistance**  
  *A tech writer needs to update the architecture documentation of a project.*  
  Launch Crawlect-GUI to generate a Markdown snapshot of the codebase and integrate it directly into writing tools.  
  Main features:  
    - GUI field to set custom output path/name
    - "Open output in viewer" button after generation
    - Export presets for documentation (e.g. skip temp folders)
    - Minimal view mode (hide advanced flags to reduce clutter)

## Overview

### Technologies

- **Frontend**: Java Swing with [`FlatLaf`](https://github.com/JFormDesigner/FlatLaf)
- **Backend**: Python Crawlect CLI app via [`Runtime`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/Runtime.html#getRuntime()) class.
- **Installer**: via [`jpackage`](https://docs.oracle.com/en/java/javase/24/docs/specs/man/jpackage.html).

### Core Features

- Select folder to crawl.
- Configure output file name and destination.
- Set variable parameters such as `--llm-model` or `--llm-api-key`…
- Toggle Crawlect flag parameters such as `--depth`, `--gitignore`…
- Trigger Crawlect execution from GUI.
- Display stdout/stderr logs in real-time in an embedded console.

### Backend Integration

- Check if Python is installed (`python --version`)
  - If not installed, prompt user, provide install instructions or:
    - Create a local **Python virtual environment** (in-app folder or user-path).
    - Install Crawlect via `pip install crawlect` inside the `venv`.
- Parse available *Crawlect* parameters from `crawlect --help` output.
- Execute Crawlect analysis request.

### Installer & Packaging

- Use **`jpackage`** (cross-platform) or **Inno Setup** (Windows) to:
  - Bundle the Java app.
  - Optionally include the JRE.
  - Launch first-run logic to:
    - Check for Python.
    - Create and configure `venv`.
    - Install Crawlect if needed.

### User Experience Enhancements

- Clean install: no pollution of the user's global Python or system paths.
- Real-time, color-coded logs and feedback
- Allow **multiple analyses** over **multiple tabs**
- Manage **global vs. per-tab parameter scopes**
- **.crawlectignore** creation and editing per tab/folder.
- Persistent **settings backup** (e.g. via JSON or Properties):
  - Global app settings.
  - Per-path defaults and recent configurations.
- Consistent UI/UX using FlatLaf theme (light/dark toggle optional).
- Toggle show/Hide embeded console.

## Prototype Scope

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
│  ├─ MainFrame.java                      # JFrame + layout
│  └─ ConsolePanel.java                   # JTextArea wrapper
├─ logic/
│  ├─ CrawlectExecutor.java               # Runs commands, handles stdout/stderr
│  ├─ CrawlectHelpParser.java (optional)  # Parses --help
│  └─ CrawlectCommandBuilder.java         # Builds final CLI command
└─ CrawlectGuiApp.java                    # Main class
```

## Getting Started

(TBD)

## Roadmap & Crazy Ideas

- Parameter autofill based on `--help` parsing.
- Presets (light `.json` configs per folder and/or global).
- Embedded `.crawlectignore` editor.
- Auto-setup venv and pip if Crawlect not found.
- Tabbed interface with progress bars.
- Theme toggle (FlatLaf dark/light and/or OS seting level).
- Output preview inside the GUI (Markdown).

## References and thanks

(TBD)

If you find Crawlect-GUI useful, **give it a ☆** to support the project!  
[![GitHub Repo stars](https://img.shields.io/github/stars/yvesguillo/crawlect-gui?style=social)](#)