package ru.linkshortner.cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.linkshortner.core.Link;
import ru.linkshortner.core.LinkManager;
import ru.linkshortner.infra.InMemoryStorage;

@ExtendWith(MockitoExtension.class)
class ConsoleAppTest {

    @Mock private LinkManager manager;
    @Mock private NotificationService notify;
    @Mock private InMemoryStorage storage;
    private ConsoleApp app;
    private UUID testUserId;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        app = new ConsoleApp(manager, testUserId, notify, storage);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void run_shortenCommand_CreatesLinkAndPrints() {
        String input = "shorten https://example.com\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Link mockLink = mock(Link.class);
        when(mockLink.getShortCode()).thenReturn("ABC123");
        when(manager.createLink(eq("https://example.com"), eq(testUserId))).thenReturn(mockLink);

        app.run();

        assertTrue(outContent.toString().contains("Короткая ссылка: clck.ru/ABC123"));
        verify(manager).createLink(eq("https://example.com"), eq(testUserId));
    }

    @Test
    void run_openCommand_RedirectsAndIncrements() throws Exception {
        String input = "open ABC123\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Link mockLink = mock(Link.class);
        when(manager.getByShortCode("ABC123")).thenReturn(mockLink);
        when(mockLink.getUserId()).thenReturn(testUserId);
        when(mockLink.isBlocked()).thenReturn(false);
        when(mockLink.isExpired()).thenReturn(false);
        when(mockLink.getLongUrl()).thenReturn("https://example.com");
        when(mockLink.getClicks()).thenReturn(1);

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop mockDesktop = mock(Desktop.class);
            desktopMock.when(Desktop::getDesktop).thenReturn(mockDesktop);
            doNothing().when(mockDesktop).browse(any(URI.class));

            app.run();

            assertTrue(outContent.toString().contains("Редирект выполнен. Кликов: 1"));
            verify(storage).saveLink(mockLink);
        }
    }

    @Test
    void run_editCommand_UpdatesLimit() {
        String input = "edit ABC123 clicks=20\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        app.run();

        assertTrue(outContent.toString().contains("Лимит обновлён до 20"));
        verify(manager).editLink(eq(testUserId), eq("ABC123"), eq(20));
    }
}
