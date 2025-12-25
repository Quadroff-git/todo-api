package dao;

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
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test class for testing the DAO using an in-memory H2 database.
 * No Spring IOC container is used, Hibernate configuration and SessionFactory dependency injection is manual
 */
public class TodoDaoImplTest {
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

    @Test
    public void testCreate() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);
        Todo testTodo = new Todo();
        testTodo.setDueDateTime(LocalDateTime.now());
        testTodo.setTitle("Test todo");

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();

            todoDao.create(testTodo);
            Long id = testTodo.getId();
            assertNotNull(id);
            transaction.commit();
        }

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Todo todo = session.find(Todo.class, testTodo.getId());
            assertEquals(todo, testTodo);
            transaction.commit();
        }
    }

    @Test
    public void testGet() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Todo testTodo = new Todo();
        testTodo.setDueDateTime(LocalDateTime.now());
        testTodo.setTitle("Test todo");

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(testTodo);
            Todo todo = todoDao.get(testTodo.getId());
            assertEquals(todo, testTodo);
            transaction.commit();
        }
    }

    @Test
    public void testGetAll() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Todo todo = new Todo();
            todo.setTitle("Test todo " + i);
            todo.setDueDateTime(LocalDateTime.now());
            testTodos.add(todo);
        }

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            testTodos.forEach(session::persist);
            transaction.commit();
        }

        List<Todo> fetchedTodos = null;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            fetchedTodos = todoDao.getAll();
        }

        assertEquals(testTodos.size(), fetchedTodos.size());
        fetchedTodos.sort(Comparator.comparing(Todo::getTitle));

        for (int i = 0; i < testTodos.size(); i++) {
            assertEquals(testTodos.get(i), fetchedTodos.get(i));
        }
    }

    @Test
    public void testMarkCompleted() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Todo testTodo = new Todo();
        testTodo.setDueDateTime(LocalDateTime.now());
        testTodo.setTitle("Test todo");
        assertFalse(testTodo.isDone());

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(testTodo);
            transaction.commit();
        }

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Todo fetchedTodo = session.find(Todo.class, testTodo.getId());

            assertFalse(fetchedTodo.isDone());

            todoDao.markCompleted(fetchedTodo.getId());
            transaction.commit();
        }

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Todo fetchedTodo = session.find(Todo.class, testTodo.getId());

            assertTrue(fetchedTodo.isDone());
        }
    }

    @Test
    public void testGetCompleted() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todos_created = 6;
        int completed_todos_count = 3;

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todos_created; i++) {
            Todo todo = new Todo();
            todo.setTitle("Test todo " + i);
            todo.setDueDateTime(LocalDateTime.now());
            testTodos.add(todo);
        }
        for (int i = 0; i < completed_todos_count; i++) {
            testTodos.get(i).setDone(true);
        }

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            testTodos.forEach(session::persist);
            transaction.commit();
        }

        List<Todo> completedTodos = new ArrayList<>();
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            completedTodos = todoDao.getCompleted();
        }

        assertEquals(completed_todos_count, completedTodos.size());
    }

    @Test
    public void testGetDueIn() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        // todo1 and todo2 are supposed to be returned
        Todo todo1 = new Todo();
        todo1.setTitle("Test todo 1");
        todo1.setDueDateTime(LocalDateTime.of(2025, 12, 24, 20, 45));

        Todo todo2 = new Todo();
        todo2.setTitle("Test todo 2");
        todo2.setDueDateTime(LocalDateTime.of(2026, 1, 1, 12, 0));

        Todo todo3 = new Todo();
        todo3.setTitle("Test todo 3");
        todo3.setDueDateTime(LocalDateTime.of(2025, 12, 22, 12, 0));

        Todo todo4 = new Todo();
        todo4.setTitle("Test todo 4");
        todo4.setDueDateTime(LocalDateTime.of(2026, 1, 14, 12, 0));

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(todo1);
            session.persist(todo2);
            session.persist(todo3);
            session.persist(todo4);
            transaction.commit();
        }

        List<Todo> todosDue = new ArrayList<>();
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDueIn(Period.of(0, 0, 14));
        }

        assertEquals(3, todosDue.size());
        assertTrue(todosDue.contains(todo1));
        assertTrue(todosDue.contains(todo2));
        assertTrue(todosDue.contains(todo3));
    }

    @Test
    public void testUpdate() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Todo todo = new Todo();
        todo.setTitle("Test todo");
        todo.setDueDateTime(LocalDateTime.now());

        // A much cleaner way of handling manual transaction interactions
        sessionFactory.inTransaction(session -> {
            session.persist(todo);
        });

        String newTitle = "An updated title";
        todo.setTitle(newTitle);

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todoDao.update(todo);
            transaction.commit();
        }

        Todo updated = sessionFactory.fromTransaction(session -> session.find(Todo.class, todo.getId()));

        assertEquals(newTitle, updated.getTitle());
        assertEquals(todo.getId(), updated.getId());
    }

    @Test
    public void testDelete() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        Todo todo = new Todo();
        todo.setTitle("Test todo");
        todo.setDueDateTime(LocalDateTime.now());

        sessionFactory.inTransaction(session -> {
            session.persist(todo);
        });

        assertNotNull(sessionFactory.fromTransaction(session -> session.find(Todo.class, todo.getId())));

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Todo fethchedTodo = session.find(Todo.class, todo.getId());
            todoDao.delete(fethchedTodo);
            transaction.commit();
        }

        assertNull(sessionFactory.fromTransaction(session -> session.find(Todo.class, todo.getId())));
    }

    @Test
    public void testGetDueOn() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        // todo1 and todo2 are supposed to be returned
        Todo todo1 = new Todo();
        todo1.setTitle("Test todo 1");
        todo1.setDueDateTime(LocalDateTime.of(2025, 12, 22, 20, 45));

        Todo todo2 = new Todo();
        todo2.setTitle("Test todo 2");
        todo2.setDueDateTime(LocalDateTime.of(2026, 1, 1, 12, 0));

        Todo todo3 = new Todo();
        todo3.setTitle("Test todo 3");
        todo3.setDueDateTime(LocalDateTime.of(2025, 12, 22, 12, 0));

        Todo todo4 = new Todo();
        todo4.setTitle("Test todo 4");
        todo4.setDueDateTime(LocalDateTime.of(2026, 1, 14, 12, 0));

        sessionFactory.inTransaction(session -> {
            session.persist(todo1);
            session.persist(todo2);
            session.persist(todo3);
            session.persist(todo4);
        });

        List<Todo> todosDue = null;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            todosDue = todoDao.getDueOn(LocalDate.of(2025, 12, 22));
            transaction.commit();
        }

        assertEquals(2, todosDue.size());
        assertTrue(todosDue.contains(todo1));
        assertTrue(todosDue.contains(todo3));
    }

    @Test
    public void testGetDue() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todos_created = 6;
        int due_todos_count = 2;

        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todos_created; i++) {
            Todo todo = new Todo();
            todo.setTitle("Test todo " + i);
            todo.setDueDateTime(LocalDateTime.now());
            testTodos.add(todo);
        }
        for (int i = 0; i < todos_created - due_todos_count; i++) {
            testTodos.get(i).setDone(true);
        }

        sessionFactory.inTransaction(session -> {
            testTodos.forEach(session::persist);
        });

        List<Todo> completedTodos = null;
        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            completedTodos = todoDao.getDue();
        }

        assertEquals(due_todos_count, completedTodos.size());
    }
}
