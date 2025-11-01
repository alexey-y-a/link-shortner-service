package ru.linkshortner.infra;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.linkshortner.cli.NotificationService;
import ru.linkshortner.core.LinkManager;

public class CleanupScheduler {

    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);

    public CleanupScheduler(LinkManager manager, NotificationService notify, long intervalMs) {
        sched.scheduleAtFixedRate(
                () -> {
                    try {
                        manager.removeExpiredLinks(notify);
                    } catch (Exception e) {
                        System.err.println("Ошибка cleanup: " + e.getMessage());
                    }
                },
                intervalMs,
                intervalMs,
                TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        sched.shutdown();
    }
}
