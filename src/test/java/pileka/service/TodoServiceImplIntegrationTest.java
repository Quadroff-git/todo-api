package pileka.service;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test class providing integration testing for service and dao layers.
 * Dependency injection and transaction management is done by Spring
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("file:src/test/java/pileka/test-applicationContext.xml")
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

    @Test
    void testGetCompleted() {
        // Create completed and incomplete todos
        Todo completedTodo1 = new Todo();
        completedTodo1.setTitle("Completed 1");
        completedTodo1.setDueDateTime(LocalDateTime.now());
        completedTodo1.setDone(true);

        Todo completedTodo2 = new Todo();
        completedTodo2.setTitle("Completed 2");
        completedTodo2.setDueDateTime(LocalDateTime.now());
        completedTodo2.setDone(true);

        Todo incompleteTodo = new Todo();
        incompleteTodo.setTitle("Incomplete");
        incompleteTodo.setDueDateTime(LocalDateTime.now());
        incompleteTodo.setDone(false);

        sessionFactory.inTransaction(session -> {
            session.persist(completedTodo1);
            session.persist(completedTodo2);
            session.persist(incompleteTodo);
        });

        List<TodoDto> completedTodos = todoService.getCompleted();

        assertEquals(2, completedTodos.size());
        assertTrue(completedTodos.stream().allMatch(TodoDto::isDone));

        List<String> titles = completedTodos.stream().map(TodoDto::getTitle).sorted().toList();
        assertTrue(titles.contains("Completed 1"));
        assertTrue(titles.contains("Completed 2"));
    }

    @Test
    void testGetDue() {
        LocalDateTime now = LocalDateTime.now();

        // Create overdue and future todos
        Todo overdueTodo = new Todo();
        overdueTodo.setTitle("Overdue");
        overdueTodo.setDueDateTime(now.minusDays(1));
        overdueTodo.setDone(false);

        Todo dueNowTodo = new Todo();
        dueNowTodo.setTitle("Due now");
        dueNowTodo.setDueDateTime(now);
        dueNowTodo.setDone(false);

        Todo futureTodo = new Todo();
        futureTodo.setTitle("Future");
        futureTodo.setDueDateTime(now.plusDays(1));
        futureTodo.setDone(false);

        Todo completedTodo = new Todo();
        completedTodo.setTitle("Completed but overdue");
        completedTodo.setDueDateTime(now.minusDays(1));
        completedTodo.setDone(true);

        sessionFactory.inTransaction(session -> {
            session.persist(overdueTodo);
            session.persist(dueNowTodo);
            session.persist(futureTodo);
            session.persist(completedTodo);
        });

        List<TodoDto> dueTodos = todoService.getDue();

        assertEquals(3, dueTodos.size());

        List<String> titles = dueTodos.stream().map(TodoDto::getTitle).sorted().toList();
        assertTrue(titles.contains("Overdue"));
        assertTrue(titles.contains("Due now"));
        assertTrue(titles.contains("Future"));
    }

    @Test
    void testGetDueOn() {
        LocalDate targetDate = LocalDate.of(2024, 1, 15);

        // Create todos on target date, before, and after
        Todo todoOnDate1 = new Todo();
        todoOnDate1.setTitle("On date morning");
        todoOnDate1.setDueDateTime(targetDate.atTime(9, 0));
        todoOnDate1.setDone(false);

        Todo todoOnDate2 = new Todo();
        todoOnDate2.setTitle("On date evening");
        todoOnDate2.setDueDateTime(targetDate.atTime(18, 0));
        todoOnDate2.setDone(false);

        Todo todoBeforeDate = new Todo();
        todoBeforeDate.setTitle("Before date");
        todoBeforeDate.setDueDateTime(targetDate.minusDays(1).atTime(12, 0));
        todoBeforeDate.setDone(false);

        Todo todoAfterDate = new Todo();
        todoAfterDate.setTitle("After date");
        todoAfterDate.setDueDateTime(targetDate.plusDays(1).atTime(12, 0));
        todoAfterDate.setDone(false);

        Todo completedTodoOnDate = new Todo();
        completedTodoOnDate.setTitle("Completed on date");
        completedTodoOnDate.setDueDateTime(targetDate.atTime(12, 0));
        completedTodoOnDate.setDone(true);

        sessionFactory.inTransaction(session -> {
            session.persist(todoOnDate1);
            session.persist(todoOnDate2);
            session.persist(todoBeforeDate);
            session.persist(todoAfterDate);
            session.persist(completedTodoOnDate);
        });

        List<TodoDto> todosDueOnDate = todoService.getDueOn(targetDate);

        // Should only include incomplete todos on the target date
        assertEquals(2, todosDueOnDate.size());

        List<String> titles = todosDueOnDate.stream().map(TodoDto::getTitle).sorted().toList();
        assertTrue(titles.contains("On date morning"));
        assertTrue(titles.contains("On date evening"));
    }

    @Test
    void testGetDueIn() {
        LocalDateTime now = LocalDateTime.now();
        Period period = Period.ofDays(3);

        // Create todos within period, before, and after
        Todo todoWithinPeriod1 = new Todo();
        todoWithinPeriod1.setTitle("Within period 1");
        todoWithinPeriod1.setDueDateTime(now.plusDays(1));
        todoWithinPeriod1.setDone(false);

        Todo todoWithinPeriod2 = new Todo();
        todoWithinPeriod2.setTitle("Within period 2");
        todoWithinPeriod2.setDueDateTime(now.plusDays(3)); // Exactly at the boundary
        todoWithinPeriod2.setDone(false);

        Todo todoPastDue = new Todo();
        todoPastDue.setTitle("Past due");
        todoPastDue.setDueDateTime(now.minusDays(1));
        todoPastDue.setDone(false);

        Todo todoBeyondPeriod = new Todo();
        todoBeyondPeriod.setTitle("Beyond period");
        todoBeyondPeriod.setDueDateTime(now.plusDays(4)); // One day beyond period
        todoBeyondPeriod.setDone(false);

        Todo completedWithinPeriod = new Todo();
        completedWithinPeriod.setTitle("Completed within period");
        completedWithinPeriod.setDueDateTime(now.plusDays(2));
        completedWithinPeriod.setDone(true);

        sessionFactory.inTransaction(session -> {
            session.persist(todoWithinPeriod1);
            session.persist(todoWithinPeriod2);
            session.persist(todoPastDue);
            session.persist(todoBeyondPeriod);
            session.persist(completedWithinPeriod);
        });

        List<TodoDto> todosDueInPeriod = todoService.getDueIn(period);

        // Should include: todos within period (including boundary) and past due todos
        // Should exclude: beyond period and completed todos
        assertEquals(3, todosDueInPeriod.size());

        List<String> titles = todosDueInPeriod.stream().map(TodoDto::getTitle).sorted().toList();
        assertTrue(titles.contains("Within period 1"));
        assertTrue(titles.contains("Within period 2"));
        assertTrue(titles.contains("Past due"));
    }

    @Test
    void testMarkCompleted() {
        Todo testTodo = new Todo();
        testTodo.setTitle("Test todo");
        testTodo.setDueDateTime(LocalDateTime.now());
        testTodo.setDone(false);

        sessionFactory.inTransaction(session -> session.persist(testTodo));

        todoService.markCompleted(testTodo.getId());

        // Verify the todo is now marked as completed
        Todo updatedTodo = sessionFactory.fromTransaction(session -> session.find(Todo.class, testTodo.getId()));
        assertTrue(updatedTodo.isDone());

        // Verify through service get
        TodoDto todoDto = todoService.get(testTodo.getId());
        assertTrue(todoDto.isDone());
    }

    @Test
    void testDeleteByTodoDto() {
        Todo testTodo = new Todo();
        testTodo.setTitle("Test todo to delete");
        testTodo.setDueDateTime(LocalDateTime.now());

        sessionFactory.inTransaction(session -> session.persist(testTodo));

        TodoDto todoDto = TodoMapper.toDto(testTodo);
        todoService.delete(todoDto);

        // Verify todo is deleted
        Long count = sessionFactory.fromTransaction(session ->
                session.createSelectionQuery("SELECT COUNT(t) FROM todo t WHERE t.id = :id", Long.class)
                        .setParameter("id", testTodo.getId())
                        .getSingleResult()
        );

        assertEquals(0L, count);
    }

    @Test
    void testDeleteById() {
        // Assuming you have a delete(long id) method in your service
        Todo testTodo = new Todo();
        testTodo.setTitle("Test todo to delete by id");
        testTodo.setDueDateTime(LocalDateTime.now());

        sessionFactory.inTransaction(session -> session.persist(testTodo));

        // If your service has delete(long id) method
        // todoService.delete(testTodo.getId());

        // Or use the delete(TodoDto) method
        todoService.delete(TodoMapper.toDto(testTodo));

        // Verify todo is deleted
        Long count = sessionFactory.fromTransaction(session ->
                session.createSelectionQuery("SELECT COUNT(t) FROM todo t WHERE t.id = :id", Long.class)
                        .setParameter("id", testTodo.getId())
                        .getSingleResult()
        );

        assertEquals(0L, count);
    }
}
