package com.youngbryanyu.simplistash;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youngbryanyu.simplistash.server.ReadOnlyServer;
import com.youngbryanyu.simplistash.server.ServerMonitor;
import com.youngbryanyu.simplistash.server.PrimaryServer;

/**
 * Unit tests for the main class.
 */
public class MainTest {
    /**
     * The mocked Spring application context.
     */
    @Mock
    private AnnotationConfigApplicationContext context;
    /**
     * The mocked primary server.
     */
    @Mock
    private PrimaryServer primaryServer;
    /**
     * The mocked read-only server.
     */
    @Mock
    private ReadOnlyServer readOnlyServer;
    /**
     * The mocked server monitor.
     */
    @Mock
    private ServerMonitor serverMonitor;
    /**
     * The mocked logger.
     */
    @Mock
    private Logger mockLogger;

    /**
     * Setup before each test.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(context.getBean(PrimaryServer.class)).thenReturn(primaryServer);
        when(context.getBean(ReadOnlyServer.class)).thenReturn(readOnlyServer);
        when(context.getBean(ServerMonitor.class)).thenReturn(serverMonitor);
        when(context.getBean(Logger.class)).thenReturn(mockLogger);
    }

    /**
     * Test {@link Main#main}.
     */
    @Test
    public void testMain() throws Exception {
        try (MockedStatic<Main> mockMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            /* Setup */
            mockMain.when(() -> Main.start(any(), any(), any(), any()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            /* Call method */
            Main.main(new String[] {});

            /* Check assertions */
            mockMain.verify(() -> Main.start(any(), any(), any(), any()), Mockito.times(1));
        }
    }

    /**
     * Test {@link Main#start} when it's successfully run.
     */
    @Test
    public void testStart_successful() throws Exception {
        /* Setup */
        doNothing().when(primaryServer).start();
        doNothing().when(readOnlyServer).start();

        /* Call method */
        Main.start(primaryServer, readOnlyServer, serverMonitor, mockLogger);

        /* Check assertions */
        verify(serverMonitor, times(1)).waitForCrash();
    }

    /**
     * Test {@link Main#start} when the primary server crashes.
     */
    @Test
    public void testStart_primaryServerCrash() throws Exception {
        /* Setup */
        doThrow(new RuntimeException("Primary server crashed")).when(primaryServer).start();
        doNothing().when(readOnlyServer).start();

        /* Call method */
        Main.start(primaryServer, readOnlyServer, serverMonitor, mockLogger);

        /* Check assertions */
        verify(serverMonitor, times(1)).waitForCrash();
    }

    /**
     * Test {@link Main#start} when the read-only server crashes.
     */
    @Test
    public void testStart_readOnlyServerCrash() throws Exception {
        /* Setup */
        doNothing().when(primaryServer).start();
        doThrow(new RuntimeException("Read-only server crashed")).when(readOnlyServer).start();

        /* Call method */
        Main.start(primaryServer, readOnlyServer, serverMonitor, mockLogger);

        /* Check assertions */
        verify(serverMonitor, times(1)).waitForCrash();
    }

    /**
     * Test {@link Main#start} when the server monitor thread is interrupted.
     */
    @Test
    public void testStart_waitForCrash_interruptedException() throws Exception {
        /* Setup */
        doNothing().when(primaryServer).start();
        doNothing().when(readOnlyServer).start();
        doThrow(new InterruptedException()).when(serverMonitor).waitForCrash();

        /* Call method */
        Main.start(primaryServer, readOnlyServer, serverMonitor, mockLogger);

        /* Check assertions */
        verify(serverMonitor, times(1)).waitForCrash();
    }

    /**
     * Test initializing the main class.
     */
    @Test
    public void testConstructor() throws Exception {
        Main main = new Main();
        assertNotNull(main);
    }
}
