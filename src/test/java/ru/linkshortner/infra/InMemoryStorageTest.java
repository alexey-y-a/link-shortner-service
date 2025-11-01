package ru.linkshortner.infra;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.linkshortner.cli.NotificationService;
import ru.linkshortner.core.Link;

@ExtendWith(MockitoExtension.class)
class InMemoryStorageTest {

    private InMemoryStorage storage;
    private UUID userId = UUID.randomUUID();
    @Mock private NotificationService notify;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
    }

    @Test
    void saveLink_SavesToBothIndexes() {
        Link link =
                new Link(
                        "https://example.com",
                        "ABC123",
                        userId,
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + 86400000L,
                        10);
        storage.saveLink(link);
        assertEquals(link, storage.getByShortCode("ABC123"));
        assertEquals(List.of(link), storage.getByUser(userId));
    }

    @Test
    void getByShortCode_NotFound_ReturnsNull() {
        assertNull(storage.getByShortCode("nonexistent"));
    }

    @Test
    void getByUser_EmptyList_ReturnsEmpty() {
        assertTrue(storage.getByUser(UUID.randomUUID()).isEmpty());
    }

    @Test
    void removeIf_RemovesExpiredAndNotifies() {
        Link expired = mock(Link.class);
        when(expired.isExpired()).thenReturn(true);
        when(expired.getShortCode()).thenReturn("EXP123");
        when(expired.getUserId()).thenReturn(userId);
        storage.saveLink(expired);

        Predicate<Link> pred = Link::isExpired;
        storage.removeIf(pred, notify);

        verify(notify).sendExpired(expired);
        assertNull(storage.getByShortCode("EXP123"));
    }

    @Test
    void removeIf_ThreadSafe_ConcurrentAccess() throws InterruptedException {
        Link link =
                new Link(
                        "https://example.com",
                        "ABC123",
                        userId,
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + 86400000L,
                        10);
        storage.saveLink(link);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            executor.submit(
                    () -> {
                        try {
                            storage.getByShortCode("ABC123");
                        } finally {
                            latch.countDown();
                        }
                    });
        }
        latch.await();
        executor.shutdown();
        assertEquals(link, storage.getByShortCode("ABC123"));
    }
}
