package ru.linkshortner;

import java.util.UUID;
import ru.linkshortner.cli.ConsoleApp;
import ru.linkshortner.cli.NotificationService;
import ru.linkshortner.config.Config;
import ru.linkshortner.core.LinkManager;
import ru.linkshortner.exceptions.ConfigLoadException;
import ru.linkshortner.infra.CleanupScheduler;
import ru.linkshortner.infra.InMemoryStorage;
import ru.linkshortner.infra.UuidProvider;

public class Main {

    public static void main(String[] args) {
        try {
            InMemoryStorage storage = new InMemoryStorage();
            NotificationService notify = new NotificationService();
            LinkManager manager =
                    new LinkManager(
                            storage,
                            Config.getDefaultMaxClicks(),
                            Config.getTtlMillis(),
                            Config.getBaseUrl());
            UUID userId = UuidProvider.getOrCreateUserId();
            CleanupScheduler scheduler =
                    new CleanupScheduler(manager, notify, Config.getCleanupIntervalMs());
            try {
                new ConsoleApp(manager, userId, notify, storage).run();
            } finally {
                scheduler.shutdown();
            }
        } catch (ConfigLoadException e) {
            System.err.println("Критическая ошибка конфигурации: " + e.getMessage());
            System.err.println("Проверьте config.properties и перезапустите.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
