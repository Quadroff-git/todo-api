package service;

import dto.TodoDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public interface TodoService {
    TodoDto create(TodoDto TodoDto);

    TodoDto get(long id);
    List<TodoDto> getAll();
    List<TodoDto> getCompleted();
    List<TodoDto> getDue();
    List<TodoDto> getDueOn(LocalDate date);

    /**
     Returns all todos which are due in the specified period or less, including ones that are overdue
     */
    List<TodoDto> getDueIn(Period period);

    TodoDto update(TodoDto TodoDto);
    void markCompleted(long id);

    void delete(TodoDto TodoDto);
}
