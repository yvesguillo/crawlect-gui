# Crawlect-GUI
**Java Swing GUI for Crawlect.**

## Overview

### Purpose

Provide a desktop GUI to run Crawlect with no CLI knowledge required.

### Technologies

Java Swing (frontend), Python (backend via Crawlect), optional installer.

## Core Features

- Select folder to crawl.
- Configure output name/path.
- Trigger Crawlect execution.
- Show output log/result in a text area.
- Toggle Flag parameters.
- Handle errors gracefully.

## Backend Integration

- Check if Python is installed (python --version).
- Create local virtual environment.
- Install Crawlect via pip into venv.
- Execute Crawlect inside the venv.


## Installer & Packaging

- Use jpackage or Inno Setup to:
- Bundle the Java app.
- Optionally bundle the JRE.
- Trigger first-run checks and venv setup.

## User Experience

- Clean installation (no global Python dependencies).
- Informative logs and error handling.
- Option to reset or reinstall Crawlect backend.
- Allow several analysis with over several tabs.
- Global and analysis tab scope paramèters
- In-analysis-tab related .crawlectignore creation / edition.
- Settings backup, both general and path related
