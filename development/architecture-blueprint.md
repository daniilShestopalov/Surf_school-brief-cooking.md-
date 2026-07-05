# Архитектурное решение (Architecture Blueprint)

Проект: Клиентское мобильное приложение кулинарной студии «Шеф-стол».
Подход: Кроссплатформенная разработка (Kotlin Multiplatform / Compose Multiplatform).

---

## 1. Обоснование и выбор технологического стека

Для реализации MVP с прицелом на масштабируемость выбран стек, обеспечивающий максимальный процент переиспользования кода (до 90% общей кодовой базы на iOS и Android).

- **Dependency Injection (DI):** **Koin**. Легковесный Service Locator/DI, не требующий кодогенерации (в отличие от Dagger/Hilt), что критически важно для ускорения сборки в KMP. Отлично интегрируется со StateHolder-ами Compose.
- **Управление состоянием (State Management):** **MVI (Model-View-Intent)** на базе KMP ViewModel (или `ScreenModel` из Voyager). MVI идеально подходит для шторки бронирования (BS-001) и сложной навигации, так как обеспечивает строгий однонаправленный поток данных (UDF) и предсказуемость стейта при расчете цен.
- **Навигация:** **Voyager**. Легковесная и мощная библиотека навигации, спроектированная специально для Compose Multiplatform. Поддерживает вложенные графы и BottomSheet-навигацию "из коробки" без привязки к тяжелому Android Lifecycle.
- **Сетевое решение:** **Ktor Client** + `kotlinx.serialization`. Де-факто стандарт в KMP. Позволяет гибко настраивать плагины авторизации (Auth) и глобальные перехватчики ответов (HttpResponseValidator) для обработки OpenAPI контрактов.
- **Безопасное хранилище:** **Multiplatform Settings** с шифрованием (через EncryptedSharedPreferences на Android и Keychain на iOS). Используется для безопасного хранения Bearer-токена сессии.
- **Даты и время:** **kotlinx-datetime**. Универсальная библиотека для расчетов таймзонов, парсинга Timestamp из API и вычисления таймаута оплаты (1 час).
- **Медиа-контент:** **Coil 3**. Библиотека асинхронной загрузки изображений (шефов, программ), получившая полную поддержку Compose Multiplatform.
- **Интеграция карт:** Паттерн **expect / actual**. Карты выносятся в отдельный изолированный модуль `core/maps`. Общий UI работает с Composable-интерфейсом `MapView(address, lat, lng)`, а под капотом (actual) платформа рендерит нативный элемент: `AndroidView` (Google Maps/Yandex) или `UIKitView` (MapKit).

---

## 2. Модульная структура проекта (Folder Structure)

Структура `shared/src/commonMain/kotlin/` базируется на Feature-Driven Development (FDD) в сочетании с Clean Architecture:

```text
├── core/                        # Инфраструктура и платформенные сервисы
│   ├── network/                 # Ktor клиент, интерсепторы, обработка HTTP-ошибок
│   ├── storage/                 # Secure Storage для токенов
│   ├── navigation/              # Глобальные графы навигации Voyager
│   ├── di/                      # Общие Koin-модули
│   ├── designsystem/            # Токены (цвета, шрифты), базовые UI-компоненты
│   └── maps/                    # expect/actual абстракция для карт
├── domain/                      # Бизнес-логика (не зависит от UI и фреймворков)
│   ├── models/                  # Доменные сущности (Client, Slot, Booking и др.)
│   ├── exceptions/              # Бизнес-ошибки (SlotFullException, EquipmentOutException)
│   └── repositories/            # Интерфейсы репозиториев
└── features/                    # Изолированные фичи (Экраны)
    ├── auth/                    # SCR-001 (Вход), SCR-002 (Регистрация)
    ├── schedule/                # SCR-003 (Расписание), SCR-004 (Детали слота)
    ├── booking/                 # BS-001 (Оформление), SCR-005 (Оплата), BS-003 (Отмена)
    └── profile/                 # SCR-006 (Мои записи), SCR-007 (Детали), BS-004 (Оценка)
        ├── ui/                  # Compose экраны (Screen)
        ├── presentation/        # MVI: ScreenModel, State, Intent, Effect
        └── data/                # Реализация репозиториев и мапперы DTO -> Domain
```

