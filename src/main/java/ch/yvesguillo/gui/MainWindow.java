package ch.yvesguillo.gui;

import ch.yvesguillo.logic.CliOption;
import ch.yvesguillo.logic.CliSchemaParser;
import ch.yvesguillo.logic.PythonRunner;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {

    private final JList<String> groupList;
    private final DefaultListModel<String> groupListModel;
    private final JPanel optionPanel;

    private final Map<CliOption, JComponent> inputMap = new HashMap<>();

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
        groupList.setFont(new Font("Roboto", Font.PLAIN, 12));
        groupList.addListSelectionListener(e -> updateOptionPanel(groupList.getSelectedValue()));

        JScrollPane groupScroll = new JScrollPane(groupList);
        groupScroll.setPreferredSize(new Dimension(250, 600));
        add(groupScroll, BorderLayout.WEST);

        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        JScrollPane optionScroll = new JScrollPane(optionPanel);
        add(optionScroll, BorderLayout.CENTER);

        updateOptionPanel(groupList.getSelectedValue());
    }

    private void updateOptionPanel(String group) {
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
            JLabel label = new JLabel(option.getPrimaryFlag());
            label.setToolTipText(option.help);

            JComponent inputField;

            if (option.isBoolean) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected("true".equalsIgnoreCase(option.defaultValue));
                inputField = checkBox;
            } else if (option.hasChoices) {
                JComboBox<String> comboBox = new JComboBox<>(option.choices.toArray(new String[0]));
                if (option.defaultValue != null) comboBox.setSelectedItem(option.defaultValue);
                inputField = comboBox;
            } else {
                JTextField textField = new JTextField(20);
                if (option.defaultValue != null) textField.setText(option.defaultValue);
                inputField = textField;
            }

            inputField.setToolTipText(option.help);
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

        // Add a Run button.
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton runButton = new JButton("Run Crawlect");
        runButton.addActionListener(event -> runCrawlectCommand());

        optionPanel.add(runButton, gbc);

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

    private void runCrawlectCommand() {
        List<String> args = new ArrayList<>();

        for (Map.Entry<CliOption, JComponent> entry : inputMap.entrySet()) {
            CliOption option = entry.getKey();
            JComponent field = entry.getValue();
            String flag = option.getPrimaryFlag();

            if (option.isBoolean && field instanceof JCheckBox check && check.isSelected()) {
                args.add(flag);
            } else if (option.hasChoices && field instanceof JComboBox combo) {
                String value = combo.getSelectedItem().toString();
                args.add(flag);
                args.add(value);
            } else if (field instanceof JTextField text) {
                String value = text.getText().trim();
                if (!value.isEmpty()) {
                    args.add(flag);
                    args.add(value);
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