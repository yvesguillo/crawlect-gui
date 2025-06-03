package ch.yvesguillo.gui;

import ch.yvesguillo.logic.CliOption;
import ch.yvesguillo.logic.CliSchemaParser;
import ch.yvesguillo.logic.PythonRunner;

import javax.swing.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final JList<String> groupList;
    private final DefaultListModel<String> groupListModel;
    private final JPanel optionPanel;

    // Style.
    private static final Font mainFont = UIManager.getFont("Label.font").deriveFont(12f);
    private static final Font heavyFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f);

    private final Map<CliOption, JComponent> inputMap = new HashMap<>();
    private final Map<CliOption, Object> storedValues = new HashMap<>(); // Cache values when refreshing option panel for input persistance.

    private final String projectName;
    private final String projectVersion;

    public MainWindow(List<String> groups) {
        // Set icon.
        try {
                setIconImages(List.of(
                    new ImageIcon(getClass().getResource("/icons/crawlect-gui_16.png")).getImage(),
                    new ImageIcon(getClass().getResource("/icons/crawlect-gui_32.png")).getImage(),
                    new ImageIcon(getClass().getResource("/icons/crawlect-gui_64.png")).getImage(),
                    new ImageIcon(getClass().getResource("/icons/crawlect-gui_256.png")).getImage()
                ));
            } catch (Exception error) {
                System.err.println("[GUI] Could not load icon: " + error.getMessage());
            }

        // Set macOS Dock icon.
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                java.awt.Taskbar.getTaskbar().setIconImage(
                    new ImageIcon(getClass().getResource("/icons/crawlect-gui_64-mac.png")).getImage()
                );
            } catch (UnsupportedOperationException | SecurityException error) {
                System.err.println("[GUI] Could not set macOS Dock icon: " + error.getMessage());
            }
        }

        // Set title from manifest (fallback if null).
        Properties props = new Properties();
        try (InputStream stream = MainWindow.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (stream != null) {
                props.load(stream);
            }
        } catch (IOException error) {
            System.err.println("[Init] Failed to load version.properties: " + error.getMessage());
        }
        this.projectName = props.getProperty("project.name", "Crawlect-GUI");
        this.projectVersion = props.getProperty("project.version", "DEV");

        setTitle(projectName + " " + projectVersion);
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout: Left panel for groups, right panel for options.
        setLayout(new BorderLayout());

        groupListModel = new DefaultListModel<>();
        groups.forEach(groupListModel::addElement);
        groupList = new JList<>(groupListModel);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setSelectedIndex(0);
        groupList.setFont(mainFont);
        groupList.addListSelectionListener(e -> updateOptionPanel(groupList.getSelectedValue()));

        JScrollPane groupScroll = new JScrollPane(groupList);
        groupScroll.setPreferredSize(new Dimension(250, 600));
        add(groupScroll, BorderLayout.WEST);

        // Add a Run button.
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(groupScroll, BorderLayout.CENTER);

        JButton runButton = new JButton("Run Crawlect ▶");
        runButton.setFont(heavyFont);
        runButton.setPreferredSize(new Dimension(250, 40));
        runButton.addActionListener(event -> runCrawlectCommand());
        leftPanel.add(runButton, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(optionPanel, BorderLayout.NORTH); // Forces top alignment.
        JScrollPane optionScroll = new JScrollPane(wrapper);
        add(optionScroll, BorderLayout.CENTER);

        // Check for existing config.
        loadConfig();

        updateOptionPanel(groupList.getSelectedValue());
    }

    public class ComboItem {
        private final String label;
        private final String value;

        public ComboItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label; // What gets displayed in the combo box.
        }
    }

    private void updateOptionPanel(String group) {
        // Save current input values before clearing
        for (Map.Entry<CliOption, JComponent> entry : inputMap.entrySet()) {
            CliOption option = entry.getKey();
            JComponent field = entry.getValue();

            if (option.isBoolean && field instanceof JCheckBox check) {
                storedValues.put(option, check.isSelected());
            } else if (option.hasChoices && field instanceof JComboBox<?> combo) {
                Object selectedItem = combo.getSelectedItem();
                if (selectedItem instanceof ComboItem item) {
                    storedValues.put(option, item.getValue());
                }
            } else if (field instanceof JTextField text) {
                storedValues.put(option, text.getText());
            }
        }
        optionPanel.removeAll();
        inputMap.clear();

        optionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;

        List<CliOption> groupOptions = CliSchemaParser.getInstance().getOptionsForGroup(group);

        int row = 0;
        for (CliOption option : groupOptions) {
            String metalabel = "";
            if (option.metavar != null) metalabel = option.metavar; else  metalabel = option.getPrimaryFlag();
            JLabel label = new JLabel(String.format("%-20s", metalabel)); // Padding.
            label.setPreferredSize(new Dimension(160, 25)); // Fixed width.
            label.setFont(mainFont);
            label.setToolTipText(option.help);

            JComponent inputField;

            if (option.isBoolean) {
                JCheckBox checkBox = new JCheckBox();
                Boolean saved = (Boolean) storedValues.get(option);
                checkBox.setSelected(saved != null ? saved : "true".equalsIgnoreCase(option.defaultValue));
                inputMap.put(option, checkBox);
                inputField = checkBox;
            } else if (option.hasChoices) {
                JComboBox<ComboItem> comboBox = new JComboBox<>();

                if (!option.choices.contains("")) {
                    comboBox.addItem(new ComboItem("(none)", "")); // Empty choice with friendly label.
                }

                for (String choice : option.choices) {
                    comboBox.addItem(new ComboItem(choice, choice)); // Label and value are the same.
                }

                // Set default value or restore user's setting.
                String saved = (String) storedValues.get(option);
                String target = (saved != null) ? saved : option.defaultValue;
                if (target != null) {
                    for (int i = 0; i < comboBox.getItemCount(); i++) {
                        ComboItem item = comboBox.getItemAt(i);
                        if (item.getValue().equals(target)) {
                            comboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                inputField = comboBox;
            } else {
                JTextField textField = new JTextField(20);
                String saved = (String) storedValues.get(option);
                if (saved != null) textField.setText(saved);
                // We do not want to populate default value for now.
                // else if (option.defaultValue != null) textField.setText(option.defaultValue);

                // Wrap in panel if needed.
                JComponent displayComponent;

                if (option.getPrimaryFlag().equals("--path")) {
                    JButton browse = new JButton("📁");
                    browse.setMargin(new Insets(2, 4, 2, 4));
                    browse.setToolTipText("Select folder to scan");
                    browse.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int result = chooser.showOpenDialog(this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            textField.setText(chooser.getSelectedFile().getAbsolutePath());
                        }
                    });

                    JPanel fileInputPanel = new JPanel(new BorderLayout());
                    fileInputPanel.add(textField, BorderLayout.CENTER);
                    fileInputPanel.add(browse, BorderLayout.EAST);
                    displayComponent = fileInputPanel;

                } else if (option.getPrimaryFlag().equals("--output")) {
                    JButton browse = new JButton("📁");
                    browse.setMargin(new Insets(2, 4, 2, 4));
                    browse.setToolTipText("Select output file path");
                    browse.addActionListener(e -> {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Choose output file (it will be created)");
                        String filename = "";
                        // Use default value as filename if exist.
                        if (option.defaultValue != null) filename = option.defaultValue;
                        chooser.setSelectedFile(new File(filename));
                        int result = chooser.showSaveDialog(this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            textField.setText(chooser.getSelectedFile().getAbsolutePath());
                        }
                    });

                    JPanel fileInputPanel = new JPanel(new BorderLayout());
                    fileInputPanel.add(textField, BorderLayout.CENTER);
                    fileInputPanel.add(browse, BorderLayout.EAST);
                    displayComponent = fileInputPanel;

                } else if (option.getPrimaryFlag().equals("--output-prefix")) {
                    // Ignore this field. Might adapt *Crawlect* in the future.
                    continue;
                } else if (option.getPrimaryFlag().equals("--output-suffix")) {
                    // Ignore this field. Might adapt *Crawlect* in the future.
                    continue;
                } else {
                    displayComponent = textField;
                }

                // Always track the actual field for value capture.
                inputMap.put(option, textField);

                // But show the decorated version if it exists.
                inputField = displayComponent;
            }

            inputField.setToolTipText(option.help);
            inputField.setFont(mainFont);

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;
            optionPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            optionPanel.add(inputField, gbc);

            row++;
        }

        optionPanel.revalidate();
        optionPanel.repaint();
    }

    public static File getUserConfigFile() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File configFile;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            configFile = (appData != null)
                    ? new File(appData, "crawlect-gui/config.json")
                    : new File(userHome, "AppData/Roaming/crawlect-gui/config.json");
        } else if (os.contains("mac")) {
            configFile = new File(userHome, "Library/Application Support/crawlect-gui/config.json");
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            configFile = (xdg != null)
                    ? new File(xdg, "crawlect-gui/config.json")
                    : new File(userHome, ".config/crawlect-gui/config.json");
        }

        File parent = configFile.getParentFile();
        if (!parent.exists()) {
            boolean created = parent.mkdirs();
            System.out.println("[Config] Creating config directory: " + parent.getAbsolutePath() +
                    " → " + (created ? "OK" : "FAILED"));
        }

        return configFile;
    }

    public void saveConfig() {
        File configFile = getUserConfigFile();
        Map<String, Object> simpleMap = new HashMap<>();

        for (Map.Entry<CliOption, Object> entry : storedValues.entrySet()) {
            simpleMap.put(entry.getKey().getPrimaryFlag(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, simpleMap);
            System.out.println("[Config] Saved to: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Save failed: " + e.getMessage());
        }
    }

    public void loadConfig() {
        File configFile = getUserConfigFile();
        if (!configFile.exists()) {
            System.out.println("[Config] No config file found at: " + configFile.getAbsolutePath());
            return;
        }

        try {
            Map<String, Object> simpleMap = new ObjectMapper().readValue(
                configFile,
                new TypeReference<Map<String, Object>>() {}
            );

            for (CliOption option : CliSchemaParser.getInstance().getAllOptions()) {
                String key = option.getPrimaryFlag();
                Object val = simpleMap.get(key);
                if (val != null) {
                    storedValues.put(option, val);
                }
            }
            System.out.println("[Config] Loaded from: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Load failed: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        for (Map.Entry<CliOption, JComponent> entry : inputMap.entrySet()) {
            CliOption option = entry.getKey();
            Object value = storedValues.get(option);

            // Mandatory: --path
            if (option.getPrimaryFlag().equals("--path")) {
                String path = (value instanceof String s) ? s.trim() : "";
                if (path.isEmpty()) {
                    showValidationError("The '--path' field is required.");
                    return false;
                }
            }

            // Mandatory: --output
            if (option.getPrimaryFlag().equals("--output")) {
                String output = (value instanceof String s) ? s.trim() : "";
                if (output.isEmpty()) {
                    showValidationError("The '--output' field is required.");
                    return false;
                }
            }

            // Integer: --depth
            if (option.getPrimaryFlag().equals("--depth")) {
                String str = (value instanceof String s) ? s.trim() : "";
                if (!str.isEmpty()) {
                    try {
                        Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        showValidationError("The '--depth' field must be a valid integer.");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
    }

    private String handleOutputFileOverwrite(List<String> command) {
        for (int i = 0; i < command.size(); i++) {
            String flag = command.get(i);
            if (flag.equals("-o") || flag.equals("--output")) {
                if (i + 1 < command.size()) {
                    String outputPath = command.get(i + 1);
                    java.io.File outputFile = new java.io.File(outputPath);

                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && (!parentDir.exists() || !parentDir.canWrite())) {
                        JOptionPane.showMessageDialog(this,
                                "Cannot write to the output directory:\n" + parentDir.getAbsolutePath(),
                                "Output Path Error",
                                JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    if (outputFile.exists()) {
                        int choice = JOptionPane.showOptionDialog(this,
                                "The file '" + outputFile.getName() + "' already exists.\nWhat would you like to do?",
                                "Output File Exists",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                new String[]{"Change file", "Overwrite", "Cancel"},
                                "Change file");

                        if (choice == 0) { // Change file.
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setSelectedFile(outputFile);
                            int result = fileChooser.showSaveDialog(this);
                            if (result == JFileChooser.APPROVE_OPTION) {
                                String newPath = fileChooser.getSelectedFile().getAbsolutePath();
                                command.set(i + 1, newPath);
                                return newPath;
                            } else {
                                return null; // Cancelled.
                            }
                        } else if (choice == 1) { // Overwrite.
                            if (!outputFile.delete()) {
                                JOptionPane.showMessageDialog(this,
                                        "Failed to delete the existing output file.\nPlease try changing the file name.",
                                        "File Deletion Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return null;
                            }
                            return outputPath;
                        } else {
                            return null; // Cancel.
                        }
                    }
                }
            }
        }
        return "ok"; // No output file set or file doesn't exist.
    }

    private void captureCurrentInputs() {
        for (Map.Entry<CliOption, JComponent> entry : inputMap.entrySet()) {
            CliOption option = entry.getKey();
            JComponent field = entry.getValue();

            if (option.isBoolean && field instanceof JCheckBox check) {
                storedValues.put(option, check.isSelected());
            } else if (option.hasChoices && field instanceof JComboBox<?> combo) {
                Object selectedItem = combo.getSelectedItem();
                if (selectedItem instanceof ComboItem item) {
                    storedValues.put(option, item.getValue());
                }
            } else if (field instanceof JTextField text) {
                storedValues.put(option, text.getText());
            }
        }
    }

    private void runCrawlectCommand() {
        // store visible inputs before collecting args.
        captureCurrentInputs();

        if (!validateInputs()) {
            return; // Stop if validation fails.
        }

        List<String> args = new ArrayList<>();

        List<CliOption> allOptions = CliSchemaParser.getInstance().getAllOptions();

        for (CliOption option : allOptions) {
            String flag = option.getPrimaryFlag();
            Object value = storedValues.get(option);

            if (option.isBoolean) {
                if (Boolean.TRUE.equals(value) && !Objects.equals(option.defaultValue, "True")) {
                    args.add(flag); // Add positive flag.
                } else if (Boolean.FALSE.equals(value) && !Objects.equals(option.defaultValue, "False")) {
                    String negativeFlag = option.getNegativeFlag();
                    if (negativeFlag != null) {
                        args.add(negativeFlag); // Add --no-flag form.
                    }
                }
            } else if (option.hasChoices) {
                if (value instanceof String strVal && !strVal.isEmpty()) {
                    args.add(flag);
                    args.add(strVal);
                }
                // else: skip empty.
            } else {
                if (value instanceof String strVal) {
                    strVal = strVal.trim();
                    if (!strVal.isEmpty()) {
                        args.add(flag);
                        args.add(strVal);
                    }
                }
            }
        }

        try {

            String outputCheck = handleOutputFileOverwrite(args);
            if (outputCheck == null) {
                return; // User cancelled.
            }

            // Check if path exists (for --path or -p)
            for (int i = 0; i < args.size(); i++) {
                String flag = args.get(i);
                if ((flag.equals("--path") || flag.equals("-p")) && i + 1 < args.size()) {
                    String pathStr = args.get(i + 1);
                    java.io.File path = new java.io.File(pathStr);
                    if (!path.exists() || !path.isDirectory()) {
                        JOptionPane.showMessageDialog(this,
                                "The selected path to scan does not exist or is not a directory:\n" + pathStr,
                                "Invalid Path to Scan",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String output = PythonRunner.runCrawlect(args);
            JTextArea textArea = new JTextArea(output);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(480, 270));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Crawlect finished",
                    JOptionPane.INFORMATION_MESSAGE);

            // SAve current settings.
            saveConfig();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error running Crawlect: " + ex.getMessage(),
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}