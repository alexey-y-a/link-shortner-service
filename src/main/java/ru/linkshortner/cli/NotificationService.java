package ru.linkshortner.cli;

import ru.linkshortner.core.Link;

public class NotificationService {

    public void sendExpired(Link link) {
        System.out.printf(
                "Уведомление: ссылка %s истекла и удалена (%s)%n",
                link.getShortCode(), link.getLongUrl());
    }

    public void sendLimit(Link link) {
        System.out.printf(
                "Уведомление: ссылка %s недоступна (исчерпан лимит переходов)%n",
                link.getShortCode());
    }
}
