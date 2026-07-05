# [TASK-004 / FEATURE-PROFILE]: Реализация экрана профиля и истории активных записей (SCR-006, SCR-007, BS-004)

## 1. Контекст и Цель

- **Симптом или цель:** Реализовать фичу 3.4: Профиль и активные записи. Создание экрана профиля с отображением личных данных пользователя, вывод списка его бронирований (с разделением на активные и прошедшие), детализация предстоящей брони и функция отмены существующей записи, а также оценка шефа.
- **Начальное состояние:** Подготовлена базовая инфраструктура (core/network, core/storage, core/mvi), реализованы авторизация (TASK-001), каталог слотов (TASK-002) и процесс бронирования (TASK-003). Модуль профиля и списка броней клиента отсутствует.

## 2. Анализ Требований и Ссылки

- **Связанные требования:** SCR-006 (Профиль и Мои записи), SCR-007 (Детали активной брони), BS-004 (Оценка шефа). Дополнительно затрагивается интеграция с отменой брони (BS-003) и настройкой аллергий (BS-002).
- **API-контракты:** `getProfile`, `getBookings`, `getSlot`, `rateChef`.
- **Спецификации:** 
  - `analysis/5-mobile-app-spec/SCR-006_profile_and_bookings_LOGIC.md`
  - `analysis/5-mobile-app-spec/SCR-006_profile_and_bookings_SCREEN.md`
  - `analysis/5-mobile-app-spec/SCR-007_upcoming_booking_details_LOGIC.md`
  - `analysis/5-mobile-app-spec/SCR-007_upcoming_booking_details_SCREEN.md`
  - `analysis/5-mobile-app-spec/BS-004_chef_rating_LOGIC.md`
  - `analysis/5-mobile-app-spec/BS-004_chef_rating_SCREEN.md`

## 3. Окружение и Инструменты (Environment)

- **IDE:** Antigravity IDE
- **AI-модель:** Gemini 3.1 Pro (High)
- **Дата реализации:** 2026-07-05

## 4. История промтов (Prompt History)

- **Промт 1**  
```text
Ты — Senior Mobile Developer (Compose Multiplatform / MVI Expert). Твоя задача — с нуля реализовать «Фичу 3.4: Профиль и активные записи» (включая экран профиля SCR-006, список активных/прошедших бронирований SCR-007 и бизнес-логику отмены записи BS-004) строго по ТЗ и на основе уже созданной инфраструктуры.

### ПЕРВЫЙ ШАГ: Инициализация Лога Разработки (Единообразие)
1. Зайди в папку `development/tasks-log/` и изучи файлы журналов `TASK-001_auth_feature.md`, `TASK-002_slots_catalog.md` и `TASK-003_booking_feature.md`. Твоя задача — полностью перенять их стиль изложения, детальность описания изменений и способ оформления истории промтов для сохранения абсолютного единообразия.
2. Скопируй файл `development/tasks-log/_TEMPLATE_TASK.md` в новый файл `development/tasks-log/TASK-004_profile_feature.md` и заполни вводные данные, строго следуя структуре прошлых логов:
   - Название: `TASK-004 / FEATURE-PROFILE: Реализация экрана профиля и истории активных записей (SCR-006, SCR-007, BS-004)`
   - Раздел 1 (Контекст и Цель): Опиши цели (отображение личных данных пользователя, вывод списка его бронирований, функция отмены существующей записи).
   - Раздел 2 (Требования): Укажи файлы спецификаций из папки `5-mobile-app-spec/`, относящиеся к SCR-006 и SCR-007, а также бизнес-правило отмены (BS-004).
   - Раздел 4 (История промтов): Вставь текст данного промта в качестве "Промта 1" (помести его в блок кода ```text с отступом в 4 пробела).

---

### ВТОРОЙ ШАГ: Контекстный онбординг (Обязательное сканирование документов)
Перед написанием кода в обязательном порядке изучи существующие файлы архитектуры и домена:
1. `development/architecture-blueprint.md` и `development/implementation-plan.md` — для понимания слоев данных и структуры MVI.
2. `development/domain-entities.md` и `development/client/domain/models/Models.kt` — для синхронизации моделей Пользователя (User) и Записи (Booking/Reservation).
3. Проанализируй Ktor-клиент в `development/client/core/` для выполнения запросов на получение профиля, истории сессий и отправки запроса `POST/DELETE` на отмену бронирования. Учти обработку токенов авторизации из Secure Storage для авторизованных запросов.
4. Изучи спецификации экранов в папке `analysis/5-mobile-app-spec/`.

---

### ТРЕТИЙ ШАГ: Реализация в коде
Всю кодовую базу фичи реализуй внутри новой директории: `development/client/features/profile/`.
Раздели код внутри фичи согласно архитектуре:
- **Logic слой:** Создай MVI Store/Component. Реализуй загрузку профиля пользователя, разделение списка бронирований на "Активные" и "Прошедшие", а также логику отмены записи (BS-004) с отправкой соответствующего экшена на бэкенд.
- **UI слой:** Напиши декларативные Compose-компоненты для экранов SCR-006 (профиль) и SCR-007 (список записей). Навигация обратно в каталог/авторизацию и показ Snackbar (например, подтверждение успешной отмены или ошибка сети) должны обрабатываться строго через подписку на MVI Effects (`LaunchedEffect`). Подключи фичу к навигатору Voyager.

---

### ЧЕТВЕРТЫЙ ШАГ: Фиксация результатов
После завершения кодинга обнови документы:
1. В `development/implementation-plan.md` отметь соответствующие пункты фичи профиля в Этапе 3 как выполненные (`- [x]`).
2. В логе `development/tasks-log/TASK-004_profile_feature.md` заполни Раздел 3 (модель и дата) и Раздел 5 (Выполненные действия и список созданных файлов), соблюдая стиль предыдущих логов.

