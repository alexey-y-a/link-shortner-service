package ru.linkshortner.utils;

import java.net.URL;
import ru.linkshortner.exceptions.InvalidUrlException;

public class UrlUtil {

    public static void validateUrl(String url) {
        try {
            new URL(url);
        } catch (Exception e) {
            throw new InvalidUrlException("Некорректный URL: " + e.getMessage());
        }
    }
}
