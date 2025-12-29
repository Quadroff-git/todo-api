import org.pileka.dto.TodoDto;
import org.pileka.model.Todo;

import java.time.LocalDateTime;

public abstract class AbstractTodoTest {
    protected Todo getTestTodo() {
        Todo testTodo = new Todo();
        testTodo.setDueDateTime(LocalDateTime.now());
        testTodo.setTitle("Test todo");

        return testTodo;
    }

    protected Todo getTestTodo(int i) {
        Todo testTodo = new Todo();
        testTodo.setDueDateTime(LocalDateTime.now());
        testTodo.setTitle("Test todo" + i);

        return testTodo;
    }

    protected TodoDto getTestTodoDto() {
        TodoDto testTodoDto = new TodoDto();
        testTodoDto.setDueDateTime(LocalDateTime.now());
        testTodoDto.setTitle("Test todo dto");

        return testTodoDto;
    }

    protected TodoDto getTestTodoDto(int i) {
        TodoDto testTodoDto = new TodoDto();
        testTodoDto.setDueDateTime(LocalDateTime.now());
        testTodoDto.setTitle("Test todo dto " + i);

        return testTodoDto;
    }
}
