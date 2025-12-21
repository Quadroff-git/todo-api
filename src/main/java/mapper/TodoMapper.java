package mapper;

import dto.ToDoDto;
import model.Todo;

public class TodoMapper {
    public static ToDoDto toDto(Todo todo) {
        return new ToDoDto(
                todo.getId(),
                todo.isDone(),
                todo.getDueDateTime(),
                todo.getTitle(),
                todo.getDescription()
        );
    }

    public static Todo toModel(ToDoDto todoDto) {
        return new Todo(
                todoDto.getId(),
                todoDto.isDone(),
                todoDto.getDueDateTime(),
                todoDto.getTitle(),
                todoDto.getDescription()
        );
    }
}
