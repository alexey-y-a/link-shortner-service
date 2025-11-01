package ru.linkshortner.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkTest {

    private Link link;
    private UUID userId = UUID.randomUUID();
    private long now = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        link = new Link("https://example.com", "ABC123", userId, now, now + 86400000L, 10);
    }

    @Test
    void isExpired_False_IfNotExpired() {
        assertFalse(link.isExpired());
    }

    @Test
    void isExpired_True_IfExpired() {
        link = new Link("https://example.com", "ABC123", userId, now - 86400001L, now - 1, 10);
        assertTrue(link.isExpired());
    }

    @Test
    void isBlocked_False_IfUnderLimit() {
        assertFalse(link.isBlocked());
    }

    @Test
    void isBlocked_True_IfAtLimit() {
        link.incrementClicks();
        for (int i = 0; i < 9; i++) link.incrementClicks();
        assertTrue(link.isBlocked());
    }

    @Test
    void incrementClicks_IncreasesCount() {
        link.incrementClicks();
        assertEquals(1, link.getClicks());
    }

    @Test
    void setMaxClicks_UpdatesLimit() {
        link.setMaxClicks(20);
        assertEquals(20, link.getMaxClicks());
    }

    @Test
    void getLongUrl_ReturnsCorrectUrl() {
        assertEquals("https://example.com", link.getLongUrl());
    }

    @Test
    void getShortCode_ReturnsCorrectCode() {
        assertEquals("ABC123", link.getShortCode());
    }

    @Test
    void getUserId_ReturnsCorrectUserId() {
        assertEquals(userId, link.getUserId());
    }

    @Test
    void isBlocked_True_IfMaxClicksZero() {
        Link zeroLimit = new Link("https://example.com", "ABC123", userId, now, now + 86400000L, 0);
        assertTrue(zeroLimit.isBlocked());
    }

    @Test
    void isExpired_False_IfExactlyExpiresNow() {
        long exactNow = System.currentTimeMillis();
        Link exact = new Link("https://example.com", "ABC123", userId, exactNow, exactNow, 10);
        assertFalse(exact.isExpired());
    }
}
