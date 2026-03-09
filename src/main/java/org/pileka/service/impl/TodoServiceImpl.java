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
    public List<TodoDto> get(TodoSpecificationDto specDto) {
        return todoDao.get(specDto).stream().map(TodoMapper::toDto).toList();
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
    @Transactional
    public void delete(long id) {
        Todo toDelete = todoDao.getById(id);
        if (toDelete == null) {
            throw new TodoNotFoundException("No todo found with id: " + id);
        }

        // Delete using the entity
        todoDao.delete(toDelete);
    }
}
