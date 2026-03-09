package org.pileka.controller;

import org.pileka.dto.TodoDto;
import org.pileka.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/todo")
public class TodoRestController {
    // a rough prototype controller made to check if the app builds and deploys correctly with the current configuration
    // most of the effort is concentrated in covering the other components with tests atm

    private final TodoService todoService;

    @Autowired
    public TodoRestController(TodoService todoService) {
        this.todoService = todoService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody TodoDto todoDto) {
        todoService.create(todoDto);
    }
}
