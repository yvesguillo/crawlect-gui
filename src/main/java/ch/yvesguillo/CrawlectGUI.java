package ch.yvesguillo;

import ch.yvesguillo.gui.MainWindow;
import ch.yvesguillo.logic.CliSchemaParser;
import ch.yvesguillo.logic.PythonRunner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

public class CrawlectGUI {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); // <- Here
            String json = PythonRunner.getCliSchemaJson();
            CliSchemaParser.initialize(json);

            SwingUtilities.invokeLater(() -> {
                MainWindow win = new MainWindow(CliSchemaParser.getInstance().getGroups());
                win.setVisible(true);
            });

        } catch (Exception error) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Crawlect is not available on this system.\n" + error.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            });
        }
    }
}