package dao;

import dao.impl.TodoDaoImpl;
import model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

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
    public static void shutDown() {
        sessionFactory.close();
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
            assertEquals(session.createSelectionQuery("from todo", Todo.class).getSingleResult().getTitle(), testTodo.getTitle());
        }
    }
}
