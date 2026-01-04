package pileka.service;

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
    public void getRetrievesEntity() {
        List<Todo> testTodos = new ArrayList<>();
        int todoCount = 5;
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        TodoDto retrievedDto = todoService.get(testTodos.get(1).getId());

        assertEquals(TodoMapper.toDto(testTodos.get(1)), retrievedDto);
    }

    @Test
    public void getReturnsNullWhenEntityNotFound() {
        List<Todo> testTodos = new ArrayList<>();
        int todoCount = 5;
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        assertNull(todoService.get(-1L));
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

        assertThrows(exception.TodoNotFoundException.class, () -> todoService.update(nonExistentDto));
    }

    @Test
    public void getCompletedRetrievesCompleted() {
        int todoCount = 6;
        int completedTodoCount = 2;

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            Todo todo = getTestTodo(i);

            if (i < completedTodoCount) {
                todo.setDone(true);
            }

            testTodos.add(todo);
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> completedDtos = todoService.getCompleted();

        assertEquals(completedTodoCount, completedDtos.size());

        for (int i = 0; i < completedTodoCount; i++) {
            assertTrue(completedDtos.contains(TodoMapper.toDto(testTodos.get(i))));
        }
    }

    @Test
    public void getCompletedReturnsEmpty() {
        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> completedDtos = todoService.getCompleted();

        assertTrue(completedDtos.isEmpty());
    }

    @Test
    public void getDueRetrievesAllDue() {
        int todoCount = 6;
        int dueTodoCount = 2;

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));

            if (i >= dueTodoCount) {
                testTodos.get(i).setDone(true);
            }
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> dueDtos = todoService.getDue();

        assertEquals(dueTodoCount, dueDtos.size());
    }

    @Test
    public void getDueReturnsEmpty() {
        int todoCount = 6;

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            Todo todo = getTestTodo(i);
            todo.setDone(true);
            testTodos.add(todo);
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> dueDtos = todoService.getDue();

        assertTrue(dueDtos.isEmpty());
    }

    @Test
    public void getDueOnRetrievesDueOnDate() {
        LocalDate dueDate = LocalDate.of(2026, 1, 1);

        List<Todo> testTodos = new ArrayList<>();

        Todo dueOnDateAtMidnight = getTestTodo();
        dueOnDateAtMidnight.setDueDateTime(dueDate.atStartOfDay());
        testTodos.add(dueOnDateAtMidnight);

        Todo dueOnDateAtNoon = getTestTodo(1);
        dueOnDateAtNoon.setDueDateTime(dueDate.atTime(12, 0));
        testTodos.add(dueOnDateAtNoon);

        Todo dueOnDateBeforeMidnight = getTestTodo(2);
        dueOnDateBeforeMidnight.setDueDateTime(dueDate.atTime(23, 59));
        testTodos.add(dueOnDateBeforeMidnight);

        Todo dueBefore = getTestTodo(3);
        dueBefore.setDueDateTime(dueDate.minusDays(1).atTime(12, 0));
        testTodos.add(dueBefore);

        Todo dueAfter = getTestTodo(4);
        dueAfter.setDueDateTime(dueDate.plusDays(1).atTime(12, 0));
        testTodos.add(dueAfter);

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> dueOnDateDtos = todoService.getDueOn(dueDate);

        assertEquals(3, dueOnDateDtos.size());

        List<Long> dueOnDateIds = dueOnDateDtos.stream().map(TodoDto::getId).toList();
        assertTrue(dueOnDateIds.contains(dueOnDateAtMidnight.getId()));
        assertTrue(dueOnDateIds.contains(dueOnDateAtNoon.getId()));
        assertTrue(dueOnDateIds.contains(dueOnDateBeforeMidnight.getId()));
    }

    @Test
    public void getDueOnReturnsEmpty() {
        LocalDate dueDate = LocalDate.of(2026, 1, 1);

        List<Todo> testTodos = new ArrayList<>();

        Todo dueBefore = getTestTodo();
        dueBefore.setDueDateTime(dueDate.minusDays(1).atTime(12, 0));
        testTodos.add(dueBefore);

        Todo dueAfter = getTestTodo(1);
        dueAfter.setDueDateTime(dueDate.plusDays(1).atTime(12, 0));
        testTodos.add(dueAfter);

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> dueOnDateDtos = todoService.getDueOn(dueDate);

        assertTrue(dueOnDateDtos.isEmpty());
    }

    @Test
    public void getDueInRetrievesDueInPeriod() {
        Period duePeriod = Period.ofDays(14);

        List<Todo> testTodos = new ArrayList<>();
        int i = 0;

        Todo dueInTwoWeeks = getTestTodo(i++);
        dueInTwoWeeks.setDueDateTime(LocalDateTime.now().plusDays(14));
        testTodos.add(dueInTwoWeeks);

        Todo dueTomorrow = getTestTodo(i++);
        dueTomorrow.setDueDateTime(LocalDateTime.now().plusDays(1));
        testTodos.add(dueTomorrow);

        Todo dueNow = getTestTodo(i++);
        dueNow.setDueDateTime(LocalDateTime.now());
        testTodos.add(dueNow);

        Todo overdue = getTestTodo(i++);
        overdue.setDueDateTime(LocalDateTime.now().minusDays(2));
        testTodos.add(overdue);

        Todo dueIn15Days = getTestTodo(i++);
        dueIn15Days.setDueDateTime(LocalDateTime.now().plusDays(15));
        testTodos.add(dueIn15Days);

        Todo dueIn1Month = getTestTodo(i++);
        dueIn1Month.setDueDateTime(LocalDateTime.now().plusMonths(1));
        testTodos.add(dueIn1Month);

        Todo completedWithinPeriod = getTestTodo(i++);
        completedWithinPeriod.setDone(true);
        completedWithinPeriod.setDueDateTime(LocalDateTime.now().plusDays(3));
        testTodos.add(completedWithinPeriod);

        Todo completedInPast = getTestTodo(i++);
        completedInPast.setDone(true);
        completedInPast.setDueDateTime(LocalDateTime.now().minusDays(3));
        testTodos.add(completedInPast);

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> dueInPeriodDtos = todoService.getDueIn(duePeriod);

        assertEquals(4, dueInPeriodDtos.size());

        List<Long> dueInPeriodIds = dueInPeriodDtos.stream().map(TodoDto::getId).toList();
        assertTrue(dueInPeriodIds.contains(dueInTwoWeeks.getId()));
        assertTrue(dueInPeriodIds.contains(dueTomorrow.getId()));
        assertTrue(dueInPeriodIds.contains(dueNow.getId()));
        assertTrue(dueInPeriodIds.contains(overdue.getId()));
    }

    @Test
    public void getDueInReturnsEmpty() {
        Period duePeriod = Period.ofDays(14);

        List<Todo> testTodos = new ArrayList<>();
        int i = 0;

        Todo dueIn15Days = getTestTodo(i++);
        dueIn15Days.setDueDateTime(LocalDateTime.now().plusDays(15));
        testTodos.add(dueIn15Days);

        Todo dueIn1Month = getTestTodo(i++);
        dueIn1Month.setDueDateTime(LocalDateTime.now().plusMonths(1));
        testTodos.add(dueIn1Month);

        Todo completedWithinPeriod = getTestTodo(i++);
        completedWithinPeriod.setDone(true);
        completedWithinPeriod.setDueDateTime(LocalDateTime.now().plusDays(3));
        testTodos.add(completedWithinPeriod);

        Todo completedInPast = getTestTodo(i++);
        completedInPast.setDone(true);
        completedInPast.setDueDateTime(LocalDateTime.now().minusDays(3));
        testTodos.add(completedInPast);

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<TodoDto> dueInPeriodDtos = todoService.getDueIn(duePeriod);

        assertTrue(dueInPeriodDtos.isEmpty());
    }

    @Test
    public void markCompletedMarksTodoAsCompleted() {
        Todo testTodo = getTestTodo();
        testTodo.setDone(false);
        sessionFactory.inTransaction(session -> session.persist(testTodo));

        todoService.markCompleted(testTodo.getId());

        TodoDto markedDto = todoService.get(testTodo.getId());
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
