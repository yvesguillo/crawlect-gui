package ch.yvesguillo.controller;

import ch.yvesguillo.view.MainWindow;

import javax.swing.*;
import java.util.Map;

public class MainController {

    private final MainWindow view;

    public MainController(MainWindow view) {
        this.view = view;
    }

    public void onRunButtonPressed(Map<CliOption, JComponent> inputMap, Map<CliOption, Object> storedValues) {
        CrawlectRunner.runCrawlectCommand(inputMap, storedValues, view);
    }
}