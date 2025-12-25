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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity(name = "todo")
public class Todo {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "is_done", nullable = false)
    @EqualsAndHashCode.Include
    private boolean isDone;

    @Column(name = "due_date_time", nullable = false)
    private LocalDateTime dueDateTime;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String title;

    @EqualsAndHashCode.Include
    private String description;

    /**
     * Persisting and fetching of model objects results in a loss of precision for dueDateTime
     * which causes equals to return false.
     * Since this kind of precision is unnecessary for this domain, we simply override the dueDateTime equals check to
     * only have minutes precision
     * */
    public boolean dueDateTimeEquals(Todo other) {
        if (this.dueDateTime == null && other.dueDateTime == null) return true;
        if (this.dueDateTime == null || other.dueDateTime == null) return false;

        return this.dueDateTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                .equals(other.dueDateTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
    }
}
