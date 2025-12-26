package service;

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
        System.out.println("rows deleted " + sessionFactory.fromTransaction(session -> {
            return session.createMutationQuery("delete todo").executeUpdate();
        }));
    }

    @Test
    public void testCreate() {
        TodoDto todoDto = new TodoDto(null, false, LocalDateTime.now(), "Test todo dto", null);

        todoDto = todoService.create(todoDto);

        List<Todo> todos = sessionFactory.fromTransaction(session -> {
            return session.createSelectionQuery("from todo", Todo.class).getResultList();
        });

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
}
