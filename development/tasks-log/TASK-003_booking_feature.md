# [TASK-003 / FEATURE-BOOKING]: Реализация логики и экранов бронирования (BS-001, BS-002, BS-003, SCR-005)

## 1. Контекст и Цель

- **Симптом или цель:** Реализовать Фичу 3.3: Бронирование. Необходимо создать процесс бронирования слота (BS-001), обработку конфликтов и ошибок доступности слотов (409, 410), экран успешного финала с реквизитами на оплату (SCR-005), а также сопутствующие шторки (BS-002, BS-003).
- **Начальное состояние:** Настроена базовая инфраструктура (core, network, storage, mvi, navigation, IdempotencyProvider). Фича бронирования не реализована.

## 2. Анализ Требований и Ссылки

- **Связанные требования:** BS-001 (Оформление), BS-002 (Аллергии), BS-003 (Отмена), SCR-005 (Подтверждение/Статус).
- **Спецификации:** 
  - `analysis/5-mobile-app-spec/BS-001_booking_confirmation_LOGIC.md`
  - `analysis/5-mobile-app-spec/BS-001_booking_confirmation_SCREEN.md`
  - `analysis/5-mobile-app-spec/BS-002_allergies_selection_LOGIC.md`
  - `analysis/5-mobile-app-spec/BS-002_allergies_selection_SCREEN.md`
  - `analysis/5-mobile-app-spec/BS-003_cancellation_modal_LOGIC.md`
  - `analysis/5-mobile-app-spec/BS-003_cancellation_modal_SCREEN.md`
  - `analysis/5-mobile-app-spec/SCR-005_payment_details_LOGIC.md`
  - `analysis/5-mobile-app-spec/SCR-005_payment_details_SCREEN.md`

## 3. Окружение и Инструменты (Environment)

- **IDE:** Antigravity IDE
- **AI-модель:** Gemini 3.1 Pro (High)
- **Дата реализации:** 2026-07-05

## 4. История промтов (Prompt History)

- **Промт 1**  
```text
Ты — Senior Mobile Developer (Compose Multiplatform / MVI Expert). Твоя задача — с нуля реализовать «Фичу 3.3: Бронирование» (включая создание бронирования BS-001, обработку конфликтов BS-002/BS-003 и экран подтверждения/статуса SCR-005) строго по ТЗ и на основе уже созданной инфраструктуры.

### ПЕРВЫЙ ШАГ: Инициализация Лога Разработки (Единообразие)
1. Зайди в папку `development/tasks-log/` и изучи уже заполненные файлы `TASK-001_auth_feature.md` и `TASK-002_slots_catalog.md`. Обрати внимание на стиль изложения, детальность описания измененных файлов и способ фиксации истории промтов.
2. Скопируй файл `development/tasks-log/_TEMPLATE_TASK.md` в новый файл `development/tasks-log/TASK-003_booking_feature.md` и заполни вводные данные, строго сохраняя обнаруженное единообразие:
   - Название: `TASK-003 / FEATURE-BOOKING: Реализация логики и экранов бронирования (BS-001, BS-002, BS-003, SCR-005)`
   - Раздел 1 (Контекст и Цель): Опиши бизнес-цель (процесс создания бронирования, обработка ошибок доступности слотов, экран успешного финала).
   - Раздел 2 (Требования): Укажи файлы спецификаций из папки `5-mobile-app-spec/`, относящиеся к SCR-005, а также связанные требования/User Stories (BS-001, BS-002, BS-003).
   - Раздел 4 (История промтов): Вставь текст данного промта в качестве "Промта 1" (помести его в блок кода ```text с отступом в 4 пробела).

---

### ВТОРОЙ ШАГ: Контекстный онбординг (Сканирование репозитория)
Внимательно изучи существующие файлы, чтобы переиспользовать инфраструктуру и доменные модели:
1. `development/architecture-blueprint.md` и `development/implementation-plan.md` — для понимания слоев и структуры MVI.
2. `development/domain-entities.md` и `development/client/domain/models/Models.kt` — для синхронизации моделей бронирования. Учти, что денежные поля (цены) должны обрабатываться в целочисленном формате (в копейках).
3. Проанализируй Ktor-клиент в `development/client/core/`. Тебе необходимо заложить корректный перехват специфичных статус-кодов ответов бэкенда: `201 Created` (успех), `409 Conflict` (слот уже занят) и `410 Gone` (слот устарел/недоступен).
4. Изучи спецификации в папке `analysis/5-mobile-app-spec/`.

---

### ТРЕТИЙ ШАГ: Реализация в коде
Всю кодовую базу фичи реализуй внутри новой директории: `development/client/features/booking/`.
Раздели код внутри фичи согласно архитектуре:
- **Logic слой:** Создай MVI Store/Component. Реализуй отправку запроса на бронирование, генерацию UUID для заголовков идемпотентности (используй core-провайдер), а также обработку ошибок 409 и 410.
- **UI слой:** Напиши декларативные Compose-компоненты для экрана SCR-005 и сопутствующих оверлеев/шторок для вывода ошибок конфликта бронирования. Навигация и отображение Snackbar/Диалогов должны обрабатываться строго через подписку на MVI Effects (`LaunchedEffect`). Подключи фичу к навигатору Voyager.

---

