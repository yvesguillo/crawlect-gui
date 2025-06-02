package ch.yvesguillo.logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PythonRunner {

    private static String pythonCommand = null;

    public static String getPythonCommand() throws Exception {
        if (pythonCommand != null) {
            return pythonCommand; // Use cached if exist.
        }

        List<String> commands = Arrays.asList("py", "python", "python3");

        for (String cmd : commands) {
            try {
                Process process = new ProcessBuilder(cmd, "--version").start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    // If this line returns without throwing, the command exists.
                    pythonCommand = cmd;
                    return cmd;
                }
            } catch (Exception ignored) {
                // Pass.
            }
        }

        throw new RuntimeException("Could not locate Python. Make sure it is installed and accessible.");
    }

    public static String getCliSchemaJson() throws Exception {
        Process process = new ProcessBuilder(getPythonCommand(), "-m", "crawlect", "-clischem").start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && output.length() > 0) {
                return output.toString();
            }
        }

        throw new RuntimeException("Crawlect CLI schema could not be fetched. Is it installed?");
    }

    public static String runCrawlect(List<String> args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(getPythonCommand());
        command.add("-m");
        command.add("crawlect");
        command.addAll(args);

        // Debbug.
        System.out.println("Executing: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Crawlect exited with code " + exitCode);
        }

        return output.toString();
    }
}