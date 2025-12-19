package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToDoDto {
    private Long id;
    private boolean isDone;
    private LocalDateTime dueDateTime;
    private String title;
    private String description;
}
