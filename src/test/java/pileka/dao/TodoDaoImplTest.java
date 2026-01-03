package pileka.dao;

import pileka.AbstractTodoTest;
import org.pileka.dao.TodoDao;
import org.pileka.dao.impl.TodoDaoImpl;
import org.pileka.model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test class for testing the DAO using an in-memory H2 database.
 * No Spring IOC container is used, Hibernate configuration and SessionFactory dependency injection is manual
 */
public class TodoDaoImplTest extends AbstractTodoTest {
    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void setUp() {
        try {
            Configuration configuration = new Configuration();

            // H2 Database Configuration
            configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            configuration.setProperty("hibernate.connection.url",
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS testdb\\;SET SCHEMA testdb");
            configuration.setProperty("hibernate.connection.username", "sa");
            configuration.setProperty("hibernate.connection.password", "");
            //configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

            // Hibernate settings
            configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "false");
            configuration.setProperty("hibernate.use_sql_comments", "true");

            configuration.setProperty("hibernate.current_session_context_class",
                    "org.hibernate.context.internal.ThreadLocalSessionContext");

            // Add annotated classes
            configuration.addAnnotatedClass(Todo.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    @AfterAll
    public static void cleanUp() {
        sessionFactory.close();
    }

    @AfterEach
    public void clearDatabase() {
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            session.createMutationQuery("delete todo").executeUpdate();
            transaction.commit();
        }
    }

    /**
     * Tests that create() correctly persists an entity that isn't in the DB yet and doesn't have an id
     * */
    @Test
    public void createPersistsWithNullId() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);
        Todo testTodo = getTestTodo();

        // All calls to todoDao's methods must be wrapped similarly to this.
        // sessionFactory.in/fromTransaction or .in/fromSession doesn't work
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todoDao.create(testTodo);
            transaction.commit();
            assertNotNull(testTodo.getId());
        }

