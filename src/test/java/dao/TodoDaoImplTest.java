package dao;

import dao.impl.TodoDaoImpl;
import model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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
}
