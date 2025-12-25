package org.pileka.dao.impl;

import org.pileka.dao.TodoDao;
import org.pileka.model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Repository
public class TodoDaoImpl implements TodoDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public TodoDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Todo create(Todo todo) {
        getSession().persist(todo);
        return todo;
    }

    @Override
    public Todo get(long id) {
        return getSession().find(Todo.class, id);
    }

    @Override
    public List<Todo> getAll() {
        return getSession().createSelectionQuery("from todo", Todo.class).getResultList();
    }

    @Override
    public List<Todo> getCompleted() {
        return getSession().createSelectionQuery("from todo where isDone = true", Todo.class).getResultList();
    }

    @Override
    public List<Todo> getDue() {
        return getSession().createSelectionQuery("from todo where isDone = false", Todo.class).getResultList();
    }

    @Override
    public List<Todo> getDueOn(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);

        return getSession().createSelectionQuery(
                        "from todo where isDone = false and dueDateTime >= ?1 and dueDateTime < ?2", Todo.class)
                .setParameter(1, startOfDay)
                .setParameter(2, endOfDay.plusDays(1))  // Or use endOfDay if you want inclusive
                .getResultList();
    }

    @Override
    public List<Todo> getDueIn(Period period) {
        return getSession()
                .createSelectionQuery("from todo where dueDateTime <= ?1 and (isDone = false)", Todo.class)
                .setParameter(1, LocalDateTime.now().plus(period))
                .getResultList();
    }

    @Override
    public Todo update(Todo todo) {
        return getSession().merge(todo);
    }

    @Override
    public void markCompleted(long id) {
        getSession().find(Todo.class, id).setDone(true);
    }

    @Override
    public void delete(Todo todo) {
        getSession().remove(todo);
    }
}
