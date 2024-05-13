package com.youngbryanyu.simplistash.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.youngbryanyu.simplistash.stash.StashManager;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

/**
 * Unit tests for the key expiration manager.
 */
public class KeyExpirationManagerTest {
    /**
     * The mocked stash manager.
     */
    @Mock
    private StashManager mockStashManager;
    /**
     * The mocked event loop group.
     */
    @Mock
    private EventLoopGroup mockEventLoopGroup;
    /**
     * The mocked scheduled future.
     */
    @Mock
    private ScheduledFuture<?> mockScheduledFuture;
    /**
     * The key expiration manager under test.
     */
    private KeyExpirationManager expirationManager;
    /**
     * The argument captor.
     */
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    /**
     * Setup before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        expirationManager = new KeyExpirationManager(mockStashManager);
        doReturn(mockScheduledFuture).when(mockEventLoopGroup).scheduleWithFixedDelay(runnableCaptor.capture(), anyLong(),
                anyLong(), any(TimeUnit.class));
    }

    /**
     * Test that the expire task runs.
     */
    @Test
    void testExpirationTaskRuns() {
        /* Setup */
        expirationManager.startExpirationTask(mockEventLoopGroup);

        /* Trigger the captured Runnable to simulate the task execution */ 
        runnableCaptor.getValue().run();

        /* Verify expireTTLKeys is called */
        verify(mockStashManager).expireTTLKeys();
    }

    /**
     * Test {@link KeyExpirationManager#startExpirationTask(EventLoopGroup)} when
     * the task isn't scheduled yet.
     */
    @Test
    void testStartExpirationTask_NotAlreadyScheduled() {
        expirationManager.startExpirationTask(mockEventLoopGroup);

        verify(mockEventLoopGroup).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
    }

    /**
     * Test {@link KeyExpirationManager#startExpirationTask(EventLoopGroup)} when
     * the task is already scheduled and not done.
     */
    @Test
    void testStartExpirationTask_AlreadyScheduledAndNotDone() {
        expirationManager.startExpirationTask(mockEventLoopGroup);
        expirationManager.startExpirationTask(mockEventLoopGroup);

        verify(mockEventLoopGroup, times(1)).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
    }

    /**
     * Test {@link KeyExpirationManager#startExpirationTask(EventLoopGroup)} when
     * the task isn't scheduled yet but it's already done.
     */
    @Test
    void testStartExpirationTask_AlreadyScheduledAndDone() {
        /* Setup */
        when(mockScheduledFuture.isDone()).thenReturn(true);

        /* Start stop then start task */
        expirationManager.startExpirationTask(mockEventLoopGroup);
        expirationManager.stopExpirationTask();
        expirationManager.startExpirationTask(mockEventLoopGroup);

        /* Check assertions */
        verify(mockEventLoopGroup, times(2)).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(1L),
                eq(TimeUnit.SECONDS));
    }

    /**
     * Test {@link KeyExpirationManager#stopExpirationTask()}.
     */
    @Test
    void testStopExpirationTask() {
        when(mockScheduledFuture.isCancelled()).thenReturn(false);
        expirationManager.startExpirationTask(mockEventLoopGroup);
        expirationManager.stopExpirationTask();

        verify(mockScheduledFuture).cancel(false);
    }

    /**
     * Test {@link KeyExpirationManager#stopExpirationTask()} when no task is
     * scheduled.
     */
    @Test
    void testStopExpirationTask_WhenNotScheduled() {
        expirationManager.stopExpirationTask();

        verify(mockScheduledFuture, never()).cancel(anyBoolean());
    }

    /**
     * Test {@link KeyExpirationManager#stopExpirationTask()} when the previous task is cancelled
     */
    @Test
    void testStopExpirationTask_cancelled() {
        expirationManager.startExpirationTask(mockEventLoopGroup);
        expirationManager.stopExpirationTask();

        /* The 2nd time stop is called the task isn't null but is cancelled */
        when(mockScheduledFuture.isCancelled()).thenReturn(true);
        expirationManager.stopExpirationTask();

        verify(mockScheduledFuture, times(1)).cancel(anyBoolean());
    }
}
