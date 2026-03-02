package org.pileka.dto.specification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoSpecificationDto {

    private Boolean isDone;

    private LocalDateTime dueBefore;

    private LocalDateTime dueAfter;

}
