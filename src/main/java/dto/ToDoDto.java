package dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ToDoDto {
    private Long id;
    private boolean isDone;
    private LocalDateTime dueDateTime;
    private String title;
    private String description;
}
