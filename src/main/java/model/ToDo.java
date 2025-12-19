package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "todo")
public class ToDo {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "is_done", nullable = false)
    private boolean isDone;

    @Column(name = "due_date_time", nullable = false)
    private LocalDateTime dueDateTime;

    @Column(nullable = false)
    private String title;

    private String description;
}
