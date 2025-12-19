package mapper;

import dto.ToDoDto;
import model.ToDo;

public class ToDoMapper {
    public static ToDoDto toDto(ToDo todo) {
        return new ToDoDto(
                todo.getId(),
                todo.isDone(),
                todo.getDueDateTime(),
                todo.getTitle(),
                todo.getDescription()
        );
    }

    public static ToDo toModel(ToDoDto todoDto) {
        return new ToDo(
                todoDto.getId(),
                todoDto.isDone(),
                todoDto.getDueDateTime(),
                todoDto.getTitle(),
                todoDto.getDescription()
        );
    }
}
