package ru.linkshortner.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Base62Test {

    @Test
    void encode_Zero_ReturnsEmptyOrBase() {
        assertEquals("a", Base62.encode(0));
    }

    @Test
    void encode_PositiveNumber_ReturnsCorrectBase62() {
        assertEquals("k", Base62.encode(10));
    }

    @Test
    void encode_LargeNumber_EncodesCorrectly() {
        assertEquals("baa", Base62.encode(3844));
    }
}
