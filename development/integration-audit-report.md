# Глобальный интеграционный аудит (development/client/)

## 1. Итоговая оценка интеграции
🔴 **ЗАБЛОКИРОВАН (Критические разрывы в навигации, DI и безопасности)**

Несмотря на то, что отдельные экраны и фичи реализованы согласно спецификациям и паттерну MVI, сквозная сборка приложения невозможна из-за отсутствия связующего слоя (Root Screen/App.kt), критических разрывов в навигационном графе между изолированными модулями и отсутствия прослушивания глобальных событий сессии. 

## 2. Карта сквозной навигации
**Статус графа:** **СЛОМАН НА СТЫКАХ ФИЧ**

*   **Auth (Авторизация) -> Schedule (Расписание):** Переход `navigator.replaceAll(ScheduleScreen())` ведет на экран-заглушку (Stub: `"Schedule Screen Stub"`). Отсутствует корневой контейнер с нижней панелью навигации (Bottom Navigation), объединяющий Каталог, Расписание и Профиль.
*   **Slots (Каталог) -> Booking (Бронирование):** Переход разорван. В `SlotDetailsScreen` закомментированы вызовы `navigator.push(BookingScreen(effect.slotId))` и `navigator.push(UpcomingBookingDetailsScreen())`. Пользователь не может забронировать слот из интерфейса.
*   **Profile (Профиль) -> Booking (Оплата):** Переход разорван. В `UpcomingBookingDetailsScreen` закомментирован вызов `navigator.push(PaymentDetailsScreen(...))`. Пользователь не может перейти к оплате после оформления.

## 3. Реестр глобальных нестыковок (Global Integration Issues)

| Подсистема | Проблема | Описание | Критичность |
| :--- | :--- | :--- | :--- |
| **DI (Koin)** | Отсутствие точки входа и `startKoin` | Модули фич (`authModule`, `slotsModule` и др.) и `coreModule` существуют, но нигде не объединяются и не инициализируются. Отсутствует `Application.kt` или функция `initKoin()`. Сборка приложения завершится крэшем при попытке резолва первой зависимости. | **High** |
| **Security / Network** | Потеря глобального `401 Unauthorized` | В `KtorClient` реализован перехватчик `401`, который делает `globalNavigationEvents.tryEmit(NavigateToAuth)`. Однако в `AppNavigation.kt` этот `SharedFlow` **не прослушивается**. Сессия будет сброшена в хранилище, но пользователь останется на текущем экране, получая локальные ошибки. | **High** |
| **Navigation** | Отсутствие Root-контейнера | Нет экрана, реализующего `BottomNavigation` (Табы: Слоты, Расписание, Профиль). `ScheduleScreen` реализован в виде пустой заглушки в 19 строк. | **High** |
| **Navigation** | Изоляция фич (Закомментированные переходы) | Фичи разрабатывались изолированно, кросс-модульные переходы (Слоты -> Бронирование -> Профиль) остались закомментированными во избежание ошибок компиляции до завершения всех фич. | **Medium** |
| **Shared State** | Десинхронизация данных между фичами | Отсутствует реактивный кэш на уровне Data/Domain слоя (репозиториев). Если в `Profile` отменяется бронь (`UpcomingBookingIntent.CancelBooking`), экран `SlotsCatalog` не получит об этом уведомления, и его `StateFlow` продолжит показывать устаревший статус места или статус брони, пока пользователь не перезайдет на экран. | **Medium** |

## 4. Action Plan по стабилизации (Для следующего агента)

Для подготовки клиента к релизу, следующему агенту необходимо выполнить следующие шаги:

1.  **Создать точку входа приложения и инициализировать Koin:**
    *   Создать `development/client/App.kt` (или аналог для платформы).
    *   Собрать `startKoin { modules(coreModule, authModule, slotsModule, bookingModule, profileModule) }`.
2.  **Реализовать Root Screen с Bottom Navigation:**
    *   Заменить `AppNavigation.kt` или добавить `RootScreen` с вкладками.
    *   Реализовать полноценный `ScheduleScreen` или временно удалить навигацию на него, оставив табы Каталога и Профиля.
3.  **Обработать глобальные события навигации (401 Unauthorized):**
    *   В `AppNavigation.kt` (или Root-компоненте) добавить `LaunchedEffect` для сбора `globalNavigationEvents`.
    *   При получении `NavigateToAuth` вызывать `navigator.replaceAll(LoginScreen())`.
4.  **Восстановить кросс-модульную навигацию:**
    *   Раскомментировать и правильно связать переходы в `SlotDetailsScreen` и `UpcomingBookingDetailsScreen`.
5.  **Внедрить Reactive State в Репозитории:**
    *   Перевести репозитории (или создать `BookingSharedState`) на использование `MutableStateFlow` или `Flow` для кэширования, чтобы отмена записи в Профиле реактивно обновляла интерфейс в Каталоге слотов.

## 5. Результаты стабилизации (System Stabilizer)
Все критические уязвимости и разрывы, выявленные в ходе аудита, успешно устранены:

1.  **Точка входа и DI:** Создан корневой файл `development/client/App.kt` с инициализацией `KoinApplication`, где подключаются модули всех фич (`coreModule`, `authModule`, `slotsModule`, `bookingModule`, `profileModule`).
2.  **Корневой контейнер навигации (Root Screen):** Реализован `RootScreen.kt` с использованием `TabNavigator` и `BottomNavigation`, объединяющий `SlotsCatalogScreen`, `ScheduleScreen` и `ProfileScreen`. `LoginScreen` теперь корректно ведет на `RootScreen` после успешной авторизации.
3.  **Глобальная безопасность (401 Handling):** В `AppNavigation.kt` добавлен `LaunchedEffect`, который собирает события из `globalNavigationEvents`. При получении `NavigateToAuth` происходит `navigator.replaceAll(LoginScreen())`, полностью очищая стек сессии.
4.  **Кросс-модульная навигация:** Восстановлены "битые" ссылки. В `SlotDetailsScreen` добавлен переход на `BookingConfirmationBottomSheet` с передачей объекта `Slot`. В `UpcomingBookingDetailsScreen` раскомментирован переход к оплате `PaymentDetailsScreen`.
5.  **Синхронизация состояний:** Создан `BookingEvents.bookingCancelled` (на базе `MutableSharedFlow`). `ProfileRepository` отправляет событие при отмене записи, а `SlotsCatalogStore` слушает его и реактивно перезагружает актуальное расписание.
