# ![Crawlect-GUI](src/main/resources/icons/crawlect-gui_32.png) Crawlect-GUI

**Your friendly GUI companion for [*Crawlect*](https://github.com/yvesguillo/crawlect)**
***Give your keyboard a well-deserved break!** Now you can crawl, collect, document your codebase and enhance it with AI LLM analysis, without touching the command line!*

![Crawlect-GUI](images/crawlect-gui.avif)

**Crawlect-GUI** is a Java-based Swing interface designed to make [**Crawlect**](https://github.com/yvesguillo/crawlect); the Python CLI tool; more accessible and user-friendly. No more typing commands by hand: simply click, choose options, and let Crawlect-GUI handle the rest!

Whether you're documenting your project, analyzing unfamiliar codebases, or collaborating with your team, **Crawlect-GUI** offers a straightforward, visual way to harness the full power of Crawlect.

> *Crawlect-GUI* is a study project initiated by [*Yves Guillo*](https://yvesguillo.ch) & [*Alexandre Jenzer*](https://github.com/Alex141298), supervised by *Aïcha Rizzotti-Kaddouri* during [*He-Arc*](https://www.he-arc.ch/en/)'s *CAS-IDD*'s *GUI* module (2025).

## Why Crawlect-GUI?

Ever wish Crawlect had a more visual, intuitive interface? We've got you covered. Crawlect-GUI:

- Provides a clean, intuitive GUI to manage Crawlect parameters.
- Fetches and dynamically displays available CLI options directly from Crawlect.
- Saves and recalls your preferences automatically, so setup is even quicker next time!
- No need to memorize Crawlect's parameters, all is there.

***Think of Crawlect-GUI as your Crawlect assistant; friendly, efficient, and all about simplicity.***

## Use Cases

- Quickly generate Markdown documentation without command-line fuss.
- Understand and visualize project structures instantly.
- Share clear, comprehensive project digests with your teammates.
- Easily test and manage complex crawling configurations.

## Scenarios

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

## Getting Started

Crawlect-GUI is built in Java (Swing) and integrates seamlessly with your existing Crawlect installation.

### Prerequisites

  - [*Maven v3*](https://maven.apache.org/download.cgi)
  - [*Python v3*](https://www.python.org/downloads/)
  - [*Java v21+*](https://www.oracle.com/ch-de/java/technologies/downloads/#java21)
  - [*Crawlect v1.0.5+*](https://pypi.org/project/Crawlect/)
    ```bash
    pip install crawlect
    ```

### Installation & Run

Clone, build, and run Crawlect-GUI in a breeze:

```bash
git clone https://github.com/yvesguillo/crawlect-gui.git
cd crawlect-gui
mvn compile exec:java
```

That's it!

### Quick Scan

Once the GUI launches:

1. **Choose the path** of the folder to crawl.
2. **Set your output** Markdown file location.
3. **Customize** your Crawlect options through the intuitive panels.
4. Click **"Run Crawlect"** and let Crawlect-GUI handle the rest.

## How Does Crawlect-GUI Work?

Crawlect-GUI dynamically retrieves available command-line options directly from Crawlect. It then:

1. Presents options neatly grouped and easy-to-navigate.
2. Validates inputs in real-time.
3. Runs Crawlect seamlessly in the background.
4. Handles output gracefully and displays results directly in the interface.

### Technologies

- Java Swing with [`FlatLaf`](https://github.com/JFormDesigner/FlatLaf)
- Python [Crawlect](https://github.com/yvesguillo/crawlect) CLI app via [`ProcessBuilder`](https://docs.oracle.com/en/java/javase/24/core/attributes-that-processbuilder-manages.html) class.
- JSON parsing with [Jackson](https://github.com/FasterXML/jackson)

## GUI Features

- **Dynamic Option Panels**: Adjust according to Crawlect's CLI schema.
- **Persistent User Preferences**: Saves your last-used settings.
- **Cross-platform Friendly**: Special care taken for macOS, Windows, and Linux users.
- **FlatLaf Dark Theme**: Easy on the eyes, professional look.

## Structure

```text
src/
└─ main/
   ├─ java/
   │  └─ ch/
   │     └─ yvesguillo/
   │        ├─ CrawlectGUI.java         Main entry point, initializes the GUI and controllers.
   │        ├─ controller/              Manages interaction between the view and the underlying Crawlect Python CLI.
   │        │  ├─ CrawlectRunner.java
   │        │  ├─ MainController.java
   │        │  ├─ PythonRunner.java
   │        │  └─ UserSettings.java
   │        ├─ model/                   Data representation and parsing logic for Crawlect's CLI schema and
   │        │  ├─ CliOption.java
   │        │  ├─ CliSchemaParser.java
   │        │  └─ ComboItem.java
   │        └─ view/                    Java Swing classes responsible for GUI rendering and user interaction handling.
   │           ├─ MainWindow.java
   │           └─ ShowMessages.java
   └─ resources/                        Non-code files (icons, version properties) utilized at runtime.
      ├─ icons/
      └─ version.properties
```

```text
+-------------------+
|   CrawlectGUI     |
+-------------------+
| - appName         |
| - appVersion      |
| - view            |
+-------------------+
| + main(args)      |
| - setAppIcon()    |
+---------+---------+
          |
          v
+--------------------+
|    MainWindow      |<----------+
+--------------------+           |
| - groupList        |           |
| - optionPanel      |           |
| - inputMap         |           |
| - storedValues     |           |
+--------------------+           |
| + initialize()     |           |
| + updateOptionPanel()|         |
+---------+----------+           |
          |                      |
          v                      |
+--------------------+           |
|   MainController   |-----------+
+--------------------+
| - instance         |
| - view             |
+--------------------+
| + runnRequest()    |
| + folderPathModifRequest()|
| + filePathModifRequest()  |
+---------+----------+
          |
          v
+--------------------+
|   CrawlectRunner   |
+--------------------+
| + runCrawlectCommand()|
| + validateInputs() |
| + captureCurrentInputs()|
+---------+----------+
          |
          v
+--------------------+
|    PythonRunner    |
+--------------------+
| + runCrawlect()    |
| + getCliSchemaJson()|
| + getPythonCommand()|
+--------------------+

+-------------------+     uses
|   CliSchemaParser |<---------------+
+-------------------+                |
| - options         |                |
+-------------------+                |
| + initialize()    |                |
| + getOptionsForGroup()|            |
| + getGroups()     |                |
+-------------------+                |
                                     |
+-------------------+     parses     |
|     CliOption     |                |
+-------------------+                |
| - flags           |                |
| - type            |                |
| - defaultValue    |                |
+-------------------+                |
                                     |
+-------------------+     displays   |
|     ComboItem     |<---------------+
+-------------------+
| - label           |
| - value           |
+-------------------+

+-------------------+
|   UserSettings    |
+-------------------+
| - storedValues    |
| - configFile      |
+-------------------+
| + initialize()    |
| + saveConfig()    |
| + loadConfig()    |
+-------------------+

+-------------------+
|   ShowMessages    |
+-------------------+
| + showValidationError()|
+-------------------+

```

```text
               +-----------------------------+
               |         Launch App          |
               +--------------+--------------+
                              |
                              v
               +-----------------------------+
               |     Load CLI Schema JSON    |
               |    (From Crawlect Python)   |
               +--------------+--------------+
                              |
                              v
               +-----------------------------+
               |        Initialize GUI       |
               |  (MainWindow with options)  |
               +--------------+--------------+
                              |
                              v
               +-----------------------------+
               |     User Selects Options    |
               |    (Path, Output, Flags)    |
               +--------------+--------------+
                              |
                              v
               +-----------------------------+
               |        User Selects         |
               |          click Run          |
               +--------------+--------------+
                              |
                              v
               +-----------------------------+
               |       Validate Inputs       |
               |                             |
               +--------------+--------------+
                              |
              +---------------+---------------+
              |                               |
           Invalid                          Valid
              v                               v
 +-------------------------+     +-------------------------+
 |    Show Validation      |     |    Build Crawlect CLI   |
 |      Error Dialog       |     |       Command Args      |
 +-------------------------+     +------------+------------+
                                              |
                                              v
                                 +-------------------------+
                                 |    Check Output File    |
                                 |   (Exists? Overwrite?)  |
                                 +------------+------------+
                                              |
                    +-------------------------+-------------+
                    |                         |             |
                 Exists                   Overwrite     Exists Not
                    v                         v             |
          +-------------------+     +-------------------+   |
          |   Prompt User to  |     |    Delete & Run   |   |
          |   Change/Cancel   |     |                   |   |
          +-------------------+     +---------+---------+   |
                                              |             |
                                              +-------------+
                                              |
                                              v
                                    +-------------------+
                                    |  Execute Crawlect |
                                    |  Python CLI Call  |
                                    +---------+---------+
                                              |
                                              v
                                    +-------------------+
                                    |      Process      |
                                    |       Ends        |
                                    +---------+---------+
                                              |
                                              v
                                    +-------------------+
                                    |   Display Result  |
                                    |   in GUI Dialog   |
                                    +---------+---------+
                                              |
                                              v
                                    +-------------------+
                                    |     Save User     |
                                    |   Settings JSON   |
                                    +---------+---------+
                                              |
                                              v
                                    +-------------------+
                                    |    Await Next     |
                                    |    User Action    |
                                    +-------------------+
```

## Roadmap & Crazy Ideas

- Presets (lightweight `.json` configs per folder and/or global).
- Embedded `.crawlectignore` editor.
- Auto-setup venv and pip if Crawlect not found.
- Tabbed interface with progress bars.
- Theme toggle (FlatLaf dark/light and/or OS setting level).
- Output preview inside the GUI (Markdown).
- Make it installable with requirement check and fetch.

## Contributing
Got ideas? Spot a bug? Wanna make this thing even cooler?  
Feel free to fork, star, or open an issue — we’d love to hear from you!

If you find Crawlect-GUI useful, **give it a ☆** to support the project!  
[![GitHub Repo stars](https://img.shields.io/github/stars/yvesguillo/crawlect-gui?style=social)](#)
