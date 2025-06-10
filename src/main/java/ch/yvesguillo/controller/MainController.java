package ch.yvesguillo.controller;

import java.io.IOException;

import ch.yvesguillo.view.MainWindow;

public class MainController {
    
    private static MainController instance;

    private MainController(MainWindow view) throws IOException {
        // Pass
    }

    /**
     * Explicitly initializes the SingletonClass. Fails if already initialized.
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
     * Lazily returns the SingletonClass, initializing it if necessary.
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
     * Returns the SingletonClass, assuming it has already been initialized.
     */
    public static MainController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MainController has not been initialized.");
        }
        return instance;
    }

    public void runnRequest() {
        // CrawlectRunner.runCrawlectCommand(inputMap, storedValues, view);
        System.out.println("[Control] Scan requested");
    }

    public void scanPathModifRequest() {
        /*JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            //textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }*/
        System.out.println("[Control] Scan path modification requested");
    }

    public void outputPathModifRequest() {
        /*JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose output file (it will be created)");
        String filename = "";
        // Use default value as filename if exist.
        if (option.defaultValue != null) filename = option.defaultValue;
        chooser.setSelectedFile(new File(filename));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }*/
        System.out.println("[Control] Output path modification requested");
    }

}