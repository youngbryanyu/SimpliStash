package com.youngbryanyu.simplistash.server.client;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import com.youngbryanyu.simplistash.config.AppConfig;

/**
 * Unit tests for the client handler factory.
 */
public class ClientHandlerFactoryTest {
    /**
     * The mock spring application context.
     */
    @Mock
    private ApplicationContext mockContext;
    /**
     * The client handler factory under test.
     */
    private ClientHandlerFactory clientHandlerFactory;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        clientHandlerFactory = new ClientHandlerFactory(mockContext);
    }

    /**
     * Test {@link ClientHandlerFactory#createPrimaryClientHandler()}.
     */
    @Test
    void testCreatePrimaryClientHandler() {
        /* Setup */
        ClientHandler expectedHandler = mock(ClientHandler.class);
        when(mockContext.getBean(AppConfig.PRIMARY_CLIENT_HANDLER, ClientHandler.class)).thenReturn(expectedHandler);

        /* Call method */
        ClientHandler actualHandler = clientHandlerFactory.createPrimaryClientHandler();

        /* Test assertions */
        assertSame(expectedHandler, actualHandler);
        verify(mockContext).getBean(AppConfig.PRIMARY_CLIENT_HANDLER, ClientHandler.class);
    }

     /**
     * Test {@link ClientHandlerFactory#createReadOnlyClientHandler()}.
     */
    @Test
    void testCreateReadOnlyClientHandler() {
        /* Setup */
        ClientHandler expectedHandler = mock(ClientHandler.class);
        when(mockContext.getBean(AppConfig.READ_ONLY_CLIENT_HANDLER, ClientHandler.class)).thenReturn(expectedHandler);

        /* Call method */
        ClientHandler actualHandler = clientHandlerFactory.createReadOnlyClientHandler();

        /* Test assertions */
        assertSame(expectedHandler, actualHandler);
        verify(mockContext).getBean(AppConfig.READ_ONLY_CLIENT_HANDLER, ClientHandler.class);
    }
}
