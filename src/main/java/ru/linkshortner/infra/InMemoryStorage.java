package ru.linkshortner.infra;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import ru.linkshortner.cli.NotificationService;
import ru.linkshortner.core.Link;

public class InMemoryStorage {

    private final Map<String, Link> codeIndex = new ConcurrentHashMap<>();
    private final Map<UUID, List<Link>> userLinks = new ConcurrentHashMap<>();

    public void saveLink(Link link) {
        codeIndex.put(link.getShortCode(), link);
        userLinks.computeIfAbsent(link.getUserId(), k -> new ArrayList<>()).add(link);
    }

    public Link getByShortCode(String code) {
        return codeIndex.get(code);
    }

    public List<Link> getByUser(UUID userId) {
        return userLinks.getOrDefault(userId, List.of());
    }

    public void removeIf(java.util.function.Predicate<Link> pred, NotificationService notify) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Link> entry : codeIndex.entrySet()) {
            if (pred.test(entry.getValue())) {
                toRemove.add(entry.getKey());
                notify.sendExpired(entry.getValue());
            }
        }
        for (String code : toRemove) {
            Link link = codeIndex.remove(code);
            if (link != null) userLinks.get(link.getUserId()).remove(link);
        }
    }
}
