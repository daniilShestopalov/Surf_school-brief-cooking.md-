# [TASK-001 / FEATURE-AUTH]: Реализация фичи авторизации (SCR-001, SCR-002)

## 1. Контекст и Цель

- **Симптом или цель:** Реализовать фичу 3.1: Авторизация. Создание экранов ввода телефона (SCR-001) и подтверждения OTP (SCR-002), отправка OTP, сохранение токенов в Secure Storage, обработка ошибок.
- **Начальное состояние:** Подготовлена базовая инфраструктура (core/network, core/storage, core/mvi), фича не реализована.

## 2. Анализ Требований и Ссылки

- **Связанные требования:** SCR-001 (Вход), SCR-002 (Регистрация).
- **API-контракты:** `sendCode`, `verifyCode`, `updateProfile`.
- **Спецификации:** 
  - `analysis/5-mobile-app-spec/SCR-001_login_LOGIC.md`
  - `analysis/5-mobile-app-spec/SCR-001_login_SCREEN.md`
  - `analysis/5-mobile-app-spec/SCR-002_registration_LOGIC.md`
  - `analysis/5-mobile-app-spec/SCR-002_registration_SCREEN.md`

## 3. Окружение и Инструменты (Environment)

- **IDE:** Antigravity IDE
- **AI-модель:** Gemini 3.1 Pro (High)
- **Дата реализации:** 2026-07-05

## 4. История промтов (Prompt History)

- **Промт 1**  
```text
Ты — Senior Mobile Developer (Compose Multiplatform / MVI Expert). Твоя задача — с нуля реализовать «Фичу 3.1: Авторизация» (включая экраны ввода телефона SCR-001 и подтверждения OTP-кода SCR-002) строго по ТЗ и на основе уже созданной инфраструктуры.

### ПЕРВЫЙ ШАГ: Инициализация Лога Разработки
Прежде чем писать код, скопируй файл `development/tasks-log/_TEMPLATE_TASK.md` в новый файл `development/tasks-log/TASK-001_auth_feature.md` и заполни вводные данные:
- Название: `TASK-001 / FEATURE-AUTH: Реализация фичи авторизации (SCR-001, SCR-002)`
- Раздел 1 (Контекст и Цель): Опиши симптомы/цели (создание экранов авторизации, отправка OTP, сохранение токенов).
- Раздел 2 (Требования): Укажи файлы спецификаций из папки `5-mobile-app-spec/`, относящиеся к SCR-001 и SCR-002.
- Раздел 4 (История промтов): Вставь текст данного промта в качестве "Промта 1".

---

### ВТОРОЙ ШАГ: Контекстный онбординг (Сканирование репозитория)
Внимательно изучи существующие файлы (структура папок приведена на image_365fc9.png), чтобы не дублировать код и использовать готовые компоненты:
1. `development/architecture-blueprint.md` и `development/implementation-plan.md` — для понимания архитектурных слоев и структуры MVI (State, Intent, Effect).
2. `development/domain-entities.md` и `development/client/domain/models/Models.kt` — для синхронизации моделей данных пользователя и сессии.
3. Проанализируй файлы в `development/client/core/` (особенно сетевой Ktor-клиент и модуль DI на Koin), чтобы корректно вызывать API авторизации и обрабатывать токен сессии/ошибки (включая 401 и 429).
4. Изучи спецификации экранов SCR-001 и SCR-002 в папке `analysis/5-mobile-app-spec/`.

---

### ТРЕТИЙ ШАГ: Реализация в коде
Всю кодовую базу фичи реализуй внутри новой директории: `development/client/features/auth/`.
Раздели код внутри фичи согласно архитектуре:
- **Logic слой:** Создай MVI Store/Component для управления состоянием авторизации, валидации ввода, отправки запросов на бэкенд через Ktor и сохранения токена в Secure Storage при успешном входе.
- **UI слой:** Напиши декларативные Compose-компоненты для экранов SCR-001 (ввод телефона) и SCR-002 (ввод OTP). Реализуй отображение состояний: Default State, Loading State, Error State (ошибки валидации, сбои сети). Подключи экраны к общему механизму навигации.

---

### ЧЕТВЕРТЫЙ ШАГ: Фиксация результатов
После завершения кодинга обнови документы:
1. В `development/implementation-plan.md` отметь соответствующие пункты фичи авторизации в Этапе 3 как выполненные (`- [x]`).
2. В логе `development/tasks-log/TASK-001_auth_feature.md` заполни Раздел 3 (укажи модель и дату реализации) и Раздел 5 (Выполненные действия и список затронутых/созданных файлов фичи).

Поехали. Выполни шаги и выведи в чат структуру созданного модуля авторизации и краткое описание логики MVI-состояний для экранов SCR-001/SCR-002.
```

