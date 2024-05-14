package com.youngbryanyu.simplistash.cli.commands.write;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cli.commands.CLICommand;
import com.youngbryanyu.simplistash.commands.write.SetCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The SET command used in the CLI.
 */
@Component
public class CLISetCommand implements CLICommand {
    /**
     * The command's name.
     */
    public static final String NAME = SetCommand.NAME;
    /**
     * The usage of the CLI command.
     */
    public static final String USAGE = "set <key> <value> [-name <name>] [-ttl <ttl>]";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * The constructor.
     */
    @Autowired
    public CLISetCommand() {
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

        /* Get key and value */
        String key = args.get(1);
        String value = args.get(2);

        /* Get optional args and creating arg to val mapping */
        Map<String, String> optArgMap = new HashMap<>();
        for (SetCommand.OptionalArg optArg : SetCommand.OptionalArg.values()) {
            String optArgName = optArg.name().toLowerCase(); /* Convert to lower case */
            if (commandLine.hasOption(optArgName)) {
                optArgMap.put(optArgName, commandLine.getOptionValue(optArgName));
            }
        }

        /* Encode to protocol */
        return ProtocolUtil.encode(NAME, List.of(key, value), true, optArgMap);
    }

    /**
     * Returns the options (optional args) for the command.
     * 
     * @return The options object.
     */
    public Options getOptions() {
        Options options = new Options();

        for (SetCommand.OptionalArg optArg : SetCommand.OptionalArg.values()) {
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
