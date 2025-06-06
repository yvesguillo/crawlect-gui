package ch.yvesguillo.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ShowMessages {

    // Private constructor to prevent instantiation.
    private ShowMessages() {
        throw new UnsupportedOperationException("ShowMessages is an utility class");
    }

    public static void showValidationError(String message, JFrame win) {
        JOptionPane.showMessageDialog(win,
                message,
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
    }
}