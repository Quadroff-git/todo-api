package org.pileka.dao.impl;

import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoSpecificationDto;
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
    public Todo getById(long id) {
        return getSession().find(Todo.class, id);
    }

    @Override
    public List<Todo> getAll() {
        return getSession().createSelectionQuery("from todo", Todo.class).getResultList();
    }

    @Override
    public List<Todo> get(TodoSpecificationDto specDto) {
        return List.of();
    }


    @Override
    public Todo update(Todo todo) {
        return getSession().merge(todo);
    }


    @Override
    public void delete(Todo todo) {
        getSession().remove(todo);
    }
}
