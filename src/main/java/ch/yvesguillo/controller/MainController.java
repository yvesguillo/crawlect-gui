package ch.yvesguillo.controller;

import java.io.IOException;
import javax.swing.*;

import ch.yvesguillo.view.MainWindow;

/**
 * Singleton controller class responsible for handling user actions 
 * and coordinating between the GUI (MainWindow) and backend logic (e.g., CrawlectRunner).
 *
 * Acts as the main entry point for control flow from the view to the core logic.
 */
public class MainController {

    private static MainController instance;

    // Reference to the main view
    private static MainWindow view;

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param view The main application window.
     * @throws IOException if initialization fails.
     */
    private MainController(MainWindow view) throws IOException {
        this.view = view;
    }

    /**
     * Explicitly initializes the singleton controller with the given view.
     * 
     * @param view The main GUI window.
     * @throws IllegalStateException if already initialized.
     */
    public static synchronized void initialize(MainWindow view) {
        if (instance != null) {
            throw new IllegalStateException("MainController has already been initialized.");
        }
        try {
            instance = new MainController(view);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize MainController: " + e.getMessage(), e);
        }
    }

    /**
     * Lazily initializes the controller if not already done, otherwise returns the existing instance.
     * 
     * @param view The main GUI window.
     * @return Singleton instance of MainController.
     */
    public static synchronized MainController lazyGetInstance(MainWindow view) {
        if (instance == null) {
            try {
                instance = new MainController(view);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to lazily initialize MainController: " + e.getMessage(), e);
            }
        }
        return instance;
    }

    /**
     * Returns the already initialized singleton instance.
     *
     * @return Singleton instance of MainController.
     * @throws IllegalStateException if the controller has not been initialized yet.
     */
    public static MainController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MainController has not been initialized.");
        }
        return instance;
    }

    /**
     * Handles the "Run Crawlect" button click.
     * Delegates the task of building CLI args, validation, execution, and result display.
     */
    public void runnRequest() {
        CrawlectRunner.runCrawlectCommand(view.inputMap, view.storedValues, view);
        System.out.println("[Control] Scan requested");
    }

    /**
     * Opens a directory chooser to allow the user to select a path (for the --path input).
     * Updates the given text field with the selected path.
     *
     * @param field JTextField to be updated with the selected directory path.
     */
    public void pathModifRequest(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(view);

        if (result == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }

        System.out.println("[Control] Path modification requested");
    }
}