package dao;

import model.Todo;

import java.time.Period;
import java.util.List;

public interface TodoDao {
    Todo create(Todo todo);

    Todo get(long id);
    List<Todo> getAll();
    List<Todo> getCompleted();
    List<Todo> getDueIn(Period period);

    Todo update(Todo todo);
    void markCompleted(long id);

    void delete(Todo todo);
}
