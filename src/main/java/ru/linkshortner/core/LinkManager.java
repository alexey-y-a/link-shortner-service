package ru.linkshortner.core;

import java.util.List;
import java.util.UUID;
import ru.linkshortner.cli.NotificationService;
import ru.linkshortner.config.Config;
import ru.linkshortner.exceptions.AccessDeniedException;
import ru.linkshortner.exceptions.InvalidLimitException;
import ru.linkshortner.exceptions.LinkNotFoundException;
import ru.linkshortner.infra.InMemoryStorage;
import ru.linkshortner.utils.Base62;
import ru.linkshortner.utils.UrlUtil;

public class LinkManager {

    private final InMemoryStorage storage;
    private final int defaultMaxClicks;
    private final long defaultTtlMillis;
    private final String baseUrl;
    private final int shortCodeLength;

    public LinkManager(
            InMemoryStorage storage, int defaultMaxClicks, long defaultTtlMillis, String baseUrl) {
        this.storage = storage;
        this.defaultMaxClicks = defaultMaxClicks;
        this.defaultTtlMillis = defaultTtlMillis;
        this.baseUrl = baseUrl;
        this.shortCodeLength = Config.getShortCodeLength();
    }

    public Link createLink(String longUrl, UUID userId) {
        UrlUtil.validateUrl(longUrl);
        String shortCode = generateShortCode(longUrl, userId);
        long now = System.currentTimeMillis();
        long expiresAt = now + defaultTtlMillis;
        Link link = new Link(longUrl, shortCode, userId, now, expiresAt, defaultMaxClicks);
        storage.saveLink(link);
        return link;
    }

    public Link getByShortCode(String shortCode) {
        Link link = storage.getByShortCode(shortCode);
        if (link == null) {
            throw new LinkNotFoundException("Ссылка не найдена по коду: " + shortCode);
        }
        return link;
    }

    public List<Link> getLinksByUser(UUID userId) {
        return storage.getByUser(userId);
    }

    public void removeExpiredLinks(NotificationService notify) {
        storage.removeIf(link -> link.isExpired(), notify);
    }

    public void editLink(UUID userId, String shortCode, int maxClicks) {
        Link link = getByShortCode(shortCode);
        if (!link.getUserId().equals(userId)) {
            throw new AccessDeniedException("Нет доступа к ссылке: вы не владелец");
        }
        if (maxClicks < 0) {
            throw new InvalidLimitException("Лимит не может быть отрицательным: " + maxClicks);
        }
        link.setMaxClicks(maxClicks);
        storage.saveLink(link);
    }

    private String generateShortCode(String longUrl, UUID userId) {
        String data = longUrl + userId.toString() + System.nanoTime();
        long hash = Math.abs(data.hashCode());
        String code = Base62.encode(hash);
        while (code.length() < shortCodeLength) {
            code = "0" + code;
        }
        return code.substring(0, shortCodeLength);
    }
}