Выполни шаги и выведи в чат структуру созданного модуля профиля и описание логики MVI-состояний для экранов SCR-006/SCR-007.
```

- **Промт 2 (Багфикс)**
```text
Ты — Senior Mobile Developer (Compose Multiplatform / MVI). Твой код реализации профиля и активных записей (SCR-006, SCR-007, BS-004) прошел технический аудит, но был ОТКЛОНЕН из-за нескольких критических архитектурных недочетов, связанных с навигацией и синхронизацией стейта.

Твоя задача — провести багфикс и доработать файлы в `development/client/features/profile/`.

### ШАГ 1: Починка навигации в ProfileScreen
Открой файл `development/client/features/profile/ui/ProfileScreen.kt` и исправь обработку `ProfileEffect.NavigateToLogin`:
1. Убери закомментированную заглушку `// navigator.replaceAll(LoginScreen())`.
2. Реализуй реальный переход на экран авторизации (убедись, что импортирован нужный `LoginScreen` из фичи auth). Это критично для функции разлогина и сброса сессии при протухшем токене.

### ШАГ 2: Синхронизация списка записей после отмены
Сейчас при успешной отмене записи на экране `UpcomingBookingDetailsScreen` и возврате (pop) назад, экран `ProfileScreen` не обновляется, и отмененная запись остается висеть в "Активных".
Доработай этот флоу:
1. Так как у Voyager по умолчанию нет удобного коллбэка `onResume`, реализуй механизм обновления. Можешь использовать `LifecycleEffect` от Voyager, чтобы при возвращении фокуса на экран профиля вызывался `screenModel.handleIntent(ProfileIntent.RefreshData)`, либо внедри межэкранную передачу результатов (Result API / SharedFlow Event Bus).
2. Опционально: добавь базовую поддержку Pull-to-Refresh в UI `ProfileScreen` (или хотя бы кнопку обновления), чтобы пользователь мог обновить список записей вручную.

### ШАГ 3: Гранулярная обработка 401 ошибки
В `ProfileScreenModel` и `UpcomingBookingScreenModel` в блоках `catch (e: Exception)` перехватываются абсолютно все ошибки, после чего показывается снекбар.
1. Добавь проверку типа ошибки. Если глобальный перехватчик сетевого слоя (Ktor) прокидывает кастомную ошибку об истечении сессии (например, `UnauthorizedException` или аналог при 401), отлавливай ее отдельно.
2. При перехвате ошибки авторизации `ProfileScreenModel` должен эмитить `ProfileEffect.NavigateToLogin`, а не просто показывать снекбар.

### ШАГ 4: Логирование багфикса
Занеси эти исправления в наш лог: `development/tasks-log/TASK-004_profile_feature.md`. 
1. В историю промтов (Раздел 4) добавь текст этого задания как "Промт 2 (Багфикс)".
2. Допиши в раздел 5 ("Реализация и Изменения") новый пункт о том, что был проведен багфикс по результатам QA-ревью (раскомментирована навигация на LoginScreen, добавлено обновление стейта при возврате на экран профиля, улучшена обработка сетевых ошибок).

Приступай к работе! Как закончишь, выведи список обновленных файлов.

```

## 5. Реализация и Изменения

- **Выполненные действия:** 
  - Создана файловая структура фичи профиля `features/profile/`.
  - Реализованы data-слой (ProfileDtos, ProfileApi, ProfileRepository).
  - Реализован MVI-компонент (ProfileScreenModel) с логикой загрузки профиля, списка записей и их разделением на "Активные" и "Прошедшие".
  - Реализован MVI-компонент (UpcomingBookingScreenModel) для экрана деталей активной брони SCR-007 с возможностью оценки шефа (BS-004) и отмены записи.
  - Написаны UI компоненты на Compose: ProfileScreen (SCR-006), UpcomingBookingDetailsScreen (SCR-007), ChefRatingBottomSheet (BS-004).
  - Реализована обработка MVI Effects через LaunchedEffect (вызов Snackbar, диалогов, навигация).
  - Настроен DI-модуль `ProfileModule.kt`.
  - Обновлен план реализации `development/implementation-plan.md`.
  - **Багфикс (Промт 2):** Раскомментирована навигация на `LoginScreen` в `ProfileScreen` и `UpcomingBookingDetailsScreen`. Добавлено обновление стейта при возврате на экран профиля через `LifecycleEffect(onStarted)`. В `ProfileScreenModel` и `UpcomingBookingScreenModel` улучшена обработка сетевых ошибок: явно перехватывается `UnauthorizedException` с последующим вызовом эффекта `NavigateToLogin`.
- **Затронутые файлы:**
  - `development/tasks-log/TASK-004_profile_feature.md`
  - `development/client/features/profile/data/dto/ProfileDtos.kt`
  - `development/client/features/profile/data/ProfileApi.kt`
  - `development/client/features/profile/data/ProfileRepository.kt`
  - `development/client/features/profile/presentation/ProfileScreenModel.kt`
  - `development/client/features/profile/presentation/UpcomingBookingScreenModel.kt`
  - `development/client/features/profile/ui/ProfileScreen.kt`
  - `development/client/features/profile/ui/UpcomingBookingDetailsScreen.kt`
  - `development/client/features/profile/ui/ChefRatingBottomSheet.kt`
  - `development/client/features/profile/di/ProfileModule.kt`
  - `development/implementation-plan.md`

## Дополнительные заметки

Модуль профиля поддерживает разделение списка бронирований на активные и прошедшие. Используется кэширование и получение слота для броней. Оценка шефа и отмена брони также интегрированы в общий MVI поток.
