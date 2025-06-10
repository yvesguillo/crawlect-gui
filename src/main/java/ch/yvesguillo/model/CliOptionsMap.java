package ch.yvesguillo.model;

import java.io.IOException;
import java.util.List;

public final class CliOptionsMap {

    private static CliOptionsMap instance;

    private CliOptionsMap(List<String> groups) throws IOException {
        //Pass
    }

    /**
     * Explicitly initializes the singleton. Fails if already initialized.
     */
    public static synchronized void initialize(List<String> groups) {
        if (instance != null) {
            throw new IllegalStateException("CliOptionMap has already been initialized.");
        }
        try {
            instance = new CliOptionsMap(groups);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize CliOptionMap: " + e.getMessage(), e);
        }
    }

    /**
     * Lazily returns the singleton, initializing it if necessary.
     */
    public static synchronized CliOptionsMap lazyGetInstance(List<String> groups) {
        if (instance == null) {
            try {
                instance = new CliOptionsMap(groups);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to lazily initialize CliOptionMap: " + e.getMessage(), e);
            }
        }
        return instance;
    }

    /**
     * Returns the singleton, assuming it has already been initialized.
     */
    public static CliOptionsMap getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CliOptionMap has not been initialized.");
        }
        return instance;
    }
}