- **Промт 2 (Багфикс)**
```text
Ты — Senior Mobile Developer (Compose Multiplatform / MVI). Твой код реализации авторизации (SCR-001, SCR-002) прошел QA-аудит, но был ОТКЛОНЕН из-за критического архитектурного бага в UI-слое.

В экранах на Compose полностью отсутствует обработка MVI Effects (Side Effects), из-за чего навигация и показ ошибок при сетевых сбоях не работают (сейчас там оставлена просто заглушка-комментарий).

Твоя задача — провести багфикс и доработать экраны в `development/client/features/auth/`. 

### ШАГ 1: Интеграция Effects в LoginScreen.kt
Открой файл `development/client/features/auth/ui/LoginScreen.kt` и реализуй полноценную обработку `LoginEffect`:
1. Получи инстанс навигатора Voyager (через `LocalNavigator.currentOrThrow`).
2. Добавь `SnackbarHostState` для вывода ошибок и интегрируй его в верстку экрана (через `Scaffold` или поверх экрана).
3. Используя `LaunchedEffect(screenModel)`, подпишись на `screenModel.effect`.
4. Обработай приходящие эффекты:
   - `NavigateToRegistration` -> используй `navigator.push(RegistrationScreen())`
   - `NavigateToSchedule` -> используй `navigator.replaceAll(...)` (если экрана Schedule еще нет, создай временную заглушку `ScheduleScreen`).
   - `ShowErrorSnackbar` -> выводи переданное сообщение в Snackbar.

### ШАГ 2: Интеграция Effects в RegistrationScreen.kt
Открой файл `development/client/features/auth/ui/RegistrationScreen.kt` и сделай аналогичную доработку для `RegistrationEffect`:
1. Добавь навигатор, снекбар и подписку через `LaunchedEffect`.
2. Обработай `NavigateToSchedule` и `ShowErrorSnackbar`.

### ШАГ 3: Рефакторинг хардкода
Открой файл `development/client/features/auth/presentation/LoginScreenModel.kt`. 
В методе `handleOtpChanged` есть хардкод длины OTP-кода (`if (code.length == 4)`). Вынеси магическое число `4` в константу компаньона (например, `private const val OTP_LENGTH = 4`).

### ШАГ 4: Логирование
Занеси эти исправления в наш лог: `development/tasks-log/TASK-001_auth_feature.md`. 
Допиши в раздел 5 ("Реализация и Изменения") новый пункт о том, что был проведен багфикс по результатам QA-ревью (добавлена обработка Effects через LaunchedEffect и SnackbarHostState в UI-слое). В историю промтов (Раздел 4) добавь этот промт как "Промт 2 (Багфикс)".

Приступай к работе! Как закончишь, выведи список обновленных файлов.
 
```

## 5. Реализация и Изменения

- **Выполненные действия:** 
  - Создана файловая структура фичи авторизации `features/auth/`.
  - Реализованы data-слой (модели запросов/ответов, AuthApi, AuthRepository).
  - Реализованы MVI-компоненты (LoginScreenModel, RegistrationScreenModel) с логикой стейта, валидации и сетевых вызовов (с обработкой `429 Too Many Requests` и `401 Unauthorized`).
  - Реализован UI на Compose (LoginScreen, RegistrationScreen) с обработкой Default, Loading, Error состояний.
  - Настроен DI-модуль `AuthModule.kt`.
  - Обновлен план реализации (`implementation-plan.md`).
  - Проведен багфикс по результатам QA-ревью: добавлена обработка Effects через LaunchedEffect и SnackbarHostState в UI-слое (LoginScreen, RegistrationScreen), а также создана заглушка ScheduleScreen. В LoginScreenModel вынесен хардкод длины OTP.
- **Затронутые файлы:**
  - `development/client/features/auth/data/dto/AuthDtos.kt`
  - `development/client/features/auth/data/AuthApi.kt`
  - `development/client/features/auth/data/AuthRepository.kt`
  - `development/client/features/auth/presentation/LoginScreenModel.kt`
  - `development/client/features/auth/presentation/RegistrationScreenModel.kt`
  - `development/client/features/auth/ui/LoginScreen.kt`
  - `development/client/features/auth/ui/RegistrationScreen.kt`
  - `development/client/features/auth/di/AuthModule.kt`
  - `development/implementation-plan.md`

## Дополнительные заметки
Фича авторизации полностью покрывает спецификации экранов SCR-001 и SCR-002, использует единый Ktor клиент из core-модуля и работает поверх MVI архитектуры.
