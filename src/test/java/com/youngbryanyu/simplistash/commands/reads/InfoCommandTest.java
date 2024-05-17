package com.youngbryanyu.simplistash.commands.reads;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.commands.Command;
import com.youngbryanyu.simplistash.commands.read.InfoCommand;
import com.youngbryanyu.simplistash.commands.read.PingCommand;
import com.youngbryanyu.simplistash.stash.Stash;
import com.youngbryanyu.simplistash.stash.StashManager;

/**
 * Unit tests for the INFO command.
 */
public class InfoCommandTest {
      /**
     * The mock stash manager.
     */
    @Mock
    StashManager mockStashManager;
    /**
     * The INFO command under test.
     */
    private Command command;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        command = new InfoCommand(mockStashManager);
    }
    /**
     * Test the get name method.
     */
    @Test
    public void testGetName() {
        assertEquals("INFO", command.getName());
    }
}
