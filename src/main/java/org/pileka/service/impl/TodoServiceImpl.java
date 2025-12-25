package org.pileka.service.impl;

import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoDto;
import jakarta.transaction.Transactional;
import org.pileka.mapper.TodoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pileka.service.TodoService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@Transactional
public class TodoServiceImpl implements TodoService {
    private TodoDao todoDao;

    @Autowired
    public TodoServiceImpl(TodoDao todoDao) {
        this.todoDao = todoDao;
    }

    @Override
    public TodoDto create(TodoDto todoDto) {
        if (todoDto.getId() != null && todoDao.get(todoDto.getId()) != null) {
            throw new IllegalArgumentException("A todo with this id exists already. TodoDtos intented for being persisted aren't supposed to have non-null id");
        }
        else {
            return TodoMapper.toDto(todoDao.create(TodoMapper.toModel(todoDto)));
        }
    }

    @Override
    public TodoDto get(long id) {
        return null;
    }

    @Override
    public List<TodoDto> getAll() {
        return List.of();
    }

    @Override
    public List<TodoDto> getCompleted() {
        return List.of();
    }

    @Override
    public List<TodoDto> getDue() {
        return List.of();
    }

    @Override
    public List<TodoDto> getDueOn(LocalDate date) {
        return List.of();
    }

    @Override
    public List<TodoDto> getDueIn(Period period) {
        return List.of();
    }

    @Override
    public TodoDto update(TodoDto todoDto) {
        return null;
    }

    @Override
    public void markCompleted(long id) {

    }

    @Override
    public void delete(TodoDto todoDto) {

    }
}
