package org.pileka.service;

import org.pileka.dto.TodoDto;
import org.pileka.dto.TodoSpecificationDto;

import java.util.List;

public interface TodoService {
    TodoDto create(TodoDto TodoDto);

    TodoDto getById(long id);
    List<TodoDto> get(TodoSpecificationDto specDto);

    TodoDto update(TodoDto TodoDto);
    void markCompleted(long id);

    void delete(TodoDto TodoDto);
}
