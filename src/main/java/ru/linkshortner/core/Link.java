package ru.linkshortner.core;

import java.time.Instant;
import java.util.UUID;

public class Link {

    private final String longUrl;
    private final String shortCode;
    private final UUID userId;
    private final long createdAt;
    private final long expiresAt;
    private int maxClicks;
    private int clicks;

    public Link(
            String longUrl,
            String shortCode,
            UUID userId,
            long createdAt,
            long expiresAt,
            int maxClicks) {
        this.longUrl = longUrl;
        this.shortCode = shortCode;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxClicks = maxClicks;
        this.clicks = 0;
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() > expiresAt;
    }

    public boolean isBlocked() {
        return clicks >= maxClicks;
    }

    public void incrementClicks() {
        clicks++;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getClicks() {
        return clicks;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public void setMaxClicks(int maxClicks) {
        this.maxClicks = maxClicks;
    }
}
