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
См. `.github/workflows/maven.yml`:
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
│   │       └── linkshortner/  # Корневой пакет проекта
│   │           ├── Main.java  # Основной класс приложения
│   │           ├── cli/       # Компоненты консольного интерфейса (CLI)
│   │           │   ├── ConsoleApp.java  # Основное консольное приложение
│   │           │   └── NotificationService.java  # Сервис для отправки уведомлений 
│   │           ├── config/    # Классы для конфигурации
│   │           │   └── Config.java  # Класс для чтения конфигурации
│   │           ├── core/      # Основная бизнес-логика (модели и менеджеры)
│   │           │   ├── Link.java  # Модель данных для ссылки
│   │           │   └── LinkManager.java  # Менеджер ссылок
│   │           ├── exceptions/  # Пользовательские исключения
│   │           │   ├── AccessDeniedException.java  # Исключение для отказа в доступе
│   │           │   ├── ConfigLoadException.java  # Искл-е для ошибок загрузки конф-ии
│   │           │   ├── InvalidLimitException.java  # Искл-е для некорр. лимита переходов
│   │           │   ├── InvalidUrlException.java  # Исключение для невалидного URL
│   │           │   └── LinkNotFoundException.java  # Исключение для несуществующей ссылки
│   │           ├── infra/     # Инфраструктурные компоненты
│   │           │   ├── CleanupScheduler.java  # Планировщик автоматической очистки
│   │           │   ├── InMemoryStorage.java  # Хранилище ссылок в памяти
│   │           │   └── UuidProvider.java  # Провайдер UUID пользователя
│   │           └── utils/     # Утилитарные классы (вспомогательные функции)
│   │               ├── Base62.java  # Утилита для кодирования в Base62
│   │               └── UrlUtil.java  # Утилиты для URL
│   └── resources/            # Ресурсы (файлы конфигурации)
└── test/                     # Директория для тестов (unit и интеграционные)
    └── java/
        └── ru/
            └── linkshortner/  # Тесты в том же пакете, что и основной код
                ├── cli/       # Тесты для CLI
                │   └── ConsoleAppTest.java  # Тесты консольного приложения
                ├── core/      # Тесты для бизнес-логики
                │   ├── LinkManagerTest.java  # Тесты менеджера
                │   └── LinkTest.java  # Тесты модели Link
                ├── infra/     # Тесты для инфраструктуры
                │   └── InMemoryStorageTest.java  # Тесты хранилища
                └── utils/     # Тесты для утилит
                    ├── Base62Test.java  # Тесты кодирования
                    └── UrlUtilTest.java  # Тесты валидации URL
```
