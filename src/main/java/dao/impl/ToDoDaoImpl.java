package dao.impl;

import dao.ToDoDao;
import model.ToDo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Period;
import java.util.List;

@Repository
public class ToDoDaoImpl implements ToDoDao {

    private final SessionFactory sessionFactory;

    @Autowired
    ToDoDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public ToDo create(ToDo todo) {
        getSession().persist(todo);
        return todo;
    }

    @Override
    public ToDo get(long id) {
        return null;
    }

    @Override
    public List<ToDo> getAll() {
        return List.of();
    }

    @Override
    public List<ToDo> getCompleted() {
        return List.of();
    }

    @Override
    public List<ToDo> getDueIn(Period period) {
        return List.of();
    }

    @Override
    public ToDo update(ToDo todo) {
        return null;
    }

    @Override
    public void delete(ToDo todo) {

    }
}
