# Sequence-диаграмма: Создание бронирования

Данный документ описывает последовательность взаимодействия систем при создании бронирования на основе `[UC-30](../2-requirements/use-cases.md)`.

## 1. Описание процесса (Create Booking)

Процесс охватывает действия клиента с момента выбора слота до получения подтверждения о резерве места. Включает логику проверки доступности инвентаря `[FR-65](../2-requirements/functional-requirements.md)`, расчета стоимости `[FR-75](../2-requirements/functional-requirements.md)` и обработки возможных отказов от бэкенда (конкурентные транзакции, неактуальный статус слота).

## 2. Диаграмма последовательности (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor U as Пользователь
    participant App as Мобильное Приложение (Frontend)
    participant API as Бэкенд (API)
    participant DB as База Данных

    Note over U,App: Предусловие: Пользователь авторизован и открыл экран слота
    
    U->>App: Нажимает "Забронировать" слот
    App->>App: Проверка available_equipment_stock [FR-65, FR-70]
    
    alt Инвентарь доступен (> 0)
        App-->>U: Отображает активный чекбокс аренды
    else Инвентарь исчерпан (= 0)
        App-->>U: Отображает заблокированный чекбокс [FR-70]
    end
    
    U->>App: Отмечает чекбокс (если доступно)
    App->>App: Расчет итоговой стоимости [FR-75]
    U->>App: Нажимает "Подтвердить запись"
    
    App->>API: POST /api/bookings {slot_id, needs_rental} [FR-60]
    
    API->>DB: BEGIN TRANSACTION
    API->>DB: SELECT * FROM Slot WHERE id = slot_id FOR UPDATE
    DB-->>API: Возвращает актуальные данные слота
    
    alt Успешное создание бронирования [HTTP 201 Created]
        Note over API: Бизнес-валидация пройдена (места и инвентарь в наличии, слот активен)
        API->>DB: UPDATE Slot: уменьшить available_seats и available_equipment_stock
        API->>DB: INSERT INTO Booking (status='PENDING_PAYMENT')
        DB-->>API: Успешно (Booking ID)
        API->>DB: COMMIT TRANSACTION
        API-->>App: HTTP 201 Created (Детали брони, реквизиты, таймер)
        App-->>U: Экран "Успешно", таймер 1 час и реквизиты [FR-80]
        
    else Бизнес-ошибка (Нехватка мест/инвентаря) [HTTP 409 Conflict]
        Note over API: Конкурентное бронирование другим пользователем (Гонка)
        API->>DB: ROLLBACK TRANSACTION
        API-->>App: HTTP 409 Conflict (код ошибки: SLOT_FULL / EQUIPMENT_OUT)
        App-->>U: Модальное окно "Места или инвентарь закончились" [UC-30]
        
    else Ошибка доступности ресурса [HTTP 410 Gone]
        Note over API: Слот был отменен студией или уже завершен
        API->>DB: ROLLBACK TRANSACTION
        API-->>App: HTTP 410 Gone
        App-->>U: Ошибка "Слот больше недоступен для бронирования"
    end
```

## 3. Трассировка требований

Диаграмма явно опирается на следующие функциональные требования и прецеденты:
* Управление арендой экипировки в UI: `[FR-65](../2-requirements/functional-requirements.md)`, `[FR-70](../2-requirements/functional-requirements.md)`.
* Калькуляция итоговой суммы: `[FR-75](../2-requirements/functional-requirements.md)`.
* Создание записи: `[FR-60](../2-requirements/functional-requirements.md)`.
* Финальный экран с реквизитами: `[FR-80](../2-requirements/functional-requirements.md)`.
* Обработка гонки за ресурсы (409 Conflict): `[UC-30](../2-requirements/use-cases.md)` (Матрица ошибок).
* Обработка недоступного слота (410 Gone): дополнительный технический кейс недоступности ресурса.
