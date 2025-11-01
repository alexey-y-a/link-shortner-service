package ru.linkshortner.config;

import java.io.FileInputStream;
import java.util.Properties;
import ru.linkshortner.exceptions.ConfigLoadException;

public class Config {

    private static final Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("config.properties"));
        } catch (Exception e) {
            throw new ConfigLoadException("Ошибка загрузки config.properties", e);
        }
    }

    public static int getDefaultMaxClicks() {
        try {
            return Integer.parseInt(props.getProperty("max.clicks", "100"));
        } catch (NumberFormatException e) {
            throw new ConfigLoadException("Некорректный формат max.clicks в config.properties", e);
        }
    }

    public static long getTtlMillis() {
        try {
            return Long.parseLong(props.getProperty("ttl.hours", "24")) * 3600_000L;
        } catch (NumberFormatException e) {
            throw new ConfigLoadException("Некорректный формат ttl.hours в config.properties", e);
        }
    }

    public static String getBaseUrl() {
        return props.getProperty("base.url.prefix", "clck.ru/");
    }

    public static long getCleanupIntervalMs() {
        try {
            return Long.parseLong(props.getProperty("cleanup.interval.mins", "30")) * 60_000L;
        } catch (NumberFormatException e) {
            throw new ConfigLoadException(
                    "Некорректный формат cleanup.interval.mins в config.properties", e);
        }
    }

    public static int getShortCodeLength() {
        try {
            return Integer.parseInt(props.getProperty("short.code.length", "6"));
        } catch (NumberFormatException e) {
            throw new ConfigLoadException(
                    "Некорректный формат short.code.length в config.properties", e);
        }
    }
}