### ЧЕТВЕРТЫЙ ШАГ: Фиксация результатов
После завершения кодинга обнови документы:
1. В `development/implementation-plan.md` отметь соответствующие пункты фичи бронирования в Этапе 3 как выполненные (`- [x]`).
2. В логе `development/tasks-log/TASK-003_booking_feature.md` заполни Раздел 3 (модель и дата) и Раздел 5 (Выполненные действия и список созданных файлов), соблюдая стиль предыдущих логов.

Поехали. Выполни шаги и выведи в чат структуру созданного модуля бронирования и краткое описание логики обработки ответов API (201, 409, 410).
```

- **Промт 2 (Багфикс)**  
```text
Ты — Senior Mobile Developer. Тебе необходимо внести точечные исправления в Фичу 3.3 (Бронирование) по результатам код-ревью. Инфраструктура идемпотентности реализована отлично, но есть пара недочетов в логике обработки ошибок 409 и 410.

Пожалуйста, выполни следующие шаги:

### 1. Исправление парсинга тела 409 ошибки (Критично)
В файле `development/client/features/booking/data/BookingRepository.kt` при обработке `ClientRequestException` (когда `status == HttpStatusCode.Conflict`) ты прокидываешь в `ConflictException` значение `e.response.status.description`. Это возвращает системную строку "Conflict", а не бизнес-код ошибки от бэкенда.
- **Что нужно сделать:** Считывай тело ответа (body) бэкенда при перехвате исключения. Тебе нужно десериализовать его (например, в DTO ошибки) или распарсить JSON, чтобы достать бизнес-код (ожидаются коды `SLOT_FULL` или `EQUIPMENT_OUT`). Передай именно этот код в `ConflictException`.
- Затем обнови `development/client/features/booking/presentation/BookingScreenModel.kt`: вместо проверки `e.message?.contains("equipment")` проверяй точный бизнес-код ошибки, чтобы корректно вызывать нужный Effect (`AskToProceedWithoutEquipment` или `ShowSlotFullDialog`).

### 2. Изменение реакции UI на 410 Gone (Минорно)
В текущей реализации при ошибке `410 Gone` вызывается `ShowErrorSnackbar`. Согласно спецификациям, конфликты должны явно блокировать UI. 
- **Что нужно сделать:** В `BookingScreenModel.kt` замени вызов снэкбара на новый Effect (например, `ShowSlotGoneDialog`). 
- В `BookingConfirmationBottomSheet.kt` добавь обработку этого эффекта: покажи `AlertDialog` с текстом "Слот устарел или больше недоступен" и кнопкой "Понятно", по нажатию на которую происходит возврат назад по стеку навигации (`navigator.pop()`).

### 3. Фиксация изменений
После внесения правок обнови раздел "Реализация и Изменения" в логе `development/tasks-log/TASK-003_booking_feature.md`, добавив пункт о фиксе парсинга тела HTTP-ошибок и обновлении диалога для 410 статуса.

Выведи в чат измененный фрагмент `BookingRepository.kt` с чтением тела ответа и подтверди, что все исправлено.
```

## 5. Реализация и Изменения

- **Выполненные действия:** 
  - Создана файловая структура фичи бронирования `features/booking/`.
  - Реализованы data-слой (BookingDtos, BookingApi, BookingRepository).
  - Реализован MVI-компонент BookingScreenModel с логикой расчета цен, генерации ключа идемпотентности через IdempotencyProvider и отправки запроса с обработкой статусов 201, 409 (Conflict), 410 (Gone).
  - Написаны UI компоненты на Compose: BookingConfirmationBottomSheet, PaymentDetailsScreen (с обратным отсчетом), AllergiesSelectionBottomSheet, CancellationBottomSheet.
  - Реализована обработка MVI Effects через LaunchedEffect (вызов Snackbar, диалогов, навигация).
  - Обновлен план реализации `development/implementation-plan.md`.
  - **Багфикс (Промт 2):** Реализовано чтение `bodyAsText()` для 409 статуса в `BookingRepository.kt` для парсинга бизнес-кодов `SLOT_FULL` и `EQUIPMENT_OUT` через новую модель `ApiErrorResponse`.
  - **Багфикс (Промт 2):** Обновлена реакция на 410 Gone: в `BookingScreenModel.kt` добавлен `ShowSlotGoneDialog` Effect, а в UI отображается `AlertDialog`, блокирующий экран и возвращающий назад.
- **Затронутые файлы:**
  - `development/tasks-log/TASK-003_booking_feature.md`
  - `development/client/features/booking/data/dto/BookingDtos.kt`
  - `development/client/features/booking/data/BookingApi.kt`
  - `development/client/features/booking/data/BookingRepository.kt`
  - `development/client/features/booking/presentation/BookingScreenModel.kt`
  - `development/client/features/booking/ui/BookingConfirmationBottomSheet.kt`
  - `development/client/features/booking/ui/PaymentDetailsScreen.kt`
  - `development/client/features/booking/ui/AllergiesSelectionBottomSheet.kt`
  - `development/client/features/booking/ui/CancellationBottomSheet.kt`
  - `development/client/features/booking/di/BookingModule.kt`
  - `development/implementation-plan.md`

## Дополнительные заметки

Модуль бронирования строго следует архитектуре UDF, корректно передает `Idempotency-Key` при создании запроса и управляет навигацией (BottomSheet/Screen) с помощью Voyager. Денежные суммы рассчитываются в копейках.
