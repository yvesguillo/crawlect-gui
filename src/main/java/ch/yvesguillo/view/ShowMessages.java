package ch.yvesguillo.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Utility class for displaying standardized message dialogs in the Crawlect GUI.
 * 
 * Centralizes all user-facing dialog logic to keep the UI consistent and maintainable.
 */
public final class ShowMessages {

    /**
     * Private constructor to prevent instantiation.
     * This class is intended to be used statically only.
     */
    private ShowMessages() {
        throw new UnsupportedOperationException("ShowMessages is a utility class");
    }

    /**
     * Displays a standard validation error message dialog.
     *
     * @param message The error message to display.
     * @param win The parent JFrame that owns the dialog.
     */
    public static void showValidationError(String message, JFrame win) {
        JOptionPane.showMessageDialog(
                win,
                message,
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
        );
    }
}