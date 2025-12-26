package service;

import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoDto;
import org.pileka.mapper.TodoMapper;
import org.pileka.model.Todo;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pileka.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test class providing integration testing for service and dao layers.
 * Dependency injection and transaction management is done by Spring
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("file:src/test/java/test-applicationContext.xml")
public class TodoServiceImplIntegrationTest {
    @Autowired
    private TodoService todoService;

    @Autowired
    private SessionFactory sessionFactory;

    @AfterEach
    public void clearDatabase() {
        System.out.println("rows deleted " + sessionFactory.fromTransaction(session -> session.createMutationQuery("delete todo").executeUpdate()));
    }

    @Test
    public void testCreate() {
        TodoDto todoDto = new TodoDto(null, false, LocalDateTime.now(), "Test todo dto", null);

        todoDto = todoService.create(todoDto);

        List<Todo> todos = sessionFactory.fromTransaction(session -> session.createSelectionQuery("from todo", Todo.class).getResultList());

        assertEquals(1, todos.size());

        assertEquals(todoDto, TodoMapper.toDto(todos.get(0)));
    }

    @Test
    public void testGet() {
        Todo testTodo = new Todo();
        testTodo.setTitle("Test todo");
        testTodo.setDueDateTime(LocalDateTime.now());

        sessionFactory.inTransaction(session -> session.persist(testTodo));

        assertEquals(TodoMapper.toDto(testTodo), todoService.get(testTodo.getId()));
    }

    @Test
    public void testGetAll() {
        int todoCount = 5;

        List<TodoDto> persisted = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            Todo todo = new Todo();
            todo.setTitle("Test todo " + i);
            todo.setDueDateTime(LocalDateTime.now());

            sessionFactory.inTransaction(session -> session.persist(todo));
            persisted.add(TodoMapper.toDto(todo));
        }

        List<TodoDto> fetched = todoService.getAll().stream().sorted(Comparator.comparing(TodoDto::getTitle)).toList();

        assertEquals(persisted, fetched);
    }

    @Test
    void testUpdate() {
        Todo testTodo = new Todo();
        testTodo.setTitle("Test todo");
        testTodo.setDueDateTime(LocalDateTime.now());

        sessionFactory.inTransaction(session -> session.persist(testTodo));

        TodoDto testTodoDto = TodoMapper.toDto(testTodo);

        testTodoDto.setTitle("Changed title");
        testTodoDto = todoService.update(testTodoDto);

        long id = testTodoDto.getId();

        assertEquals(testTodoDto, TodoMapper.toDto(sessionFactory.fromTransaction(session -> session.find(Todo.class, id))));
    }
}
