package ch.yvesguillo.logic;

import ch.yvesguillo.gui.ShowMessages;
import ch.yvesguillo.gui.ComboItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.*;
import java.awt.*;

public class CrawlectRunner {

    // Private constructor to prevent instantiation.
    private CrawlectRunner() {
        throw new UnsupportedOperationException("CrawlectRunner is an utility class");
    }

    public static void captureCurrentInputs(Map<CliOption, JComponent> inputMap, Map<CliOption, Object> storedValues) {
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
    }

    public static boolean validateInputs(Map<CliOption, JComponent> inputMap, Map<CliOption, Object> storedValues, JFrame win) {
        for (Map.Entry<CliOption, JComponent> entry : inputMap.entrySet()) {
            CliOption option = entry.getKey();
            Object value = storedValues.get(option);

            // Mandatory: --path
            if (option.getPrimaryFlag().equals("--path")) {
                String path = (value instanceof String s) ? s.trim() : "";
                if (path.isEmpty()) {
                    ShowMessages.showValidationError("The '--path' field is required.", win);
                    return false;
                }
            }

            // Mandatory: --output
            if (option.getPrimaryFlag().equals("--output")) {
                String output = (value instanceof String s) ? s.trim() : "";
                if (output.isEmpty()) {
                    ShowMessages.showValidationError("The '--output' field is required.", win);
                    return false;
                }
            }

            // Integer: --depth
            if (option.getPrimaryFlag().equals("--depth")) {
                String str = (value instanceof String s) ? s.trim() : "";
                if (!str.isEmpty()) {
                    try {
                        Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        ShowMessages.showValidationError("The '--depth' field must be a valid integer.", win);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static String handleOutputFileOverwrite(List<String> command, JFrame win) {
        for (int i = 0; i < command.size(); i++) {
            String flag = command.get(i);
            if (flag.equals("-o") || flag.equals("--output")) {
                if (i + 1 < command.size()) {
                    String outputPath = command.get(i + 1);
                    java.io.File outputFile = new java.io.File(outputPath);

                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && (!parentDir.exists() || !parentDir.canWrite())) {
                        JOptionPane.showMessageDialog(win, "Cannot write to the output directory:\n" + parentDir.getAbsolutePath(), "Output Path Error", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }

                    if (outputFile.exists()) {
                        int choice = JOptionPane.showOptionDialog(win,
                                "The file '" + outputFile.getName() + "' already exists.\nWhat would you like to do?",
                                "Output File Exists",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                new String[]{"Change file", "Overwrite", "Cancel"},
                                "Change file");

                        if (choice == 0) {
                            // Change file.
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setSelectedFile(outputFile);
                            int result = fileChooser.showSaveDialog(win);
                            if (result == JFileChooser.APPROVE_OPTION) {
                                String newPath = fileChooser.getSelectedFile().getAbsolutePath();
                                command.set(i + 1, newPath);
                                return newPath;
                            } else {
                                // Cancelled.
                                return null;
                            }
                        } else if (choice == 1) {
                            // Overwrite.
                            if (!outputFile.delete()) {
                                JOptionPane.showMessageDialog(win, "Failed to delete the existing output file.\nPlease try changing the file name.", "File Deletion Error", JOptionPane.ERROR_MESSAGE);
                                return null;
                            }
                            return outputPath;
                        } else {
                            // Cancel.
                            return null;
                        }
                    }
                }
            }
        }
        // No output file set or file doesn't exist.
        return "ok";
    }

    public static void runCrawlectCommand(Map<CliOption, JComponent> inputMap, Map<CliOption, Object> storedValues, JFrame win) {
        // store visible inputs before collecting args.
        captureCurrentInputs(inputMap, storedValues);

        if (!validateInputs(inputMap, storedValues, win)) {
            // Stop if validation fails.
            return;
        }

        List<String> args = new ArrayList<>();

        List<CliOption> allOptions = CliSchemaParser.getInstance().getAllOptions();

        for (CliOption option : allOptions) {
            String flag = option.getPrimaryFlag();
            Object value = storedValues.get(option);

            System.out.println("[Arg] got " + flag + " = " + value);

            if (option.isBoolean) {
                if (Boolean.TRUE.equals(value) && !Objects.equals(option.defaultValue, "True")) {
                    // Add positive flag.
                    args.add(flag);
                } else if (Boolean.FALSE.equals(value) && !Objects.equals(option.defaultValue, "False")) {
                    String negativeFlag = option.getNegativeFlag();
                    if (negativeFlag != null) {
                        // Add --no-flag form.
                        args.add(negativeFlag);
                    }
                }
            } else if (option.hasChoices) {
                if (value instanceof String strVal && !strVal.isEmpty()) {
                    args.add(flag);
                    args.add(strVal);
                }
                // else: skip empty.
            } else {
                if (value instanceof String strVal) {
                    strVal = strVal.trim();
                    if (!strVal.isEmpty()) {
                        args.add(flag);
                        args.add(strVal);
                    }
                }
            }
        }

        try {

            String outputCheck = handleOutputFileOverwrite(args, win);
            if (outputCheck == null) {
                return; // User cancelled.
            }

            // Check if path exists (for --path or -p)
            for (int i = 0; i < args.size(); i++) {
                String flag = args.get(i);
                if ((flag.equals("--path") || flag.equals("-p")) && i + 1 < args.size()) {
                    String pathStr = args.get(i + 1);
                    java.io.File path = new java.io.File(pathStr);
                    if (!path.exists() || !path.isDirectory()) {
                        JOptionPane.showMessageDialog(win, "The selected path to scan does not exist or is not a directory:\n" + pathStr, "Invalid Path to Scan", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String output = PythonRunner.runCrawlect(args);
            JTextArea textArea = new JTextArea(output);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(480, 270));

            JOptionPane.showMessageDialog(win, scrollPane, "Crawlect finished", JOptionPane.INFORMATION_MESSAGE);

            // Save current settings.
            UserSettings.getInstance().saveConfig(storedValues);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(win, "Error running Crawlect: " + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}