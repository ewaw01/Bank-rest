# 💳 Bank REST API

**REST API** для управления банковскими картами с **JWT-аутентификацией**, ролевой моделью (`USER` / `ADMIN`),  
**шифрованием** данных карт, **переводами** между своими картами и **unit-тестами** с покрытием ~75%.

---

## 📚 Оглавление
- [Документация API](#-документация-api)
- [О проекте](#-о-проекте)
- [Стек технологий](#-стек-технологий)
- [Что умеет приложение](#-что-умеет-приложение)
- [Проблемы и решения](#-проблемы-и-решения)
- [Как запустить](#-как-запустить)
- [API-эндпоинты](#-api-эндпоинты)
- [Примеры запросов](#-примеры-запросов-postman)
- [Тестирование](#-тестирование)
- [Дальнейшие планы](#-дальнейшие-планы)
- [Контакты](#-контакты)

---

## Документация API

После запуска приложения документация доступна по адресу:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Файл спецификации OpenAPI находится в `docs/openapi.yaml`.

---

## О проекте

Проект создан для отработки навыков разработки **REST API** на **Spring Boot** с акцентом на:

- реальную **JWT-аутентификацию** и **ролевую авторизацию** (`USER` / `ADMIN`)
- **шифрование** и **маскирование** номеров банковских карт
- **переводы** между своими картами
- **глобальную обработку ошибок** и **логирование**
- полноценное **тестирование** (юнит + интеграционное)
- управление миграциями через **Liquibase**
- контейнеризацию через **Docker Compose**

---

## Стек технологий

| Категория | Технологии |
|-----------|-------------|
| **Язык** | Java 21 |
| **Фреймворк** | Spring Boot 4, Spring Security, Spring Data JPA |
| **Безопасность** | JWT (JJWT), BCrypt, AES (шифрование) |
| **Базы данных** | PostgreSQL, Liquibase |
| **Тестирование** | JUnit 5, Mockito, Spring Boot Test |
| **Сборка** | Maven |
| **Инструменты** | Git, Postman, IntelliJ IDEA, Docker Compose |

---

## Что умеет приложение

### Пользователи
- Регистрация и логин (JWT)
- Просмотр всех пользователей (только ADMIN)
- Обновление своих данных (USER) или любых (ADMIN)
- Удаление пользователя (только ADMIN)
- Блокировка пользователя (только ADMIN)

### Карты
- Создание карты (только ADMIN) с автоматической генерацией и шифрованием номера
- Просмотр своих карт с маскированием номера (`**** **** **** 1234`)
- Запрос на блокировку карты (USER — создаёт заявку `BLOCK_REQUESTED`)
- Подтверждение блокировки карты (ADMIN)
- Активация карты (ADMIN)
- Удаление карты (ADMIN)
- Просмотр баланса карты
- Изменение баланса карты (только ADMIN)

### Переводы
- Переводы между своими картами (USER)
- Проверка статуса, срока действия и баланса карт

### Дополнительно
- **Пагинация** и **динамические фильтры**
- **Глобальная обработка ошибок** (`@ControllerAdvice`)
- **Логирование** всех ключевых событий
- **Unit-тесты** (покрытие ~75%)
- **Миграции** через Liquibase
- **Docker Compose**

---

## Проблемы и решения

**_Проблема_**: Номер карты нужно хранить в зашифрованном виде, а в ответе API показывать только маску.
**Решение**: Использован `Encryptors.text()` для двустороннего шифрования номера карты. В DTO возвращается маскированный номер через `CardMapper`.

**_Проблема_**: При перезапуске приложения возникала ошибка `BadPaddingException`, потому что соль для шифрования генерировалась заново при каждом запуске.
**Решение**: Соль вынесена в `application.properties` как фиксированная (`crypt.salt`).

**_Проблема_**: При переводе между картами нужно было проверять, что карты принадлежат текущему пользователю, активны и не просрочены.
**Решение**: В `CardService.transferBetweenCards()` реализована полная проверка: принадлежность карт, статус, срок действия, баланс.

**_Проблема_**: Пользователь должен не сразу блокировать карту, а только запрашивать блокировку.
**Решение**: Добавлен статус `BLOCK_REQUESTED`. Пользователь отправляет запрос на блокировку (`/card/{id}/block-request`), администратор подтверждает (`/admin/card/{id}/block`).

**_Проблема_**: В `User` DTO передавался список `CardEntity` (сущности JPA), что нарушало принципы DTO.
**Решение**: Создан `Card` DTO с маскированным номером, а в `UserMapper` список карт маппится через `cardMapper.toDto()`.

---

## Как запустить

### 1. Требования
- Java 21 (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Docker (для PostgreSQL)

### 2. Клонирование
```bash
git clone https://github.com/ewaw01/bank-rest.git
cd bank-rest
```

### 3. Поднять PostgreSQL через Docker Compose
```bash
docker-compose up -d
```

### 4. Настроить application.properties
1. Создайте файл application.properties (src/main/resources/application.properties)
2. Скопируйте в свой application.properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5446/bank_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=none
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=YOUR_JWT_SECRET
jwt.expiration=86400000

crypt.password=YOUR_CRYPT_PASSWORD
crypt.salt=YOUR_CRYPT_SALT

spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.driver-class-name=org.postgresql.Driver
spring.liquibase.url=jdbc:postgresql://localhost:5446/bank_db
spring.liquibase.user=postgres
spring.liquibase.password=YOUR_DB_PASSWORD
```
3. Не забудьте заменить YOUR_DB_PASSWORD (можете взять его из docker-compose, либо заменить на свой, но тогда придется его заменить и в compose и в properties), YOUR_JWT_SECRET, YOUR_CRYPT_PASSWORD, YOUR_CRYPT_SALT

`jwt.secret` - JWT-секрет для security

|Требование|Значение|
|---|---|
|**Длина**|минимум **32 символа** (рекомендуется 64+)|
|**Символы**|только **буквы и цифры** (латиница)|
|**Пример**|`a7K9mN2pQ5rT8vW1xZ4cF6gH9jL2oP5sU8yB3eR7tY0uI4oK7lQ2wE5rT9yU1o`|
|**Что нельзя**|пробелы, кавычки, спецсимволы (`!@#$%^&*()`)|

`crypt.password` — мастер-пароль для шифрования

|Требование|Значение|
|---|---|
|**Длина**|минимум **16 символов** (рекомендуется 32)|
|**Символы**|только **буквы и цифры** (латиница)|
|**Пример**|`MySuperSecretPassword123`|
|**Что нельзя**|пробелы, кавычки, спецсимволы (`!@#$%^&*()`)|

`crypt.salt` — соль для шифрования

|Требование|Значение|
|---|---|
|**Длина**|**чётное** количество символов (например, 16)|
|**Символы**|только **шестнадцатеричные**: `0-9`, `a-f`|
|**Пример**|`8f3a7c2e1d4b6f9a`|
|**Что нельзя**|нечётное количество, буквы выше `f`, пробелы|

### 5. Запуск
```bash
./mvnw spring-boot:run
```

---

## API-эндпоинты
Базовый URL: `http://localhost:8080/api/bank`

### Аутентификация (`/api/auth`)
| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | `/api/auth/register` | Регистрация нового пользователя |
| POST | `/api/auth/login` | Логин, возвращает JWT-токен |

### Пользователи
| Метод | Эндпоинт | Доступ | Описание |
|-------|----------|--------|----------|
| GET | `/admin/user` | `ADMIN` | Получить всех пользователей (пагинация) |
| DELETE | `/user/{id}` | `USER`/`ADMIN` | Удалить пользователя |
| PUT | `/user` | `USER`/`ADMIN` | Обновить данные пользователя |
| PUT | `/admin/user/{id}/block` | `ADMIN` | Заблокировать пользователя |

### Карты
| Метод | Эндпоинт | Доступ | Описание |
|-------|----------|--------|----------|
| POST | `/admin/card` | `ADMIN` | Создать карту |
| DELETE | `/admin/card/{id}` | `ADMIN` | Удалить карту |
| PUT | `/card/{id}/block-request` | `USER` | Запросить блокировку карты |
| PUT | `/admin/card/{id}/block` | `ADMIN` | Подтвердить блокировку |
| PUT | `/admin/card/{id}/activate` | `ADMIN` | Активировать карту |
| GET | `/user/card` | `USER`/`ADMIN` | Получить карты (с фильтрацией) |
| PUT | `/admin/card/balance` | `ADMIN` | Обновить баланс карты |

### Переводы
| Метод | Эндпоинт | Доступ | Описание |
|-------|----------|--------|----------|
| POST | `/card/transfer` | `USER` | Перевод между своими картами |

---

## Примеры запросов (Postman)
### 1. Регистрация
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@test.com",
  "password": "123456",
  "username": "john"
}
```

### 2. Логин (получить JWT)
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@test.com",
  "password": "123456"
}
```

**Ответ:** токен в формате `Successfully logged in!\nYour token: eyJhbGciOi...`

### 3. Создать карту (ADMIN)
```http
POST http://localhost:8080/api/bank/admin/card
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "ownerId": 1,
  "expiryDate": "2028-12-31"
}
```

### 4. Получить свои карты
```http
GET http://localhost:8080/api/bank/user/card?user_id=1
Authorization: Bearer <USER_TOKEN>
```

### 5. Запросить блокировку карты (USER)
```http
PUT http://localhost:8080/api/bank/card/1/block-request
Authorization: Bearer <USER_TOKEN>
```

### 6. Подтвердить блокировку (ADMIN)
```http
PUT http://localhost:8080/api/bank/admin/card/1/block
Authorization: Bearer <ADMIN_TOKEN>
```

### 7. Перевод между своими картами
```http
POST http://localhost:8080/api/bank/card/transfer
Authorization: Bearer <USER_TOKEN>
Content-Type: application/json

{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 500
}
```

---

## Тестирование
Проект покрыт unit-тестами и интеграционными тестами:

- `AuthServiceTest` — регистрация и логин (Mockito)
- `CardServiceTest` — создание, удаление, блокировка, переводы (Mockito)
- `UserServiceTest` — обновление, удаление, блокировка пользователей (Mockito)
- `MainControllerTest` — REST API (MockMvc, JWT)

Всего тестов: 40+  
Покрытие кода: ~75%

### Запуск тестов:
```bash
./mvnw test
```

---

## Дальнейшие планы
1. Вынести правильно логику, где можно, для большей абстракции
2. Добавить новую бизнес-логику (допустим, Добавить таблицу transactions, куда записывается каждое действие с картой: перевод, блокировка, активация, изменение баланса)
3. Повысить технологический стек проекта (допустим, добавить Kafka. Если перевод большой или система загружена — отправлять его в очередь и обрабатывать асинхронно)

---

## 📫 Контакты
GitHub: github.com/ewaw01
