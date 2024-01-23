package com.curehunter;

import com.curehunter.picocli.PropertiesFileVersionProvider;
import com.curehunter.transform.TransformAll;

import picocli.CommandLine;

@CommandLine.Command(name = "app", mixinStandardHelpOptions = true, versionProvider = PropertiesFileVersionProvider.class, subcommands = {
        ListFilesCommand.class, TransformAll.class })
public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
