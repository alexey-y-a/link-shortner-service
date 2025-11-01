package ru.linkshortner.cli;

import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;
import java.util.UUID;
import ru.linkshortner.config.Config;
import ru.linkshortner.core.Link;
import ru.linkshortner.core.LinkManager;
import ru.linkshortner.exceptions.AccessDeniedException;
import ru.linkshortner.exceptions.InvalidLimitException;
import ru.linkshortner.exceptions.InvalidUrlException;
import ru.linkshortner.exceptions.LinkNotFoundException;
import ru.linkshortner.infra.InMemoryStorage;

public class ConsoleApp {

    private final LinkManager manager;
    private final UUID userId;
    private final NotificationService notify;
    private final InMemoryStorage storage;

    public ConsoleApp(
            LinkManager manager, UUID userId, NotificationService notify, InMemoryStorage storage) {
        this.manager = manager;
        this.userId = userId;
        this.notify = notify;
        this.storage = storage;
    }

    public void run() {
        System.out.println("Сервис сокращения ссылок. Введите 'help' для помощи.");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine().trim();
            if (input.equals("exit")) {
                break;
            }
            if (input.equals("help")) {
                printHelp();
            } else if (input.startsWith("shorten ")) {
                String url = input.substring(8).trim();
                try {
                    Link link = manager.createLink(url, userId);
                    System.out.println(
                            "Короткая ссылка: " + Config.getBaseUrl() + link.getShortCode());
                } catch (InvalidUrlException e) {
                    System.out.println("Ошибка URL: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Неожиданная ошибка: " + e.getMessage());
                }
            } else if (input.equals("list")) {
                for (Link l : manager.getLinksByUser(userId)) {
                    System.out.printf(
                            "%s -> %s [%d/%d переходов]%n",
                            l.getShortCode(), l.getLongUrl(), l.getClicks(), l.getMaxClicks());
                }
            } else if (input.startsWith("open ")) {
                String code = input.substring(5).trim();
                try {
                    Link link = manager.getByShortCode(code);
                    if (!link.getUserId().equals(userId)) {
                        throw new AccessDeniedException("Нет доступа к ссылке");
                    }
                    if (link.isBlocked()) {
                        notify.sendLimit(link);
                        continue;
                    }
                    if (link.isExpired()) {
                        notify.sendExpired(link);
                        continue;
                    }
                    link.incrementClicks();
                    storage.saveLink(link);
                    Desktop.getDesktop().browse(new URI(link.getLongUrl()));
                    System.out.println("Редирект выполнен. Кликов: " + link.getClicks());
                } catch (LinkNotFoundException e) {
                    System.out.println("Ссылка не найдена: " + e.getMessage());
                } catch (AccessDeniedException e) {
                    System.out.println("Доступ запрещён: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Ошибка запуска браузера: " + e.getMessage());
                }
            } else if (input.startsWith("edit ")) {
                String[] parts = input.split(" ");
                if (parts.length != 3 || !parts[2].startsWith("clicks=")) {
                    System.out.println("Использование: edit <code> clicks=<N>");
                    continue;
                }
                try {
                    int n = Integer.parseInt(parts[2].substring(7));
                    manager.editLink(userId, parts[1], n);
                    System.out.println("Лимит обновлён до " + n);
                } catch (LinkNotFoundException e) {
                    System.out.println("Ссылка не найдена: " + e.getMessage());
                } catch (AccessDeniedException e) {
                    System.out.println("Доступ запрещён: " + e.getMessage());
                } catch (InvalidLimitException e) {
                    System.out.println("Некорректный лимит: " + e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("Некорректное число для лимита.");
                } catch (Exception e) {
                    System.out.println("Ошибка редактирования: " + e.getMessage());
                }
            } else {
                System.out.println("Неизвестная команда. Введите 'help' для списка.");
            }
        }
        System.out.println("Завершено.");
    }

    private void printHelp() {
        System.out.println("Доступные команды:");
        System.out.println(
                "  shorten <url>          — создать короткую ссылку (пример: shorten https://example.com)");
        System.out.println("  open <code>            — открыть ссылку в браузере");
        System.out.println("  list                   — список ваших ссылок");
        System.out.println(
                "  edit <code> clicks=<N> — изменить лимит переходов (пример: edit ABC123 clicks=50)");
        System.out.println("  help                   — справка");
        System.out.println("  exit                   — выход");
    }
}
