package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Feed {
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Integer eventId;
    private Long entityId;
}
