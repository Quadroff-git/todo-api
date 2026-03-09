package org.pileka.dao.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoSpecificationDto;
import org.pileka.model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    public List<Todo> get(TodoSpecificationDto specDto) {
        // Building the criteriaQuery or at least the predicate could probably be put into a util class, but ehhh

        CriteriaBuilder criteriaBuilder = getSession().getCriteriaBuilder();
        CriteriaQuery<Todo> criteriaQuery = criteriaBuilder.createQuery(Todo.class);
        Root<Todo> todo = criteriaQuery.from(Todo.class);

        var predicates = new ArrayList<Predicate>();
        if (specDto.getIsDone() != null) {
            predicates.add(criteriaBuilder.equal(todo.get("isDone"), specDto.getIsDone()));
        }

        if (specDto.getDueBefore() != null) {
            predicates.add(criteriaBuilder.lessThan(todo.get("dueDateTime"), specDto.getDueBefore()));
        }

        if (specDto.getDueAfter() != null) {
            predicates.add(criteriaBuilder.greaterThan(todo.get("dueDateTime"), specDto.getDueAfter()));
        }

        return getSession().createQuery(
                        criteriaQuery.where(predicates.isEmpty() ?
                                criteriaBuilder.conjunction() :
                                criteriaBuilder.and(predicates)
                        )
                ).getResultList();
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
