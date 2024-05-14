package com.youngbryanyu.simplistash.cli.commands.write;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cli.commands.CLICommand;
import com.youngbryanyu.simplistash.commands.write.DropCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The DROP command used in the CLI.
 */
@Component
public class CLIDropCommand implements CLICommand {
    /**
     * The command's name.
     */
    public static final String NAME = DropCommand.NAME;
    /**
     * The usage of the CLI command.
     */
    public static final String USAGE = "drop <name>";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * The constructor.
     */
    @Autowired
    public CLIDropCommand() {
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
        String name = args.get(1);

        /* Encode to protocol */
        return ProtocolUtil.encode(NAME, List.of(name), false, Collections.emptyMap());
    }

    /**
     * Returns the options (optional args) for the command.
     * 
     * @return The options object.
     */
    public Options getOptions() {
        return new Options();
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
