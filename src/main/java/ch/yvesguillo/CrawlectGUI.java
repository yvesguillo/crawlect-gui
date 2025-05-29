package ch.yvesguillo;

import ch.yvesguillo.logic.CliOption;
import ch.yvesguillo.logic.CliSchemaParser;

import java.io.File;

public class CrawlectGUI {
    public static void main(String[] args) throws Exception {
        File file = new File("cli-schema.json");
        CliSchemaParser parser = new CliSchemaParser(file);

        for (String group : parser.getGroups()) {
            System.out.println("### " + group);
            for (CliOption opt : parser.getOptionsForGroup(group)) {
                System.out.printf("- %s (%s)%n", opt.getPrimaryFlag(), opt.help);
            }
        }
    }
}