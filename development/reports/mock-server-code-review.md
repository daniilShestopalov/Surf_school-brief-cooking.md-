# Отчет по результатам Code Review (Этап 5: Mock-сервер)

## 1. Архитектура и DI (KoinModules.kt)
✅ **Оценка: Безупречно.** 
Интеграция `MockEngine` реализована корректно. Экземпляр Ktor `HttpClient` корректно инициализируется фабрикой `MockApiHandler.createMockEngine()` внутри `coreModule`. Подмена сетевого слоя реализована чисто и не ломает основную архитектуру приложения.

## 2. Консистентность JSON-ответов (`MockApiHandler.kt`)
✅ **Оценка: Соответствует схеме данных (`data-schema.md`).**
- **Форматы цен:** Все стоимости (например, `equipmentTariff: 150000`, `fixedBasePrice: 500000`) корректно передаются в копейках.
- **Типы данных:** Массив аллергий (`allergyProfile`) возвращается как массив строк (`["Орехи", "Мед"]`). Поле `datetimeStart` передается в корректном формате Timestamp.
- **Enums:** Статусы `SCHEDULED` (для слотов) и `ACTIVE` (для бронирований) написаны без опечаток и соответствуют доменным сущностям.

## 3. Логика сложных сценариев и краевые случаи (Найдено 3 ошибки)

Здесь найдены критические недочеты, блокирующие полноценное QA-тестирование.

### 🔴 Баг 1: Нарушение логики Идемпотентности (Уязвимость мока)
В `MockApiHandler.kt` (строки 153–165) допущена грубая ошибка:
```kotlin
if (idempotencyKey != null && seenKeys.contains(idempotencyKey)) {
    // Return same successful response for repeated idempotent key
    respond(...) // Всегда возвращает статус 200 OK
```
**В чем проблема:** Если первый запрос от клиента завершился ошибкой (например, `409 SLOT_FULL`), и клиентская логика попытается повторить запрос (retry) с тем же самым `Idempotency-Key`, сервер ошибочно вернет `200 OK`! Кроме того, ключ попадает в `seenKeys` до формирования ответа, ломая эмуляцию ошибок при ретраях.

### 🔴 Баг 2: Отсутствие обработки 429 Too Many Requests
В задании указана необходимость проверки 429 статуса и заголовка `Retry-After`, однако в `MockApiHandler.kt` полностью отсутствует триггер для симуляции этой ошибки. 

### 🔴 Баг 3: Отсутствие обработки 410 Gone
Аналогично багу 2, в коде нет "пасхалки" (hardcoded ID) для проверки статуса 410 (случай, когда слот был снят с публикации в момент бронирования).

### 🟡 Недочет: Неполнота QA-гайда
В `mock-server-qa-guide.md` описаны только 3 пасхалки (409 SLOT_FULL, 409 EQUIPMENT_OUT и 200 OK). Нет упоминания о том, как QA-инженерам вызывать 410 и 429 ошибки для тестирования UI-отклика.

---

## 🛠 Предложение по исправлению (Fix Proposal)

Требуется переписать логику эндпоинта POST `/bookings` в `MockApiHandler.kt`, чтобы устранить баг идемпотентности и добавить обработку статусов 410 и 429:

```kotlin
// Bookings
url.endsWith("/bookings") && method == HttpMethod.Post -> {
    val idempotencyKey = request.headers["Idempotency-Key"]
    val bodyString = request.body.toByteArray().decodeToString()
    
    // Определяем "Пасхалку" на основе тела запроса
    val slotId = when {
        bodyString.contains("slot-full-id") -> "slot-full-id"
        bodyString.contains("slot-no-equipment-id") -> "slot-no-equipment-id"
        bodyString.contains("slot-gone-id") -> "slot-gone-id"
        bodyString.contains("slot-429-id") -> "slot-429-id"
        else -> "regular-id"
    }

    // Сохраняем ключ (для мока нам достаточно просто игнорировать повторную бизнес-логику)
    if (idempotencyKey != null) {
        seenKeys.add(idempotencyKey)
    }

    when (slotId) {
        "slot-full-id" -> respond(
            content = ByteReadChannel("""{"code": "SLOT_FULL", "message": "Нет свободных мест на данный слот"}"""),
            status = HttpStatusCode.Conflict,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
        "slot-no-equipment-id" -> respond(
            content = ByteReadChannel("""{"code": "EQUIPMENT_OUT", "message": "Оборудование для данного слота закончилось"}"""),
            status = HttpStatusCode.Conflict,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
        "slot-gone-id" -> respond(
            content = ByteReadChannel("""{"code": "SLOT_GONE", "message": "Данный слот больше недоступен"}"""),
            status = HttpStatusCode.Gone,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
        "slot-429-id" -> respond(
            content = ByteReadChannel("""{"code": "TOO_MANY_REQUESTS", "message": "Слишком много запросов. Повторите позже."}"""),
            status = HttpStatusCode.TooManyRequests,
            headers = headersOf(
                HttpHeaders.ContentType to listOf("application/json"),
                HttpHeaders.RetryAfter to listOf("30") // <-- Требуемый заголовок
            )
        )
        else -> respond(
            content = ByteReadChannel("""
                {
                    "id": "b0b8a211-0000-0000-0000-book10000001",
                    "status": "ACTIVE",
                    "paymentUrl": null
                }
            """.trimIndent()),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
}
```

Также потребуется:
1. Добавить объекты с `id: "slot-gone-id"` и `id: "slot-429-id"` в список отдачи GET `/slots`, чтобы тестировщики могли кликнуть на них в UI (в `MockApiHandler.kt`).
2. Дописать инструкции по вызову 410 и 429 ошибок в документ `development/mock-server-qa-guide.md`.

**Вердикт:** Приложение пока **НЕ готово** к передаче ручным тестировщикам.
