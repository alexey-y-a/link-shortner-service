package ru.linkshortner.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ru.linkshortner.exceptions.InvalidUrlException;

class UrlUtilTest {

    @Test
    void validateUrl_ValidHttp_DoesNotThrow() {
        assertDoesNotThrow(() -> UrlUtil.validateUrl("https://example.com"));
    }

    @Test
    void validateUrl_InvalidFormat_ThrowsInvalidUrlException() {
        InvalidUrlException exception =
                assertThrows(InvalidUrlException.class, () -> UrlUtil.validateUrl("invalid-url"));
        assertTrue(exception.getMessage().contains("Некорректный URL"));
    }
}
