# [BUG-008]: Отсутствие возможности редактирования аллергий в профиле

## 1. Контекст и Цель

- **Симптом или цель:** На экране профиля отображался список аллергий (или "Нет", если их нет), однако не было возможности их изменить или добавить новые, как указано в технических требованиях (FR-25).
- **Начальное состояние:** В `ProfileScreen.kt` отсутствовала кнопка "Редактировать" ("Изменить"), а сам BottomSheet с выбором аллергий (`AllergiesSelectionBottomSheet`) существовал только в модуле `booking` и был заполнен заглушками ("Список аллергенов (TODO)") и не был привязан к профилю.

## 2. Анализ Требований и Ссылки

- **Связанные требования:** Блок "Мои данные" в профиле (SCR-006) должен включать кнопку "Редактировать", открывающую модальную шторку `BS-002` со списком аллергий (Орехи, Молоко, Мед и т.д.).
- **API-контракты:** Добавлен метод `PATCH /profile/allergies` (`updateAllergies`) с параметром `allergy_profile` в виде массива строк, для синхронизации локального состояния с бэкендом/моками.

## 3. Окружение и Инструменты (Environment)

- **IDE:** Antigravity IDE (Claude Code)
- **AI-модель:** Gemini 3.1 Pro (High)
- **Дата реализации:** 2026-07-06

## 4. История промтов (Prompt History)

*Сюда логгируются все отправленные агенту запросы в ходе решения задачи.*

- **Промт 1**  
```text
### ОПИСАНИЕ ПРОБЛЕМЫ & КОНТЕКСТ:
На текущий момент на экране профиля (SCR-006) отображается только статический список аллергий. Отсутствует возможность их изменения или добавления новых. Компонент AllergiesSelectionBottomSheet изолирован в модуле booking, содержит заглушки и никак не связан со слоем данных профиля.

### ЗАДАЧА ДЛЯ АГЕНТА:
1. API & Data Layer: В модуле profile реализовать поддержку эндпоинта `PATCH /profile/allergies`. Создать необходимую DTO-модель запроса (`UpdateAllergiesRequest`) и добавить метод `updateAllergies(allergies: List<String>)` в ProfileApi и ProfileRepository.
2. Presentation Layer: В ProfileScreenModel добавить новый интент (например, `Intent.UpdateAllergies`), который отправляет массив выбранных аллергенов на бэкенд/моки, а после успешного ответа инвалидирует состояние и инициирует повторную загрузку профиля (`loadData()`).
3. UI Layer (Compose): 
   - В `ProfileScreen.kt` добавить кнопку «Изменить» рядом с блоком аллергий.
   - Разработать переиспользуемый компонент шторки `ProfileAllergiesBottomSheet.kt` на базе спецификации BS-002. 
   - Реализовать в шторке список доступных аллергенов с Checkbox-элементами. Состояние чекбоксов должно инициализироваться текущими данными пользователя.
   - Настроить вызов шторки через LocalBottomSheetNavigator и передачу сохраненного списка обратно в ScreenModel.

Обеспечь консистентность MVI-архитектуры и реактивное обновление интерфейса после закрытия шторки.
```

## 5. Реализация и Изменения

- **Выполненные действия:** 
  - Создана DTO `UpdateAllergiesRequest` для сериализации запроса.
  - Добавлен метод `updateAllergies` в `ProfileApi.kt` и `ProfileRepository.kt` (вызывающий `PATCH /profile/allergies`).
  - Добавлен Intent `UpdateAllergies` в `ProfileScreenModel.kt`, который вызывает репозиторий и затем обновляет данные профиля (вызов `loadData`).
  - Создан компонент `ProfileAllergiesBottomSheet.kt` в `features/profile/ui/`, реализующий шторку `BS-002`. Шторка отображает список `ALLERGY_OPTIONS` с чекбоксами (Checkbox) и кнопку "Сохранить", которая возвращает выбранный массив.
  - На экране `ProfileScreen.kt` в блоке аллергий добавлена кнопка "Изменить", которая использует `LocalBottomSheetNavigator` для отображения новой шторки и передачи функции сохранения `onUpdateAllergies`.
- **Затронутые файлы:**
  - `development/client/src/desktopMain/kotlin/com/surfschool/features/profile/data/dto/ProfileDtos.kt`
  - `development/client/src/desktopMain/kotlin/com/surfschool/features/profile/data/ProfileApi.kt`
  - `development/client/src/desktopMain/kotlin/com/surfschool/features/profile/data/ProfileRepository.kt`
  - `development/client/src/desktopMain/kotlin/com/surfschool/features/profile/presentation/ProfileScreenModel.kt`
  - `development/client/src/desktopMain/kotlin/com/surfschool/features/profile/ui/ProfileAllergiesBottomSheet.kt` (NEW)
  - `development/client/src/desktopMain/kotlin/com/surfschool/features/profile/ui/ProfileScreen.kt`

## Дополнительные заметки

Новая шторка принимает текущий список аллергий и инициализирует им локальное состояние чекбоксов, что позволяет корректно добавлять и убирать аллергены. После отправки `PATCH` запроса экран профиля перезагружает свежие данные.