        // Calls to pure Hibernate API are still wrapped in in/fromTransaction for cleanliness though.
        Todo fetchedTodo = sessionFactory.fromTransaction(session -> session.find(Todo.class, testTodo.getId()));
        assertEquals(testTodo, fetchedTodo);
    }

    /**
     * Tests that get() retrieves an entity by id when such an entity exists
     * */
    @Test
    public void getRetrievesEntity() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }
        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Todo todo = todoDao.get(testTodos.get(0).getId());
            assertEquals(todo, testTodos.get(0));
            transaction.rollback();
        }
    }

    /**
     * Tests that get() returns null when an entity with specified id doesn't exist
     * */
    @Test
    public void getReturnsNull() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }
        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            assertNull(todoDao.get(-1));
            transaction.rollback();
        }
    }

    /**
     * Tests that getAll() retrieves all entities
     * */
    @Test
    public void getAllRetrievesAll() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }
        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<Todo> fetchedTodos;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            fetchedTodos = todoDao.getAll();
            transaction.rollback();
        }

        assertEquals(testTodos.size(), fetchedTodos.size());
        fetchedTodos.sort(Comparator.comparing(Todo::getTitle));
        assertEquals(testTodos, fetchedTodos);
    }

    /**
     * Tests that getAll() returns an empty List when there are no entities to retrieve
     * */
    @Test
    public void getAllReturnsEmpty() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            assertTrue(todoDao.getAll().isEmpty());
            transaction.rollback();
        }
    }

    /**
     * Tests that getCompleted() returns all completed todos and no due todos
     * */
    @Test
    public void getCompletedRetrievesCompleted() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todosCreated = 6;
        int completedTodosCount = 3;

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todosCreated; i++) {
            Todo todo = getTestTodo(i);

            // first completed_todos_count todos in testTodos are marked ready
            if (i < completedTodosCount) {
                todo.setDone(true);
            }

            testTodos.add(todo);
        }

        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        List<Todo> completedTodos;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            completedTodos = todoDao.getCompleted();
            transaction.rollback();
        }

        assertEquals(completedTodosCount, completedTodos.size());
        for (int i = 0; i < completedTodosCount; i++) {
            assertTrue(completedTodos.contains(testTodos.get(i)));
        }
    }

    /**
     * Tests that getCompleted() returns an empty list when there's no completed todos
     * */
    @Test
    public void getCompletedReturnsEmpty() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 5;
        for (int i =0; i < todoCount; i++) {
            int finalI = i;
            sessionFactory.inTransaction(session -> session.persist(getTestTodo(finalI)));
        }

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            assertTrue(todoDao.getCompleted().isEmpty());
            transaction.rollback();
        }
    }

    /**
     * Test that getDueIn() returns all todos that are due in specified period, less, or overdue
     * */
    @Test
    public void getDueInRetrievesDueInPeriod() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Period duePeriod = Period.of(0,0,14);

        List<Todo> todos = new ArrayList<>();
        int i = 0;
        Todo dueInTwoWeeks = getTestTodo(i++);
        dueInTwoWeeks.setDueDateTime(LocalDateTime.now().plusDays(14));
        todos.add(dueInTwoWeeks);

        Todo dueTomorrow = getTestTodo(i++);
        dueTomorrow.setDueDateTime(LocalDateTime.now().plusDays(1));
        todos.add(dueTomorrow);

        Todo dueNow = getTestTodo(i++);
        dueNow.setDueDateTime(LocalDateTime.now());
        todos.add(dueNow);

        Todo overdue = getTestTodo(i++);
        overdue.setDueDateTime(LocalDateTime.now().minusDays(2));
        todos.add(overdue);

        Todo dueIn15Days = getTestTodo(i++);
        dueIn15Days.setDueDateTime(LocalDateTime.now().plusDays(15));
        todos.add(dueIn15Days);

        Todo dueIn1Month = getTestTodo(i++);
        dueIn1Month.setDueDateTime(LocalDateTime.now().plusMonths(1));
        todos.add(dueIn1Month);

        Todo completedWithinPeriod = getTestTodo(i++);
        completedWithinPeriod.setDone(true);
        completedWithinPeriod.setDueDateTime(LocalDateTime.now().plusDays(3));
        todos.add(completedWithinPeriod);

        Todo completedInPast = getTestTodo(i++);
        completedInPast.setDone(true);
        completedInPast.setDueDateTime(LocalDateTime.now().minusDays(3));
        todos.add(completedInPast);

        Todo completedAfterPeriod = getTestTodo(i++);
        completedAfterPeriod.setDone(true);
        completedAfterPeriod.setDueDateTime(LocalDateTime.now().plusMonths(1));
        todos.add(completedAfterPeriod);

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        List<Todo> todosDue;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDueIn(duePeriod);
            transaction.rollback();
        }

        assertEquals(4, todosDue.size());
        assertTrue(todosDue.contains(dueInTwoWeeks));
        assertTrue(todosDue.contains(dueTomorrow));
        assertTrue(todosDue.contains(dueNow));
        assertTrue(todosDue.contains(overdue));
    }

    /**
     * Tests that getDueIn() returns an empty list when there are no overdue todos or todos due in specified period
     * */
    @Test
    public void getDueInReturnsEmpty() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Period duePeriod = Period.of(0,0,14);

        List<Todo> todos = new ArrayList<>();
        int i = 0;

        Todo dueIn15Days = getTestTodo(i++);
        dueIn15Days.setDueDateTime(LocalDateTime.now().plusDays(15));
        todos.add(dueIn15Days);

        Todo dueIn1Month = getTestTodo(i++);
        dueIn1Month.setDueDateTime(LocalDateTime.now().plusMonths(1));
        todos.add(dueIn1Month);

        Todo completedWithinPeriod = getTestTodo(i++);
        completedWithinPeriod.setDone(true);
        completedWithinPeriod.setDueDateTime(LocalDateTime.now().plusDays(3));
        todos.add(completedWithinPeriod);

        Todo completedInPast = getTestTodo(i++);
        completedInPast.setDone(true);
        completedInPast.setDueDateTime(LocalDateTime.now().minusDays(3));
        todos.add(completedInPast);

        Todo completedAfterPeriod = getTestTodo(i++);
        completedAfterPeriod.setDone(true);
        completedAfterPeriod.setDueDateTime(LocalDateTime.now().plusMonths(1));
        todos.add(completedAfterPeriod);

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        List<Todo> todosDue;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDueIn(duePeriod);
            transaction.rollback();
        }

        assertTrue(todosDue.isEmpty());
    }

    /**
     * Tests that update works correctly
     * */
    @Test
    public void updateWorksCorrectly() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Todo todo = getTestTodo();
        sessionFactory.inTransaction(session -> session.persist(todo));

        String newTitle = "An updated title";
        todo.setTitle(newTitle);

        Todo updated;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            updated = todoDao.update(todo);
            transaction.commit();
        }

        assertEquals(newTitle, updated.getTitle());
        assertEquals(todo.getId(), updated.getId());
    }
    
    /**
     * Tests that delete works correctly
     * */
    @Test
    public void deleteWorksCorrectly() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        List<Todo> todos = new ArrayList<>();
        int todoCount = 5;
        for (int i = 0; i < todoCount; i++) {
            todos.add(getTestTodo(i));
        }

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        int indexToDelete = 0;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todoDao.delete(todos.get(0));
            transaction.commit();
        }

        assertFalse(
                sessionFactory.fromTransaction(
                        session ->
                                session.createSelectionQuery("from todo", Todo.class).getResultList()
                ).contains(todos.get(0))
        );
    }

    /**
     * Tests that getDueOn() returns all todos that are due on specified date
     * */
    @Test
    public void getDueOnRetrievesDueOnDate() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        LocalDate dueDate = LocalDate.of(2026, 1, 1);
        List<Todo> todos = new ArrayList<>();

        Todo dueBefore = getTestTodo();
        dueBefore.setDueDateTime(LocalDateTime.of(dueDate.minusDays(1), LocalTime.NOON));
        todos.add(dueBefore);

        Todo dueOnDateAtMidnight = getTestTodo();
        dueOnDateAtMidnight.setDueDateTime(LocalDateTime.of(dueDate, LocalTime.of(0,0)));
        todos.add(dueOnDateAtMidnight);

        Todo dueOnDateAtNoon = getTestTodo();
        dueOnDateAtNoon.setDueDateTime(LocalDateTime.of(dueDate, LocalTime.NOON));
        todos.add(dueOnDateAtNoon);

        Todo dueOnDateBeforeMidnight = getTestTodo();
        dueOnDateBeforeMidnight.setDueDateTime(LocalDateTime.of(dueDate, LocalTime.of(23,59)));
        todos.add(dueOnDateBeforeMidnight);

        Todo dueAfter = getTestTodo();
        dueAfter.setDueDateTime(LocalDateTime.of(dueDate.plusDays(1), LocalTime.NOON));
        todos.add(dueAfter);

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        List<Todo> todosDue;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDueOn(dueDate);
            transaction.rollback();
        }

        assertEquals(3, todosDue.size());
        assertTrue(todosDue.contains(dueOnDateAtMidnight));
        assertTrue(todosDue.contains(dueOnDateAtNoon));
        assertTrue(todosDue.contains(dueOnDateBeforeMidnight));
    }

    /**
     * Tests that getDueOn() returns an empty list when there are no todos due on the passed date
     * */
    @Test
    public void getDueOnReturnsEmpty() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        LocalDate dueDate = LocalDate.of(2026, 1, 1);
        List<Todo> todos = new ArrayList<>();

        Todo dueBefore = getTestTodo();
        dueBefore.setDueDateTime(LocalDateTime.of(dueDate.minusDays(1), LocalTime.NOON));
        todos.add(dueBefore);

        Todo dueAfter = getTestTodo();
        dueAfter.setDueDateTime(LocalDateTime.of(dueDate.plusDays(1), LocalTime.NOON));
        todos.add(dueAfter);

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        List<Todo> todosDue;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDueOn(dueDate);
            transaction.rollback();
        }

        assertTrue(todosDue.isEmpty());
    }

    /**
     * Tests that getDue() retrieves all todos that are due
     * */
    @Test
    public void getDueRetrievesAllDue() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 6;
        int dueTodoCount = 2;

        List<Todo> todos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            todos.add(getTestTodo(i));

            if (i >= dueTodoCount) {
                todos.get(i).setDone(true);
            }
        }

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        List<Todo> todosDue;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDue();
            transaction.rollback();
        }

        assertEquals(dueTodoCount, todosDue.size());
    }

    /**
     * Tests that getDue() returns an empty list when there are no todos due
     * */
    @Test
    public void getDueReturnsEmpty() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 6;

        List<Todo> todos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            Todo todo = getTestTodo(i);
            todo.setDone(true);

            todos.add(todo);
        }

        sessionFactory.inTransaction(session -> todos.forEach(session::persist));

        List<Todo> todosDue;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDue();
            transaction.rollback();
        }

        assertTrue(todosDue.isEmpty());
    }
}
