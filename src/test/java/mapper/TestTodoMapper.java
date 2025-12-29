package mapper;

import org.junit.jupiter.api.Test;
import org.pileka.dto.TodoDto;
import org.pileka.mapper.TodoMapper;
import org.pileka.model.Todo;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestTodoMapper {
    @Test
    public void toDtoReturnsNull() {
        assertNull(TodoMapper.toDto(null));
    }

    @Test
    public void toModelReturnsNull() {
        assertNull(TodoMapper.toModel(null));
    }

    @Test
    public void toDtoMapsCorrectly() {
        Todo todo = new Todo(1L, false, LocalDateTime.now(), "Test todo", "This is a test todo");

        TodoDto todoDto = TodoMapper.toDto(todo);

        assertEquals(todo.getId(), todoDto.getId());
        assertEquals(todo.isDone(), todoDto.isDone());
        assertEquals(todo.getDueDateTime(), todoDto.getDueDateTime());
        assertEquals(todo.getTitle(), todoDto.getTitle());
        assertEquals(todo.getDescription(), todoDto.getDescription());
    }

    @Test
    public void toModelMapsCorrectly() {
        TodoDto todoDto = new TodoDto(1L, false, LocalDateTime.now(), "Test todo", "This is a test todo");

        Todo todo = TodoMapper.toModel(todoDto);

        assertEquals(todo.getId(), todoDto.getId());
        assertEquals(todo.isDone(), todoDto.isDone());
        assertEquals(todo.getDueDateTime(), todoDto.getDueDateTime());
        assertEquals(todo.getTitle(), todoDto.getTitle());
        assertEquals(todo.getDescription(), todoDto.getDescription());
    }
}
