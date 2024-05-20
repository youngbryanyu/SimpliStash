package com.youngbryanyu.simplistash.cli;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

import com.youngbryanyu.simplistash.cli.commands.CLICommandHandler;

/**
 * Unit tests for the CLI.
 */
class CLITest {
    /**
     * The mock CLI command handler.
     */
    @Mock
    private CLICommandHandler mockCLICommandHandler;
    /**
     * The mock CLI client.
     */
    @Mock
    private CLIClient mockCLIClient;
    /**
     * The Terminal handler.
     */
    @Mock
    private TerminalHandler mockTerminalHandler;
    /**
     * The CLI.
     */
    @InjectMocks
    private CLI cli;
    /**
     * Argument captor.
     */
    @Captor
    private ArgumentCaptor<String> captor;
    /**
     * The mocked application context.
     */
    @Mock
    private ApplicationContext mockContext;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test handling user input from the terminal.
     */
    @Test
    void testStartHandlesUserInput() throws IOException {
        /* Simulate user input */
        when(mockTerminalHandler.readLine(anyString()))
                .thenReturn("command1")
                .thenReturn("command2")
                .thenReturn("exit");

        when(mockCLICommandHandler.processCommand("command1")).thenReturn(null);
        when(mockCLICommandHandler.processCommand("command2")).thenReturn("response2");

        /* Call the method to test */
        cli.start("127.0.0.1", "8080");

        /* Verify connection */
        verify(mockCLIClient).connect("127.0.0.1", 8080);

        /* Verify that processCommand was called with the right inputs */
        verify(mockCLICommandHandler).processCommand("command1");
        verify(mockCLICommandHandler).processCommand("command2");

        /* Verify that responses are printed */
        verify(mockTerminalHandler, times(3)).readLine("> ");

        /* Verify CLI client is closed */
        verify(mockCLIClient).close();
    }

    /**
     * Test the exit command.
     */
    @Test
    void testStartHandlesExitCommand() throws IOException {
        /* Simulate user input */
        when(mockTerminalHandler.readLine(anyString())).thenReturn("exit");

        /* Call the method to test */
        cli.start("127.0.0.1", "8080");

        /* Verify the connection */
        verify(mockCLIClient).connect("127.0.0.1", 8080);

        /* Verify that processCommand was never called */
        verify(mockCLICommandHandler, never()).processCommand(anyString());

        /* Verify that CLIClient was closed */
        verify(mockCLIClient).close();
    }

    /**
     * Test handling server disconnect.
     */
    @Test
    void testStartHandlesServerDisconnect() throws IOException {
        /* Simulate user input */
        when(mockTerminalHandler.readLine(anyString()))
                .thenReturn("command")
                .thenReturn("exit");

        when(mockCLICommandHandler.processCommand("command")).thenReturn("exit");

        /* Call the method to test */
        cli.start("127.0.0.1", "8080");

        /* Verify the connection */
        verify(mockCLIClient).connect("127.0.0.1", 8080);

        /* Verify that processCommand was called */
        verify(mockCLICommandHandler).processCommand("command");

        /* Verify that CLIClient was closed */
        verify(mockCLIClient).close();
    }

    /**
     * Test starting the CLI when an IO exception is thrown.
     */
    @Test
    void testStartIOException() throws IOException {
        /* Simulate user input */
        when(mockTerminalHandler.readLine(anyString())).thenReturn("exit");

        /* Simulate an IOException when connecting */
        doThrow(new IOException("Connection failed")).when(mockCLIClient).connect(anyString(), anyInt());

        /* Call the method to test */
        cli.start("127.0.0.1", "8080");

        /* Verify the connection */
        verify(mockCLIClient).connect("127.0.0.1", 8080);

        /* Verify that CLIClient was not closed since connect() threw an exception */
        verify(mockCLIClient, never()).close();
    }

    /**
     * Test the main method.
     */
    @Test
    public void testMain() throws Exception {
        /* Set system properties */
        System.setProperty("ip", "127.0.0.1");
        System.setProperty("port", "8080");

        try (MockedStatic<CLI> mockMain = Mockito.mockStatic(CLI.class, Mockito.CALLS_REAL_METHODS)) {
            /* Setup */
            mockMain.when(() -> CLI.startCLI(any(), any(), any()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            /* Call method */
            CLI.main(new String[] {});

            /* Check assertions */
            mockMain.verify(() -> CLI.startCLI(any(), any(), any()), times(1));
        }

        /* Clear system properties */
        System.clearProperty("ip");
        System.clearProperty("port");
    }

    /**
     * Test the main method when the arg port is missing.
     */
    @Test
    public void testMain_missingPort() throws Exception {
        /* Set system properties, port is missing */
        System.setProperty("ip", "127.0.0.1");

        try (MockedStatic<CLI> mockMain = Mockito.mockStatic(CLI.class, Mockito.CALLS_REAL_METHODS)) {
            /* Setup */
            mockMain.when(() -> CLI.startCLI(any(), any(), any()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            /* Call method */
            CLI.main(new String[] {});

            /* Check assertions */
            mockMain.verify(() -> CLI.startCLI(any(), any(), any()), never());
        }

         /* Set system properties, port is missing */
         System.clearProperty("ip");
    }

     /**
     * Test the main method when both args port and ip are missing.
     */
    @Test
    public void testMain_missingIp() throws Exception {
        try (MockedStatic<CLI> mockMain = Mockito.mockStatic(CLI.class, Mockito.CALLS_REAL_METHODS)) {
            /* Setup */
            mockMain.when(() -> CLI.startCLI(any(), any(), any()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            /* Call method */
            CLI.main(new String[] {});

            /* Check assertions */
            mockMain.verify(() -> CLI.startCLI(any(), any(), any()), never());
        }
    }

    /**
     * Test the static start method inside main.
     */
    @Test
    void testStartCLI() throws IOException {
        /* Setup */
        doThrow(new IOException("test")).when(mockCLIClient).connect(anyString(), anyInt());
        when(mockContext.getBean(CLI.class)).thenReturn(cli);

        /* Call method */
        CLI.startCLI(mockContext, "127.0.0.1", "8080");

        /* Test assertions */
        verify(mockContext).getBean(CLI.class);
    }
}
