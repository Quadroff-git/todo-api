package pileka.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pileka.dao.TodoDao;
import org.pileka.dto.TodoDto;
import org.pileka.mapper.TodoMapper;
import org.pileka.model.Todo;
import org.pileka.service.impl.TodoServiceImpl;
import pileka.AbstractTodoTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * A pure unit test class using Mockito
 * */
@ExtendWith(MockitoExtension.class)
public class TodoServiceImplMockTest extends AbstractTodoTest {
    @Mock
    private TodoDao todoDao;

    @InjectMocks
    private TodoServiceImpl todoService;

    @Test
    public void createPersistsWithNullId() {
        long id = 1L;
        when(todoDao.create(any(Todo.class))).thenAnswer(args -> {
            Todo created = args.getArgument(0);
            created.setId(id);
            return created;
        });

        TodoDto dtoToCreate = getTestTodoDto();

        TodoDto createdDto = todoService.create(dtoToCreate);
        verify(todoDao).create(any(Todo.class));

        assertEquals(id, createdDto.getId());
        assertEquals(dtoToCreate.getTitle(), createdDto.getTitle());
        assertEquals(dtoToCreate.getDueDateTime(), createdDto.getDueDateTime());
    }

    @Test
    public void createThrowsIllegalArgumentExceptionForNonNullId() {
        TodoDto testDto = getTestTodoDto();
        testDto.setId(69L);

        assertThrows(IllegalArgumentException.class, () -> todoService.create(testDto));
        verify(todoDao, never()).create(any(Todo.class));
    }

    @Test
    public void getRetrievesEntity() {
        long id = 1L;

        TodoDto toReturn = new TodoDto(id, false, LocalDateTime.now(), "Test todo title", "Test todo description");

        when(todoDao.get(id)).thenReturn(TodoMapper.toModel(toReturn));

        assertEquals(toReturn, todoService.get(id));
        verify(todoDao).get(id);
    }

    @Test
    public void getReturnsNullWhenEntityNotFound() {
        long nonExistingId = 420;

        when(todoDao.get(nonExistingId)).thenReturn(null);

        assertNull(todoService.get(nonExistingId));
        verify(todoDao).get(nonExistingId);
    }

    @Test
    public void getAllRetrievesAll() {
        int listSize = 5;
        List<Todo> toReturn = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            Todo t = getTestTodo(i);
            t.setId((long) i);
            toReturn.add(t);
        }
        // Shuffling since the dao implementation doesn't explicitly add any sorting to the query
        Collections.shuffle(toReturn);

        when(todoDao.getAll()).thenReturn(toReturn);

        List<TodoDto> expected = toReturn.stream()
                .map(TodoMapper::toDto)
                .sorted(Comparator.comparing(TodoDto::getId))
                .toList();

        List<TodoDto> retrieved = todoService.getAll().stream()
                .sorted(Comparator.comparing(TodoDto::getId)).toList();

        verify(todoDao).getAll();

        assertEquals(expected, retrieved);
    }

    @Test
    public void getAllReturnsEmpty() {
        when(todoDao.getAll()).thenReturn(new ArrayList<>());
        assertTrue(todoService.getAll().isEmpty());
        verify(todoDao).getAll();
    }
}
