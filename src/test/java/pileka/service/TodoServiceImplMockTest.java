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
    public void getByIdRetrievesEntity() {
        long id = 1L;

        TodoDto toReturn = new TodoDto(id, false, LocalDateTime.now(), "Test todo title", "Test todo description");

        when(todoDao.getById(id)).thenReturn(TodoMapper.toModel(toReturn));

        assertEquals(toReturn, todoService.getById(id));
        verify(todoDao).getById(id);
    }

    @Test
    public void getByIdReturnsNullWhenEntityNotFound() {
        long nonExistingId = 420;

        when(todoDao.getById(nonExistingId)).thenReturn(null);

        assertNull(todoService.getById(nonExistingId));
        verify(todoDao).getById(nonExistingId);
    }
}
