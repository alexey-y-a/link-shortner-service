package ru.linkshortner.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.linkshortner.exceptions.AccessDeniedException;
import ru.linkshortner.exceptions.InvalidLimitException;
import ru.linkshortner.exceptions.InvalidUrlException;
import ru.linkshortner.exceptions.LinkNotFoundException;
import ru.linkshortner.infra.InMemoryStorage;

@ExtendWith(MockitoExtension.class)
class LinkManagerTest {

    private LinkManager manager;
    @Mock private InMemoryStorage storage;
    private UUID userId1 = UUID.randomUUID();
    private UUID userId2 = UUID.randomUUID();
    private long ttl = 86400000L;
    private int maxClicks = 10;

    @BeforeEach
    void setUp() {
        manager = new LinkManager(storage, maxClicks, ttl, "clck.ru/");
    }

    @Test
    void createLink_ValidUrl_CreatesAndSavesLink() {
        String url = "https://example.com";
        Link link = manager.createLink(url, userId1);
        assertNotNull(link);
        assertEquals(url, link.getLongUrl());
        verify(storage).saveLink(any(Link.class));
    }

    @Test
    void createLink_InvalidUrl_ThrowsInvalidUrlException() {
        assertThrows(InvalidUrlException.class, () -> manager.createLink("invalid-url", userId1));
    }

    @Test
    void createLink_GeneratesUniqueCodes_ForSameUrlDifferentUsers() {
        String url = "https://example.com";
        Link link1 = manager.createLink(url, userId1);
        Link link2 = manager.createLink(url, userId2);
        assertNotEquals(link1.getShortCode(), link2.getShortCode());
    }

    @Test
    void getByShortCode_Found_ReturnsLink() {
        String code = "ABC123";
        Link mockLink =
                new Link(
                        "https://example.com",
                        code,
                        userId1,
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + ttl,
                        maxClicks);
        when(storage.getByShortCode(code)).thenReturn(mockLink);
        Link result = manager.getByShortCode(code);
        assertEquals(mockLink, result);
    }

    @Test
    void getByShortCode_NotFound_ThrowsLinkNotFoundException() {
        when(storage.getByShortCode("nonexistent")).thenReturn(null);
        assertThrows(LinkNotFoundException.class, () -> manager.getByShortCode("nonexistent"));
    }

    @Test
    void editLink_ValidOwnerAndLimit_UpdatesAndSaves() {
        String code = "ABC123";
        Link mockLink =
                new Link(
                        "https://example.com",
                        code,
                        userId1,
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + ttl,
                        maxClicks);
        when(storage.getByShortCode(code)).thenReturn(mockLink);
        manager.editLink(userId1, code, 20);
        assertEquals(20, mockLink.getMaxClicks());
        verify(storage).saveLink(mockLink);
    }

    @Test
    void editLink_WrongOwner_ThrowsAccessDeniedException() {
        String code = "ABC123";
        Link mockLink =
                new Link(
                        "https://example.com",
                        code,
                        userId1,
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + ttl,
                        maxClicks);
        when(storage.getByShortCode(code)).thenReturn(mockLink);
        assertThrows(AccessDeniedException.class, () -> manager.editLink(userId2, code, 20));
    }

    @Test
    void editLink_NegativeLimit_ThrowsInvalidLimitException() {
        String code = "ABC123";
        Link mockLink =
                new Link(
                        "https://example.com",
                        code,
                        userId1,
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + ttl,
                        maxClicks);
        when(storage.getByShortCode(code)).thenReturn(mockLink);
        assertThrows(InvalidLimitException.class, () -> manager.editLink(userId1, code, -1));
    }
}
