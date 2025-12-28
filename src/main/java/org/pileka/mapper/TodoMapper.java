package org.pileka.mapper;

import org.pileka.dto.TodoDto;
import org.pileka.model.Todo;

public class TodoMapper {
    public static TodoDto toDto(Todo todo) {
        if (todo == null) {
            return null;
        }

        return new TodoDto(
                todo.getId(),
                todo.isDone(),
                todo.getDueDateTime(),
                todo.getTitle(),
                todo.getDescription()
        );
    }

    public static Todo toModel(TodoDto todoDto) {
        if (todoDto == null) {
            return null;
        }

        return new Todo(
                todoDto.getId(),
                todoDto.isDone(),
                todoDto.getDueDateTime(),
                todoDto.getTitle(),
                todoDto.getDescription()
        );
    }
}