---

## 3. Спецификация каркаса экранов (State Management Blueprint)

Каждый экран (Feature) реализует строгий паттерн UDF. Архитектура стейта состоит из 3 компонентов:

```kotlin
// 1. Data State (Состояние UI) - Хранится в StateFlow
// Примечание: все денежные значения (slotBasePrice, equipmentTariff, totalPrice) хранятся и обрабатываются в копейках согласно доменной модели.
data class BookingState(
    val isLoading: Boolean = false,
    val slotBasePrice: Int = 0,
    val equipmentTariff: Int = 0,
    val isEquipmentAvailable: Boolean = false,
    val isEquipmentChecked: Boolean = false,
    val totalPrice: Int = 0 // Пересчитывается динамически (slotBasePrice + equipmentTariff)
)

// 2. User Actions (Намерения пользователя) - Входящие события
sealed interface BookingIntent {
    data class ToggleEquipment(val isChecked: Boolean) : BookingIntent
    object ConfirmBooking : BookingIntent
    object ConfirmWithoutEquipment : BookingIntent // Действие при согласии на бронь без инвентаря (обработка 409)
}

// 3. One-off Events (Сайд-эффекты) - Хранится в SharedFlow
sealed interface BookingEffect {
    data class NavigateToPayment(val bookingId: String, val expiresAt: Long) : BookingEffect
    data class ShowErrorSnackbar(val message: String) : BookingEffect
    object ShowSlotFullDialog : BookingEffect
    object AskToProceedWithoutEquipment : BookingEffect // Показ Action-снекбара при 409 EQUIPMENT_OUT
}
```

**Жизненный цикл:** View (Compose Screen) прокидывает `Intent` в `ScreenModel`. `ScreenModel` исполняет бизнес-логику (через UseCase), мутирует иммутабельный `State` и при необходимости эмитит одноразовый `Effect` (например, для навигации или снекбара).

---

## 4. Проектирование Core-сервисов под задачи MVP

### 4.1. Сетевая обертка (Network Wrapper) и Обработка Ошибок
Вся работа с сетью инкапсулирована в `Ktor HttpClient` с использованием плагинов:
- **Глобальный перехват `401 Unauthorized`:** Реализуется через `HttpResponseValidator`. При получении статуса 401 очищается Secure Storage с токенами, и через глобальную шину событий (Global Event Bus) триггерится безусловный сброс стека навигации до экрана авторизации (SCR-001).
- **Обработка `429 Too Many Requests`:** Перехватчик анализирует заголовок `Retry-After`. Ошибка мапится в кастомное исключение `RateLimitException`. В UI (например, при запросе OTP) это приводит к блокировке кнопки отправки на указанное время.
- **Транспортные сбои (Timeout/Offline):** Ошибки `IOException` и таймауты перехватываются на уровне Data-слоя и пробрасываются в Presentation-слой как доменное `NetworkException`. UI реагирует показом снекбара "Отсутствует подключение".

### 4.2. Провайдер идемпотентности (Idempotency Provider)
Для надежного создания бронирований (POST `/bookings`) и защиты от гонок и двойных списаний критически важно правильно генерировать `Idempotency-Key` (UUIDv4).

- **Архитектурное правило:** Ключ генерируется **на слое Presentation (в `BookingScreenModel`)**, а НЕ в сетевом интерсепторе Ktor. 
- **Жизненный цикл ключа:** Ключ создается один раз в момент открытия шторки бронирования (BS-001).
- **Сценарий Retry:** Если пользователь нажимает "Подтвердить", но происходит сбой сети (Timeout), бэкенд, возможно, успел обработать запрос. Пользователь видит ошибку и снова нажимает "Повторить". Так как ключ хранится в стейте `ScreenModel`, приложение отправит **тот же самый `Idempotency-Key`**. Бэкенд распознает дубль, не создаст вторую бронь и не спишет двойную стоимость, а просто вернет успешный результат первой операции.
- **Транспортный контракт:** Сгенерированный в `ScreenModel` UUID передается аргументом через методы доменного слоя (`UseCase` -> `Repository`) в слой Network, где Ktor-клиент обязан прикрепить его к POST-запросу в виде HTTP-заголовка `Idempotency-Key`.
