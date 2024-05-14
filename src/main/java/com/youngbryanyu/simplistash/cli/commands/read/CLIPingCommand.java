package com.youngbryanyu.simplistash.cli.commands.read;

import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.youngbryanyu.simplistash.cli.commands.CLICommand;
import com.youngbryanyu.simplistash.commands.read.PingCommand;
import com.youngbryanyu.simplistash.protocol.ProtocolUtil;

/**
 * The PING command used in the CLI.
 */
@Component
public class CLIPingCommand implements CLICommand {
    /**
     * The command's name.
     */
    public static final String NAME = PingCommand.NAME;
    /**
     * The usage of the CLI command.
     */
    public static final String USAGE = "ping";
    /**
     * The minimum number of required arguments.
     */
    private final int minRequiredArgs;

    /**
     * The constructor.
     */
    @Autowired
    public CLIPingCommand() {
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

        /* Encode to protocol */
        return ProtocolUtil.encode(NAME, Collections.emptyList(), false, Collections.emptyMap());
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
     * @return The command's usage.
     */
    public String getUsage() {
        return USAGE;
    }
}
