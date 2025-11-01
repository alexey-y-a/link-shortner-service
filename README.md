## Сервис сокращения ссылок (Java CLI)

Консольное приложение для сокращения длинных ссылок с поддержкой:

- уникальных коротких ссылок для каждого пользователя (UUID),
- лимита переходов,
- контроля срока жизни ссылки (TTL),
- автоматического удаления "протухших" ссылок,
- администрирования через консоль.

## Установка и запуск
- Требования: Java 17+, Maven 3.6+.
- Клонируйте репозиторий git clone https://github.com/alexey-y-a/link-shortner-service.git
- Сборка проекта: `mvn clean package`.

## Запуск
- `mvn exec:java -Dexec.mainClass="ru.linkshortner.Main"` (или `java -cp target/classes ru.linkshortner.Main`).
- При первом запуске генерируется UUID в `user.uuid` (для изоляции пользователя).
- В CLI: введите команды (см. `help`).

## Тестирование
- Запуск тестов: `mvn test`.

## Команды (UX CLI)
- `shorten <url>` — создать короткую ссылку (пример: `shorten https://example.com` → `clck.ru/ABC123`).
- `open <code>` — открыть в браузере (редирект + инкремент кликов; блокирует если лимит/TTL истёк).
- `list` — список ссылок: `<code> -> <url> [<clicks>/<max>]`.
- `edit <code> clicks=<N>` — изменить лимит (пример: `edit ABC123 clicks=50`; только владелец).
- `help` — справка.
- `exit` — выход.

## GitHub Actions (CI шаблон)
Создайте `.github/workflows/maven.yml`:
```yaml
name: Java CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with: { java-version: '17'}
    - run: mvn clean spotless:check compile test jacoco:report
```
    
## Структура проекта
```
src/
├── main/
│   ├── java/
│   │   └── ru/
│   │       └── linkshortner/  # Корневой пакет проекта (сервис сокращения ссылок)
│   │           ├── Main.java  # Основной класс приложения (точка входа, инициализация компонентов и запуск CLI)
│   │           ├── cli/       # Компоненты консольного интерфейса (CLI)
│   │           │   ├── ConsoleApp.java  # Основное консольное приложение (обработка команд: shorten, open и т.д.)
│   │           │   └── NotificationService.java  # Сервис для отправки уведомлений 
│   │           ├── config/    # Классы для конфигурации (загрузка свойств из файла)
│   │           │   └── Config.java  # Класс для чтения конфигурации (TTL, лимиты и т.д. из config.properties)
│   │           ├── core/      # Основная бизнес-логика (модели и менеджеры)
│   │           │   ├── Link.java  # Модель данных для ссылки (поля: URL, код, TTL и методы isExpired/isBlocked)
│   │           │   └── LinkManager.java  # Менеджер ссылок (создание, редактирование, генерация уникальных кодов)
│   │           ├── exceptions/  # Пользовательские исключения (для обработки ошибок)
│   │           │   ├── AccessDeniedException.java  # Исключение для отказа в доступе (не владелец ссылки)
│   │           │   ├── ConfigLoadException.java  # Исключение для ошибок загрузки конфигурации
│   │           │   ├── InvalidLimitException.java  # Исключение для некорректного лимита переходов
│   │           │   ├── InvalidUrlException.java  # Исключение для невалидного URL
│   │           │   └── LinkNotFoundException.java  # Исключение для несуществующей ссылки
│   │           ├── infra/     # Инфраструктурные компоненты (хранение, планировщики, провайдеры)
│   │           │   ├── CleanupScheduler.java  # Планировщик автоматической очистки
│   │           │   ├── InMemoryStorage.java  # Хранилище ссылок в памяти (ConcurrentHashMap для thread-safety)
│   │           │   └── UuidProvider.java  # Провайдер UUID пользователя (генерация и сохранение в файл user.uuid)
│   │           └── utils/     # Утилитарные классы (вспомогательные функции)
│   │               ├── Base62.java  # Утилита для кодирования в Base62 (генерация коротких кодов из хэша)
│   │               └── UrlUtil.java  # Утилиты для URL (валидация с throw на ошибки)
│   └── resources/            # Ресурсы (файлы конфигурации)
└── test/                     # Директория для тестов (unit и интеграционные)
    └── java/
        └── ru/
            └── linkshortner/  # Тесты в том же пакете, что и основной код
                ├── cli/       # Тесты для CLI
                │   └── ConsoleAppTest.java  # Тесты консольного приложения (команды shorten, open, edit с моками)
                ├── core/      # Тесты для бизнес-логики
                │   ├── LinkManagerTest.java  # Тесты менеджера (создание, уникальность, редактирование, исключения)
                │   └── LinkTest.java  # Тесты модели Link (isExpired, isBlocked, incrementClicks)
                ├── infra/     # Тесты для инфраструктуры
                │   └── InMemoryStorageTest.java  # Тесты хранилища (save/get/remove, concurrency с ExecutorService)
                └── utils/     # Тесты для утилит
                    ├── Base62Test.java  # Тесты кодирования (encode для чисел)
                    └── UrlUtilTest.java  # Тесты валидации URL (valid/invalid с assertThrows)
```
