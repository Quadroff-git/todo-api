package dao.impl;

import dao.TodoDao;
import model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
        return null;
    }

    @Override
    public List<Todo> getAll() {
        return List.of();
    }

    @Override
    public List<Todo> getCompleted() {
        return List.of();
    }

    @Override
    public List<Todo> getDueIn(Period period) {
        return List.of();
    }

    @Override
    public Todo update(Todo todo) {
        return null;
    }

    @Override
    public void delete(Todo todo) {

    }
}
