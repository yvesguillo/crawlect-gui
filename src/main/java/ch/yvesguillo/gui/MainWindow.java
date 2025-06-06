package ch.yvesguillo.gui;

import ch.yvesguillo.logic.CliOption;
import ch.yvesguillo.logic.CliSchemaParser;
import ch.yvesguillo.logic.UserSettings;
import ch.yvesguillo.logic.CrawlectRunner;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final JList<String> groupList;
    private final DefaultListModel<String> groupListModel;
    private final JPanel optionPanel;

    // Style.
    private static final Font mainFont = UIManager.getFont("Label.font").deriveFont(12f);
    private static final Font heavyFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f);

    // Cache form composition.
    private final Map<CliOption, JComponent> inputMap = new HashMap<>();
    // Cache form values for input persistance.
    private Map<CliOption, Object> storedValues = new HashMap<>();

    public MainWindow(List<String> groups, String appTitle, String appVersion) {

        setTitle(appTitle + " " + appVersion);
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
        runButton.addActionListener(event -> CrawlectRunner.runCrawlectCommand(inputMap, storedValues, this));
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
        storedValues = UserSettings.getInstance().loadConfig();

        updateOptionPanel(groupList.getSelectedValue());
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
                // Add field to option mapping.
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
                // Add field to option mapping.
                inputMap.put(option, comboBox);
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
                    // Replace with custom value check from *ClOption* class like `customType == "Path_Selector"`.
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
                    // Replace with custom value check from *ClOption* class like `customType == "File_Selector"`.
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
                    // Ignore this field. Might adapt *Crawlect* in the future and remove the field from -clischem output.
                    continue;
                } else if (option.getPrimaryFlag().equals("--output-suffix")) {
                    // Ignore this field. Might adapt *Crawlect* in the future and remove the field from -clischem output.
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
}