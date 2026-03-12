package org.pileka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "todo")
public class Todo {
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

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;

        if (o instanceof Todo that) {
            return this.id != null && this.id.equals(that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
