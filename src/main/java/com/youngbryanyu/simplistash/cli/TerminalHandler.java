package com.youngbryanyu.simplistash.cli;

import java.io.IOException;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The terminal handler for the CLI.
 */
@Component
public class TerminalHandler {
    /**
     * The line reader.
     */
    private LineReader lineReader;

    /**
     * The constructor.
     * 
     * @throws IOException If an I/O exception occurs while creating the terminal.
     */
    @Autowired
    public TerminalHandler(LineReader lineReader) throws IOException {
        this.lineReader = lineReader;
    }

    /**
     * Prints a prompt and reads a line from the CLI's terminal.
     * 
     * @param prompt The prompt to display to the user.
     * @return Return the line read, or the exit command if ^C or ^D.
     */
    public String readLine(String prompt) {
        try {
            return lineReader.readLine(prompt);
        } catch (UserInterruptException e) {
            return ""; /* Indicate to CLI to simply print a new line */
        }
        catch (EndOfFileException e) {
            return CLI.EXIT;
        }
    }
}
