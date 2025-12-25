package mapper;

import dto.TodoDto;
import model.Todo;

public class TodoMapper {
    public static TodoDto toDto(Todo todo) {
        return new TodoDto(
                todo.getId(),
                todo.isDone(),
                todo.getDueDateTime(),
                todo.getTitle(),
                todo.getDescription()
        );
    }

    public static Todo toModel(TodoDto todoDto) {
        return new Todo(
                todoDto.getId(),
                todoDto.isDone(),
                todoDto.getDueDateTime(),
                todoDto.getTitle(),
                todoDto.getDescription()
        );
    }
}
