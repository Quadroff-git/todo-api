package pileka.service;

import org.pileka.dto.TodoDto;
import org.pileka.exception.TodoNotFoundException;
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
import pileka.AbstractTodoTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test class providing integration testing for service and dao layers.
 * Dependency injection and transaction management is done by Spring
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("file:src/test/java/pileka/test-applicationContext.xml")
public class TodoServiceImplIntegrationTest extends AbstractTodoTest {
    @Autowired
    private TodoService todoService;

    @Autowired
    private SessionFactory sessionFactory;

    @AfterEach
    public void clearDatabase() {
        System.out.println("rows deleted " + sessionFactory.fromTransaction(session -> session.createMutationQuery("delete todo").executeUpdate()));
    }

    @Test
    public void createPersistsWithNullId() {
        TodoDto todoDto = getTestTodoDto();

        TodoDto createdDto = todoService.create(todoDto);

        assertNotNull(createdDto.getId());

        Todo persistedTodo = sessionFactory.fromTransaction(session -> session.find(Todo.class, createdDto.getId()));
        assertEquals(createdDto, TodoMapper.toDto(persistedTodo));
    }

    @Test
    public void createThrowsIllegalArgumentExceptionForNonNullId() {
        TodoDto todoDto = getTestTodoDto();
        todoDto.setId(1L);

        assertThrows(IllegalArgumentException.class, () -> todoService.create(todoDto));
    }

    @Test
    public void getByIdRetrievesEntity() {
        List<Todo> testTodos = new ArrayList<>();
        int todoCount = 5;
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        TodoDto retrievedDto = todoService.getById(testTodos.get(1).getId());

        assertEquals(TodoMapper.toDto(testTodos.get(1)), retrievedDto);
    }

    @Test
    public void getByIdReturnsNullWhenEntityNotFound() {
        List<Todo> testTodos = new ArrayList<>();
        int todoCount = 5;
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        assertNull(todoService.getById(-1L));
    }

    @Test
    public void getAllRetrievesAll() {
        List<Todo> testTodos = new ArrayList<>();
        int todoCount = 5;
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> fetchedDtos = todoService.getAll();
        fetchedDtos.sort(Comparator.comparing(TodoDto::getTitle));

        assertEquals(testTodos.size(), fetchedDtos.size());

        List<TodoDto> expectedDtos = testTodos.stream()
                .map(TodoMapper::toDto)
                .sorted(Comparator.comparing(TodoDto::getTitle))
                .toList();

        assertEquals(expectedDtos, fetchedDtos);
    }

    @Test
    public void getAllReturnsEmpty() {
        assertTrue(todoService.getAll().isEmpty());
    }

    @Test
    public void updateWorksCorrectly() {
        Todo testTodo = getTestTodo();
        sessionFactory.inTransaction(session -> session.persist(testTodo));

        TodoDto testTodoDto = TodoMapper.toDto(testTodo);
        String newTitle = "An updated title";
        testTodoDto.setTitle(newTitle);

        TodoDto updatedDto = todoService.update(testTodoDto);

        assertEquals(newTitle, updatedDto.getTitle());
        assertEquals(testTodo.getId(), updatedDto.getId());

        Todo persistedTodo = sessionFactory.fromTransaction(session -> session.find(Todo.class, testTodo.getId()));
        assertEquals(newTitle, persistedTodo.getTitle());
    }

    @Test
    public void updateThrowsTodoNotFoundExceptionWhenEntityNotFound() {
        TodoDto nonExistentDto = getTestTodoDto();
        nonExistentDto.setId(999L);

        assertThrows(TodoNotFoundException.class, () -> todoService.update(nonExistentDto));
    }

    @Test
    public void markCompletedMarksTodoAsCompleted() {
        Todo testTodo = getTestTodo();
        testTodo.setDone(false);
        sessionFactory.inTransaction(session -> session.persist(testTodo));

        todoService.markCompleted(testTodo.getId());

        TodoDto markedDto = todoService.getById(testTodo.getId());
        assertTrue(markedDto.isDone());

        Todo persistedTodo = sessionFactory.fromTransaction(session -> session.find(Todo.class, testTodo.getId()));
        assertTrue(persistedTodo.isDone());
    }

    @Test
    public void markCompletedDoesNothingWhenEntityNotFound() {
        int todoCount = 3;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        assertDoesNotThrow(() -> todoService.markCompleted(999L));
    }

    @Test
    public void deleteWorksCorrectly() {
        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        TodoDto todoDtoToDelete = TodoMapper.toDto(testTodos.get(0));
        todoService.delete(todoDtoToDelete);

        List<Todo> remainingTodos = sessionFactory.fromTransaction(
                session -> session.createSelectionQuery("from todo", Todo.class).getResultList()
        );

        assertEquals(todoCount - 1, remainingTodos.size());
        assertFalse(remainingTodos.contains(testTodos.get(0)));
    }

    @Test
    public void deleteThrowsIllegalArgumentExceptionWhenDtoHasNullId() {
        TodoDto todoDto = getTestTodoDto();
        todoDto.setId(null);

        assertThrows(IllegalArgumentException.class, () -> todoService.delete(todoDto));
    }
}
