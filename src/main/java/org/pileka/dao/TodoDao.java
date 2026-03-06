package org.pileka.dao;

import org.pileka.dto.TodoSpecificationDto;
import org.pileka.model.Todo;

import java.util.List;

public interface TodoDao {
    Todo create(Todo todo);

    Todo getById(long id);
    List<Todo> getAll();
    List<Todo> get(TodoSpecificationDto specDto);

    Todo update(Todo todo);

    void delete(Todo todo);
}
