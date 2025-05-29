package ch.yvesguillo.gui;

import ch.yvesguillo.logic.CliOption;
import ch.yvesguillo.logic.CliSchemaParser;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainWindow extends JFrame {

    private final JList<String> groupList;
    private final DefaultListModel<String> groupListModel;
    private final JPanel optionPanel;

    public MainWindow(List<String> groups) {
        setTitle("Crawlect GUI");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout: Left panel for groups, right panel for options
        setLayout(new BorderLayout());

        groupListModel = new DefaultListModel<>();
        groups.forEach(groupListModel::addElement);
        groupList = new JList<>(groupListModel);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setSelectedIndex(0);
        groupList.setFont(new Font("SansSerif", Font.PLAIN, 14));
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
        optionPanel.add(new JLabel("Options for: " + group));
        optionPanel.add(Box.createVerticalStrut(10));
        // Placeholder, real options later
        optionPanel.add(new JLabel("Options will appear here…"));
        optionPanel.revalidate();
        optionPanel.repaint();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new FlatLightLaf());

        File file = new File("cli-schema.json");
        CliSchemaParser parser = new CliSchemaParser(file);

        SwingUtilities.invokeLater(() -> {
            MainWindow win = new MainWindow(parser.getGroups());
            win.setVisible(true);
        });
    }
}