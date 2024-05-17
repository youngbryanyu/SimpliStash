package com.youngbryanyu.simplistash.cli.commands.read;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cli.commands.CLICommand;
import com.youngbryanyu.simplistash.commands.read.InfoCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The INFO command used in the CLI.
 */
@Component
public class CLIInfoCommand implements CLICommand {
    /**
     * The command's name.
     */
    public static final String NAME = InfoCommand.NAME;
    /**
     * The usage of the CLI command.
     */
    public static final String USAGE = "INFO [-name <name>]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * The constructor.
     */
    @Autowired
    public CLIInfoCommand() {
        minRequiredArgs = ProtocolUtil.getMinRequiredArgs(USAGE);
    }

    /**
     * Encodes the CLI command into the server protocol.
     */
    public String encodeCLICommand(CommandLine commandLine) {
        /* Get required arguments */
        List<String> args = commandLine.getArgList();

        /* Check if there's enough arguments */
        if (args.size() < minRequiredArgs) {
            return null;
        }

        /* Get optional args and creating arg to val mapping */
        Map<String, String> optArgMap = new HashMap<>();
        for (InfoCommand.OptionalArg optArg : InfoCommand.OptionalArg.values()) {
            String optArgName = optArg.name().toLowerCase(); /* Convert to lower case */
            if (commandLine.hasOption(optArgName)) {
                optArgMap.put(optArgName, commandLine.getOptionValue(optArgName));
            }
        }

        /* Encode to protocol */
        return ProtocolUtil.encode(NAME, Collections.emptyList(), true, optArgMap);
    }

    /**
     * Returns the options (optional args) for the command.
     * 
     * @return The options object.
     */
    public Options getOptions() {
        Options options = new Options();

        for (InfoCommand.OptionalArg optArg : InfoCommand.OptionalArg.values()) {
            options.addOption(Option.builder()
                    .longOpt(optArg.name().toLowerCase())
                    .hasArg()
                    .build());
        }

        return options;
    }

    /**
     * Returns the command's name.
     * 
     * @return The command name
     */
    public String getName() {
        return NAME;
    }

    /**
     * Returns the command's usage.
     * 
     * @return The command's usage.
     */
    public String getUsage() {
        return USAGE;
    }
}