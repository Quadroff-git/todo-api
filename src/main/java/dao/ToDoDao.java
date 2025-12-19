package dao;

import model.ToDo;

import java.time.Period;
import java.util.List;

public interface ToDoDao {
    ToDo create(ToDo todo);

    ToDo get(long id);
    List<ToDo> getAll();
    List<ToDo> getCompleted();
    List<ToDo> getDueIn(Period period);

    ToDo update(ToDo todo);

    void delete(ToDo todo);
}
