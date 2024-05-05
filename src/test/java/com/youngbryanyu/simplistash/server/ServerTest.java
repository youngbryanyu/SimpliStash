package com.youngbryanyu.simplistash.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

/**
 * Unit tests for {@link Server}.
 */
public class ServerTest {
    /**
     * The mocked server handler.
     */
    ClientHandler clientHandler;


    /**
     * Setup before each test runs.
     */
    @BeforeEach
    public void beforeEachSetup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test running the server startup script through the main method.
     */
    @Test
    public void test1() throws IOException {
    }

  
}
