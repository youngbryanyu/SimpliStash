package com.youngbryanyu.simplistash.cli.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Interface for a CLI command.
 */
public interface CLICommand {
    /**
     * Encodes a command from the CLI into the protocol that can be understood by
     * the server.
     * 
     * @param commandLine The CLI.
     */
    public String encodeCLICommand(CommandLine commandLine);

    /**
     * Returns the options (optional args) for the command.
     * 
     * @return The options object.
     */
    public Options getOptions();

    /**
     * Returns the command's name.
     * 
     * @return The command's name.
     */
    public String getName();

    /**
     * Returns the command's usage.
     * 
     * @return The command's usage.
     */
    public String getUsage();
}
