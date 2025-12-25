package service;

import jakarta.transaction.Transactional;
import org.pileka.dto.TodoDto;
import org.pileka.mapper.TodoMapper;
import org.pileka.model.Todo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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

        assertEquals(TodoMapper.toModel(todoDto), todos.get(0));
    }
}
