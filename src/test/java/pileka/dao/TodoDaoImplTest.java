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
        sessionFactory.inTransaction(session -> session.createMutationQuery("delete todo").executeUpdate());
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
     * Tests that getById() retrieves an entity by id when such an entity exists
     * */
    @Test
    public void getByIdRetrievesEntity() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }
        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            Todo todo = todoDao.getById(testTodos.get(0).getId());
            assertEquals(todo, testTodos.get(0));
            transaction.rollback();
        }
    }

    /**
     * Tests that getById() returns null when an entity with specified id doesn't exist
     * */
    @Test
    public void getByIdReturnsNull() {
        TodoDao todoDao = new TodoDaoImpl(sessionFactory);

        int todoCount = 5;
        List<Todo> testTodos = new ArrayList<>();
        for (int i = 0; i < todoCount; i++) {
            testTodos.add(getTestTodo(i));
        }
        sessionFactory.inTransaction(session -> testTodos.forEach(session::persist));

        try (Session session = sessionFactory.getCurrentSession()) {
            Transaction transaction = session.beginTransaction();
            assertNull(todoDao.getById(-1));
            transaction.rollback();
        }
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
}
