package ch.yvesguillo.view;

import ch.yvesguillo.controller.MainController;
import ch.yvesguillo.controller.UserSettings;
import ch.yvesguillo.model.CliOption;
import ch.yvesguillo.model.CliSchemaParser;
import ch.yvesguillo.model.ComboItem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame {

    private static MainWindow instance;

    private final JList<String> groupList;
    private final DefaultListModel<String> groupListModel;
    private final JPanel optionPanel;

    private static final Font mainFont = UIManager.getFont("Label.font").deriveFont(12f);
    private static final Font heavyFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f);

    public static final Map<CliOption, JComponent> inputMap = new HashMap<>();
    public static Map<CliOption, Object> storedValues = new HashMap<>();

    private final MainController controller;

    public MainWindow(List<String> groups, String appTitle, String appVersion) throws IOException {

        controller = MainController.getInstance();

        setTitle(appTitle + " " + appVersion);
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(groupScroll, BorderLayout.CENTER);

        JButton runButton = new JButton("Run Crawlect ▶");
        runButton.setFont(heavyFont);
        runButton.setPreferredSize(new Dimension(250, 40));
        runButton.addActionListener(e -> controller.runnRequest());
        leftPanel.add(runButton, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(optionPanel, BorderLayout.NORTH);
        JScrollPane optionScroll = new JScrollPane(wrapper);
        add(optionScroll, BorderLayout.CENTER);

        storedValues = UserSettings.getInstance().loadConfig();

        updateOptionPanel(groupList.getSelectedValue());
    }

    public static synchronized void initialize(List<String> groups, String appTitle, String appVersion) {
        if (instance != null) {
            throw new IllegalStateException("MainWindow has already been initialized.");
        }
        try {
            instance = new MainWindow(groups, appTitle, appVersion);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize MainWindow: " + e.getMessage(), e);
        }
    }

    public static synchronized MainWindow lazyGetInstance(List<String> groups, String appTitle, String appVersion) {
        if (instance == null) {
            try {
                instance = new MainWindow(groups, appTitle, appVersion);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to lazily initialize MainWindow: " + e.getMessage(), e);
            }
        }
        return instance;
    }

    public static MainWindow getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MainWindow has not been initialized.");
        }
        return instance;
    }

    private void updateOptionPanel(String group) {
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
            JLabel label = new JLabel(String.format("%-20s", metalabel));
            label.setPreferredSize(new Dimension(160, 25));
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
                    comboBox.addItem(new ComboItem("(none)", ""));
                }

                for (String choice : option.choices) {
                    comboBox.addItem(new ComboItem(choice, choice));
                }

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
                inputMap.put(option, comboBox);
                inputField = comboBox;
            } else {
                JTextField textField = new JTextField(20);
                String saved = (String) storedValues.get(option);
                if (saved != null) textField.setText(saved);

                JComponent displayComponent;

                if (option.getPrimaryFlag().equals("--path")) {
                    JButton browse = new JButton("📁");
                    browse.setMargin(new Insets(2, 4, 2, 4));
                    browse.setToolTipText("Select folder to scan");
                    browse.addActionListener(e -> controller.pathModifRequest(textField));

                    JPanel fileInputPanel = new JPanel(new BorderLayout());
                    fileInputPanel.add(textField, BorderLayout.CENTER);
                    fileInputPanel.add(browse, BorderLayout.EAST);
                    displayComponent = fileInputPanel;

                } else if (option.getPrimaryFlag().equals("--output")) {
                    JButton browse = new JButton("📁");
                    browse.setMargin(new Insets(2, 4, 2, 4));
                    browse.setToolTipText("Select output file path");
                    browse.addActionListener(e -> controller.pathModifRequest(textField));

                    JPanel fileInputPanel = new JPanel(new BorderLayout());
                    fileInputPanel.add(textField, BorderLayout.CENTER);
                    fileInputPanel.add(browse, BorderLayout.EAST);
                    displayComponent = fileInputPanel;

                } else if (option.getPrimaryFlag().equals("--output-prefix")) {
                    continue;
                } else if (option.getPrimaryFlag().equals("--output-suffix")) {
                    continue;
                } else {
                    displayComponent = textField;
                }
                inputMap.put(option, textField);

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