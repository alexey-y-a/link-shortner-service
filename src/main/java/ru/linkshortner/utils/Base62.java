package ru.linkshortner.utils;

public class Base62 {

    private static final String CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String encode(long num) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(CHARS.charAt((int) (num % 62)));
            num /= 62;
        } while (num > 0);
        return sb.reverse().toString();
    }
}
