package org.pileka.service.impl;

import org.pileka.dto.TodoSpecificationDto;
import org.pileka.exception.TodoNotFoundException;
import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoDto;

import org.pileka.mapper.TodoMapper;
import org.pileka.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pileka.service.TodoService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
    @Transactional(readOnly = true)
    public TodoDto getById(long id) {
        return TodoMapper.toDto(todoDao.getById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoDto> getAll() {
        return todoDao.getAll().stream()
                .map(TodoMapper::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<TodoDto> get(TodoSpecificationDto specDto) {
        return List.of();
    }

    @Override
    public TodoDto update(TodoDto todoDto) {
        if (todoDao.getById(todoDto.getId()) != null) {
            return TodoMapper.toDto(todoDao.update(TodoMapper.toModel(todoDto)));
        }
        else {
            throw new TodoNotFoundException("No todo found with id: " + todoDto.getId());
        }
    }

    @Override
    public void markCompleted(long id) {
        Todo toMark = todoDao.getById(id);
        if (toMark != null) {
           toMark.setDone(true);
        }
    }

    @Override
    public void delete(TodoDto todoDto) {
        if (todoDto.getId() == null) {
            throw new IllegalArgumentException("TodoDto must have an id to be deleted");
        }

        if (todoDao.getById(todoDto.getId()) == null) {
            throw new TodoNotFoundException("No todo found with id: " + todoDto.getId());
        }

        // Delete using the entity
        todoDao.delete(TodoMapper.toModel(todoDto));
    }
}
