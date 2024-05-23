package com.youngbryanyu.simplistash.cli.commands.write;

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
import com.youngbryanyu.simplistash.commands.write.CreateCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The CREATE command used in the CLI.
 */
@Component
public class CLICreateCommand implements CLICommand {
    /**
     * The command's name.
     */
    public static final String NAME = CreateCommand.NAME;
    /**
     * The usage of the CLI command.
     */
    public static final String USAGE = "create <name> [-off-heap <true/false>]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * The constructor.
     */
    @Autowired
    public CLICreateCommand() {
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

        /* Get name */
        String name = args.get(1);

        /* Get optional args and creating arg to val mapping */
        Map<String, String> optArgMap = new HashMap<>();
        for (CreateCommand.OptionalArg optArg : CreateCommand.OptionalArg.values()) {
             /* Convert to lower case and replace _ with - */
            String optArgName = optArg.name().toLowerCase().replace("_", "-");

            if (commandLine.hasOption(optArgName)) {
                optArgMap.put(optArgName, commandLine.getOptionValue(optArgName));
            }
        }
        /* Encode to protocol */
        return ProtocolUtil.encode(NAME, List.of(name), true, optArgMap);
    }

    /**
     * Returns the options (optional args) for the command.
     * 
     * @return The options object.
     */
    public Options getOptions() {
        Options options = new Options();

        for (CreateCommand.OptionalArg optArg : CreateCommand.OptionalArg.values()) {
            options.addOption(Option.builder()
                    .longOpt(optArg.name().toLowerCase().replace("_", "-"))
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
