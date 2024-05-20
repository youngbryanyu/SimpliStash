package com.youngbryanyu.simplistash.cli;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Dependency injection configuration class for the CLI.
 */
@Configuration
@ComponentScan(basePackages = "com.youngbryanyu.simplistash.cli")
public class CLIConfig {
    /**
     * Returns a singleton instance of the terminal.
     * 
     * @return A singleton instance of the terminal.
     * @throws IOException If an IOException is thrown while initializing.
     */
    @Bean
    public Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .system(true)
                .build();
    }

    /**
     * Returns a singleton instance of the line reader.
     * 
     * @return A singleton instance of the line reader.
     * @throws IOException If an IOException is thrown while initializing.
     */
    @Bean
    public LineReader lineReader(Terminal terminal) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .history(new DefaultHistory())
                .build();
    }
}
