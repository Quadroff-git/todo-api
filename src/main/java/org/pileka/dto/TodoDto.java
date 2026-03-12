package org.pileka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TodoDto {
    @EqualsAndHashCode.Include
    private Long id;

    @JsonProperty("isDone") // TODO: fix returned JSON having two fields: isDone and done with the same value
    @EqualsAndHashCode.Include
    private boolean isDone;

    private LocalDateTime dueDateTime;

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
    public boolean dueDateTimeEquals(TodoDto other) {
        if (this.dueDateTime == null && other.dueDateTime == null) return true;
        if (this.dueDateTime == null || other.dueDateTime == null) return false;

        return this.dueDateTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                .equals(other.dueDateTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
    }
}
