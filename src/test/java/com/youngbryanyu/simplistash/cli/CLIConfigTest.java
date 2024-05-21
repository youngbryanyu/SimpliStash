package com.youngbryanyu.simplistash.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.cli.CommandLineParser;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Tests for the spring IoC/DI config class used by the CLI.
 */
@SpringJUnitConfig(CLIConfig.class)
public class CLIConfigTest {
     /**
     * The application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Test getting the terminal bean.
     */
    @Test
    public void testTerminalBean() {
        Terminal terminal = context.getBean(Terminal.class);
        assertTrue(terminal instanceof Terminal);
    }

    /**
     * Test getting the line reader bean.
     */
    @Test
    public void testLineReaderBean() {
        LineReader lineReader = context.getBean(LineReader.class);
        assertTrue(lineReader instanceof LineReader);
    }

     /**
     * Test getting the command line parser.
     */
    @Test
    public void testCommandLineParser() {
        CommandLineParser parser = context.getBean(CommandLineParser.class);
        assertTrue(parser instanceof CommandLineParser);
    }
}
