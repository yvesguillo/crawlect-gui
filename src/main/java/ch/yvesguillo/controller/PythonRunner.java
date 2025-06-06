package ch.yvesguillo.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for executing Python-related tasks,
 * such as locating the Python executable, running the Crawlect CLI,
 * and fetching the CLI schema in JSON format.
 *
 * Automatically caches the first valid Python command detected.
 */
public final class PythonRunner {

    private static String pythonCommand = null;

    // Private constructor to prevent instantiation.
    private PythonRunner() {
        throw new UnsupportedOperationException("PythonRunner is an utility class");
    }

    /**
     * Returns the path or command name of the first working Python interpreter.
     *
     * @return the working Python command (e.g., "python3").
     * @throws RuntimeException if no Python interpreter is found.
     */
    public static String getPythonCommand() throws Exception {
        if (pythonCommand != null) {
            return pythonCommand;
        }

        List<String> candidates = Arrays.asList("py", "python", "python3");

        for (String cmd : candidates) {
            try {
                Process process = new ProcessBuilder(cmd, "--version").start();
                if (process.waitFor() == 0) {
                    pythonCommand = cmd;
                    System.out.println("[Run] Python is: " + cmd);
                    return cmd;
                }
            } catch (Exception e) {
                System.out.println("[Run] Python is not " + cmd);
            }
        }

        throw new RuntimeException("Could not locate Python. Make sure it is installed and accessible.");
    }

    /**
     * Fetches the CLI schema from the Crawlect module.
     *
     * @return JSON string representing the CLI schema.
     * @throws RuntimeException if Crawlect is not installed or execution fails.
     */
    public static String getCliSchemaJson() throws Exception {
        Process process = new ProcessBuilder(getPythonCommand(), "-m", "crawlect", "-clischem").start();
        String output = readProcessOutput(process);

        if (process.waitFor() == 0 && !output.isBlank()) {
            return output;
        }

        throw new RuntimeException("Crawlect CLI schema could not be fetched. Is Crawlect installed?");
    }

    /**
     * Runs the Crawlect module with the given arguments.
     *
     * @param args list of CLI arguments (excluding the "python -m crawlect" prefix).
     * @return stdout output as a string.
     * @throws RuntimeException if Crawlect exits with an error.
     */
    public static String runCrawlect(List<String> args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(getPythonCommand());
        command.add("-m");
        command.add("crawlect");
        command.addAll(args);

        System.out.println("[Run] Executing: " + String.join(" ", command));

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = readProcessOutput(process);

        if (process.waitFor() != 0) {
            throw new RuntimeException("Crawlect exited with code " + process.exitValue());
        }

        return output;
    }

    /**
     * Reads the full stdout of a process and returns it as a String.
     *
     * @param process the running process.
     * @return process output as a String.
     * @throws Exception if an I/O error occurs.
     */
    private static String readProcessOutput(Process process) throws Exception {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}