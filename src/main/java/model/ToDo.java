package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity(name = "todo")
public class ToDo {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private boolean isDone;

    @Column(name = "due_date_time", nullable = false)
    private LocalDateTime dueDateTime;

    @Column(nullable = false)
    private String title;

    private String description;
}
