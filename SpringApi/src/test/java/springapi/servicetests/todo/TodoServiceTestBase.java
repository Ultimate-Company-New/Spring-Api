package springapi.ServiceTests.Todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import springapi.controllers.TodoController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.models.databasemodels.Todo;
import springapi.models.requestmodels.TodoRequestModel;
import springapi.repositories.TodoRepository;
import springapi.services.TodoService;
import springapi.services.UserLogService;
import springapi.services.interfaces.TodoSubTranslator;

@ExtendWith(MockitoExtension.class)
abstract class TodoServiceTestBase {

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_TODO_ID = 1L;
  protected static final String DEFAULT_TODO_TITLE = "Test Todo";
  protected static final Boolean DEFAULT_TODO_COMPLETED = false;
  protected static final Long DEFAULT_USER_ID = 1L;
  protected static final String DEFAULT_CREATED_USER = "admin";

  @Mock protected TodoRepository todoRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected HttpServletRequest request;

  @Mock protected TodoSubTranslator todoServiceMock;

  @InjectMocks protected TodoService todoService;

  protected Todo testTodo;
  protected TodoRequestModel validRequest;

  protected TodoController todoController;

  protected static final Long TEST_TODO_ID = DEFAULT_TODO_ID;
  protected static final Long TEST_USER_ID = DEFAULT_USER_ID;
  protected static final String TEST_TASK = DEFAULT_TODO_TITLE;
  protected static final String CREATED_USER = DEFAULT_CREATED_USER;
  protected static final String LONG_TASK = "A".repeat(501);

  @BeforeEach
  protected void setUp() {
    validRequest = createValidTodoRequest();
    validRequest.setTodoId(TEST_TODO_ID);
    validRequest.setTask(TEST_TASK);
    validRequest.setIsDone(false);

    testTodo = createTestTodo();
    testTodo.setTodoId(TEST_TODO_ID);
    testTodo.setUserId(TEST_USER_ID);
    testTodo.setCreatedAt(LocalDateTime.now());
    testTodo.setUpdatedAt(LocalDateTime.now());

    todoController = new TodoController(todoServiceMock);
  }

  // ==========================================
  // STUBS
  // ==========================================

  protected void stubTodoRepositoryExistsById(Long id, boolean exists) {
    lenient().when(todoRepository.existsById(id)).thenReturn(exists);
  }

  protected void stubTodoRepositoryExistsByIdAny(boolean exists) {
    lenient().when(todoRepository.existsById(anyLong())).thenReturn(exists);
  }

  protected void stubTodoRepositorySave(Todo returnTodo) {
    lenient().when(todoRepository.save(any(Todo.class))).thenReturn(returnTodo);
  }

  protected void stubTodoRepositoryFindById(Long id, Optional<Todo> result) {
    lenient().when(todoRepository.findById(id)).thenReturn(result);
  }

  protected void stubTodoRepositoryFindByIdAny(Optional<Todo> result) {
    lenient().when(todoRepository.findById(anyLong())).thenReturn(result);
  }

  protected void stubTodoRepositoryDeleteById(Long id) {
    lenient().doNothing().when(todoRepository).deleteById(id);
  }

  protected void stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List<Todo> result) {
    lenient().when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(result);
  }

  protected void stubUserLogServiceLogDataReturnsTrue() {
    lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
  }

  protected void stubTodoServiceAddTodoThrowsUnauthorized() {
    lenient()
        .doThrow(
            new springapi.exceptions.UnauthorizedException(
                springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(todoServiceMock)
        .addTodo(any(TodoRequestModel.class));
  }

  protected void stubTodoServiceUpdateTodoThrowsUnauthorized() {
    lenient()
        .doThrow(
            new springapi.exceptions.UnauthorizedException(
                springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(todoServiceMock)
        .updateTodo(any(TodoRequestModel.class));
  }

  protected void stubTodoServiceGetTodoItemsThrowsUnauthorized() {
    lenient()
        .doThrow(
            new springapi.exceptions.UnauthorizedException(
                springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(todoServiceMock)
        .getTodoItems();
  }

  protected void stubTodoServiceDeleteTodoThrowsUnauthorized() {
    lenient()
        .doThrow(
            new springapi.exceptions.UnauthorizedException(
                springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(todoServiceMock)
        .deleteTodo(anyLong());
  }

  protected void stubTodoServiceToggleTodoThrowsUnauthorized() {
    lenient()
        .doThrow(
            new springapi.exceptions.UnauthorizedException(
                springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(todoServiceMock)
        .toggleTodo(anyLong());
  }

  // ==================== FACTORY METHODS ====================

  protected TodoRequestModel createValidTodoRequest() {
    return createValidTodoRequest(DEFAULT_TODO_ID);
  }

  protected TodoRequestModel createValidTodoRequest(Long todoId) {
    TodoRequestModel todoRequest = new TodoRequestModel();
    todoRequest.setTodoId(todoId);
    todoRequest.setTask(DEFAULT_TODO_TITLE);
    todoRequest.setIsDone(DEFAULT_TODO_COMPLETED);
    return todoRequest;
  }

  protected Todo createTestTodo() {
    return createTestTodo(createValidTodoRequest(), DEFAULT_CREATED_USER);
  }

  protected Todo createTestTodo(TodoRequestModel todoRequest, String createdUser) {
    Todo todo = new Todo();
    todo.setTodoId(todoRequest.getTodoId());
    todo.setTask(todoRequest.getTask());
    todo.setIsDone(todoRequest.getIsDone());
    todo.setCreatedUser(createdUser);
    todo.setCreatedAt(LocalDateTime.now());
    todo.setUpdatedAt(LocalDateTime.now());
    return todo;
  }

  protected void assertThrowsBadRequest(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    BadRequestException ex = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }

  protected void assertThrowsNotFound(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    NotFoundException ex = assertThrows(NotFoundException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }
}
