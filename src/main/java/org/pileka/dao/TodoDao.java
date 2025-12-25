package org.pileka.dao;

import org.pileka.model.Todo;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public interface TodoDao {
    Todo create(Todo todo);

    Todo get(long id);
    List<Todo> getAll();
    List<Todo> getCompleted();
    List<Todo> getDue();
    List<Todo> getDueOn(LocalDate date);

    /**
    Returns all todos which are due in the specified period or less, including ones that are overdue
    */
    List<Todo> getDueIn(Period period);

    Todo update(Todo todo);
    void markCompleted(long id);

    void delete(Todo todo);
}
