package ch.yvesguillo.gui;

import ch.yvesguillo.logic.CliOption;
import ch.yvesguillo.logic.CliSchemaParser;
import ch.yvesguillo.logic.PythonRunner;

import javax.swing.*;
import java.awt.*;
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
        // Set title from manifest (fallback if null).
        Package pkg = MainWindow.class.getPackage();
        this.projectName = (pkg.getImplementationTitle() != null) ? pkg.getImplementationTitle() : "Crawlect-GUI";
        this.projectVersion = (pkg.getImplementationVersion() != null) ? pkg.getImplementationVersion() : "DEV";

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
            JLabel label = new JLabel(String.format("%-20s", option.getPrimaryFlag())); // Padding.
            label.setPreferredSize(new Dimension(160, 25)); // Fixed width.
            label.setFont(mainFont);
            label.setToolTipText(option.help);

            JComponent inputField;

            if (option.isBoolean) {
                JCheckBox checkBox = new JCheckBox();
                Boolean saved = (Boolean) storedValues.get(option);
                checkBox.setSelected(saved != null ? saved : "true".equalsIgnoreCase(option.defaultValue));
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
                else if (option.defaultValue != null) textField.setText(option.defaultValue);
                inputField = textField;
            }

            inputField.setToolTipText(option.help);
            inputField.setFont(mainFont);
            inputMap.put(option, inputField);

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

    private String handleOutputFileOverwrite(List<String> command) {
        for (int i = 0; i < command.size(); i++) {
            String flag = command.get(i);
            if (flag.equals("-o") || flag.equals("--output")) {
                if (i + 1 < command.size()) {
                    String outputPath = command.get(i + 1);
                    java.io.File outputFile = new java.io.File(outputPath);

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

            String output = PythonRunner.runCrawlect(args);
            JTextArea textArea = new JTextArea(output);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(800, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Crawlect finished",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error running Crawlect: " + ex.getMessage(),
                    "Execution Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}