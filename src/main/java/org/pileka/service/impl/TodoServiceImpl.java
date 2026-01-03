package org.pileka.service.impl;

import exception.TodoNotFoundException;
import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoDto;
import jakarta.transaction.Transactional;
import org.pileka.mapper.TodoMapper;
import org.pileka.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pileka.service.TodoService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


@Service
@Transactional
public class TodoServiceImpl implements TodoService {
    private final TodoDao todoDao;

    @Autowired
    public TodoServiceImpl(TodoDao todoDao) {
        this.todoDao = todoDao;
    }

    @Override
    public TodoDto create(TodoDto todoDto) {
        if (todoDto.getId() != null) {
            throw new IllegalArgumentException("TodoDto's id field isn't null! It can't be persisted");
        }
        else {
            return TodoMapper.toDto(todoDao.create(TodoMapper.toModel(todoDto)));
        }
    }

    @Override
    public TodoDto get(long id) {
        return TodoMapper.toDto(todoDao.get(id));
    }

    @Override
    public List<TodoDto> getAll() {
        return todoDao.getAll().stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    @Override
    public List<TodoDto> getCompleted() {
        return todoDao.getCompleted().stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    @Override
    public List<TodoDto> getDue() {
        return todoDao.getDue().stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    @Override
    public List<TodoDto> getDueOn(LocalDate date) {
        return todoDao.getDueOn(date).stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    @Override
    public List<TodoDto> getDueIn(Period period) {
        return todoDao.getDueIn(period).stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    @Override
    public TodoDto update(TodoDto todoDto) {
        if (todoDao.get(todoDto.getId()) != null) {
            return TodoMapper.toDto(todoDao.update(TodoMapper.toModel(todoDto)));
        }
        else {
            throw new TodoNotFoundException("No todo found with id: " + todoDto.getId());
        }
    }

    @Override
    public void markCompleted(long id) {
        Todo toMark = todoDao.get(id);
        if (toMark != null) {
           toMark.setDone(true);
        }
    }

    @Override
    public void delete(TodoDto todoDto) {
        if (todoDto.getId() == null) {
            throw new IllegalArgumentException("TodoDto must have an id to be deleted");
        }

        if (todoDao.get(todoDto.getId()) == null) {
            throw new TodoNotFoundException("No todo found with id: " + todoDto.getId());
        }

        // Delete using the entity
        todoDao.delete(TodoMapper.toModel(todoDto));
    }
}
