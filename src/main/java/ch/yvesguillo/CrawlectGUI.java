package ch.yvesguillo;

import ch.yvesguillo.controller.PythonRunner;
import ch.yvesguillo.controller.UserSettings;
import ch.yvesguillo.controller.MainController;
import ch.yvesguillo.model.CliSchemaParser;
import ch.yvesguillo.view.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.formdev.flatlaf.FlatDarkLaf;

public class CrawlectGUI {
    public static String appName;
    public static String appVersion;
    public static MainWindow view;
    public static MainController controler;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            String json = PythonRunner.getCliSchemaJson();
            CliSchemaParser.initialize(json);

            String name = "Crawlect-GUI";
            String version = "DEV";

            try (InputStream stream = CrawlectGUI.class.getResourceAsStream("/version.properties")) {
                if (stream != null) {
                    Properties props = new Properties();
                    props.load(stream);
                    appName = props.getProperty("project.name", name);
                    appVersion = props.getProperty("project.version", version);
                }
            } catch (Exception e) {
                System.err.println("[Init] Could not load version.properties: " + e.getMessage());
            }

            UserSettings.initialize(appName.toLowerCase() + " " + appVersion);

            SwingUtilities.invokeLater(() -> {
                MainWindow.initialize(CliSchemaParser.getInstance().getGroups(), appName, appVersion);
                view = MainWindow.getInstance();
                setAppIcon(view);
                view.setVisible(true);
            });

            MainController.initialize(view);

        } catch (Exception error) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Crawlect is not available on this system.\n" + error.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            });
        }
    }

    private static void setAppIcon(JFrame win) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("mac")) {
                Taskbar.getTaskbar().setIconImage(
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_64-mac.png")).getImage()
                );
            } else {
                List<Image> icons = List.of(
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_16.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_32.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_64.png")).getImage(),
                        new ImageIcon(CrawlectGUI.class.getResource("/icons/crawlect-gui_256.png")).getImage()
                );
                win.setIconImages(icons);

                if (os.contains("linux")) {
                    UIManager.put("Frame.iconImage", icons.get(2));
                }
            }
        } catch (Exception e) {
            System.err.println("[GUI] Could not set icon: " + e.getMessage());
        }
    }
}