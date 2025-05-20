# Crawlect-GUI

**Java Swing GUI for [Crawlect](https://github.com/yvesguillo/crawlect)**

## Why Crawlect-GUI?

Enhance Crawlect’s accessibility by providing a GUI that simplifies trying, checking, and editing analysis runs.

> *Crawlect-GUI* is a study project initiated by [*Yves Guillo*](https://yvesguillo.ch) & [*Alexandre Jenzer*](https://github.com/Alex141298), supervised by *Aïcha Rizzotti-Kaddouri* during [*He-Arc*](https://www.he-arc.ch/en/)'s *CAS-IDD*'s *GUI* module (2025).

## Use cases

(TBD)

## Overview

### Technologies

- **Frontend**: Java Swing (with [FlatLaf](https://github.com/JFormDesigner/FlatLaf) for modern UI).
- **Backend**: Python (running Crawlect inside a virtual environment).
- **Installer**: Optional (via `jpackage` or `Inno Setup`).

### Core Features

- Select folder to crawl.
- Configure output file name and destination.
- Trigger Crawlect execution from GUI.
- Display stdout/stderr logs in real-time (output area).
- Toggle Crawlect flag parameters (e.g. `--depth`, `--gitignore`, etc.).
- Graceful error handling and user-friendly messaging.

### Backend Integration

- Check if Python is installed (`python --version`)
- If not installed, prompt  user or provide install instructions.
- Create a local **Python virtual environment** (in-app folder or user-path).
- Install Crawlect via `pip install crawlect` inside the `venv`.
- Execute Crawlect inside the `venv` for each analysis request.
- Parse available *Crawlect* parameters from `crawlect --help` output.

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
- Toggle show/Hide embed console.

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
    - Pipe the output to the console window in real time.
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
- Theme toggle (FlatLaf dark/light).
- Output preview inside the GUI (Markdown).

## References and thanks

(TBD)

If you find Crawlect-GUI useful, **give it a ☆** to support the project!  
[![GitHub Repo stars](https://img.shields.io/github/stars/yvesguillo/crawlect-gui?style=social)](#)