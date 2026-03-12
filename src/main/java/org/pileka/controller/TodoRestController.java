package org.pileka.controller;

import org.pileka.dto.TodoDto;
import org.pileka.dto.TodoSpecificationDto;
import org.pileka.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/todo")
public class TodoRestController {

    private final TodoService todoService;

    @Autowired
    public TodoRestController(TodoService todoService) {
        this.todoService = todoService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody TodoDto todoDto) {
        todoService.create(todoDto);
    }

    @GetMapping(value = "/example", produces = MediaType.APPLICATION_JSON_VALUE)
    public TodoDto getExample() {
        return new TodoDto(69L,
                false,
                LocalDateTime.now(),
                "Sample todo",
                "This is a sample todo with JSON parsing handled by Jackson");
    }

    @GetMapping("/{id}")
    public TodoDto getById(@PathVariable Long id) {
        return todoService.getById(id);
    }

    @GetMapping
    public List<TodoDto> get(TodoSpecificationDto specDto) {
        return todoService.get(specDto);
    }

    @PutMapping
    public TodoDto update(@RequestBody TodoDto todoDto) {
        return todoService.update(todoDto);
    }

    @PutMapping("/{id}")
    public void markCompleted(@PathVariable Long id) {
        todoService.markCompleted(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        todoService.delete(id);
    }
}
