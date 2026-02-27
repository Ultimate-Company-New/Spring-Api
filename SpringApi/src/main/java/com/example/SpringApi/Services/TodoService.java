package com.example.springapi.services;

import com.example.springapi.ErrorMessages;
import com.example.springapi.SuccessMessages;
import com.example.springapi.authentication.JwtTokenProvider;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.databasemodels.Todo;
import com.example.springapi.models.requestmodels.TodoRequestModel;
import com.example.springapi.models.responsemodels.TodoResponseModel;
import com.example.springapi.repositories.TodoRepository;
import com.example.springapi.services.interfaces.TodoSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Represents the todo service component.
 */
@Service
public class TodoService extends BaseService implements TodoSubTranslator {

  private final TodoRepository todoRepository;
  private final UserLogService userLogService;

  /**
   * Initializes TodoService.
   */
  @Autowired
  public TodoService(
      TodoRepository todoRepository,
      UserLogService userLogService,
      HttpServletRequest request,
      JwtTokenProvider jwtTokenProvider) {
    super(jwtTokenProvider, request);
    this.todoRepository = todoRepository;
    this.userLogService = userLogService;
  }

  @Override
  public void addTodo(TodoRequestModel todoRequestModel) {
    String authenticatedUser = getUser();
    Todo todo = new Todo(todoRequestModel, authenticatedUser, getUserId());
    Todo savedTodo = todoRepository.save(todo);
    userLogService.logData(
        getUserId(),
        SuccessMessages.TodoSuccessMessages.INSERT_TODO + " " + savedTodo.getTodoId(),
        ApiRoutes.TodoSubRoute.ADD_ITEM);
  }

  @Override
  public void updateTodo(TodoRequestModel todoRequestModel) {
    if (todoRequestModel == null) {
      throw new BadRequestException(ErrorMessages.TodoErrorMessages.INVALID_REQUEST);
    }

    if (todoRequestModel.getTodoId() == null) {
      throw new BadRequestException(ErrorMessages.TodoErrorMessages.INVALID_REQUEST);
    }

    String task = todoRequestModel.getTask();
    if (task == null || task.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.TodoErrorMessages.INVALID_TASK);
    }

    if (task.length() > 500) {
      throw new BadRequestException(ErrorMessages.TodoErrorMessages.TASK_TOO_LONG);
    }

    String authenticatedUser = getUser();

    Todo todoToUpdate =
        todoRepository
            .findById(todoRequestModel.getTodoId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.TodoErrorMessages.INVALID_ID));

    Todo updatedTodo = new Todo(todoRequestModel, authenticatedUser, todoToUpdate, getUserId());
    userLogService.logData(
        getUserId(),
        SuccessMessages.TodoSuccessMessages.UPDATE_TODO
            + " "
            + todoRepository.save(updatedTodo).getTodoId(),
        ApiRoutes.TodoSubRoute.UPDATE_ITEM);
  }

  @Override
  public void deleteTodo(long id) {
    if (!todoRepository.existsById(id)) {
      throw new NotFoundException(ErrorMessages.TodoErrorMessages.INVALID_ID);
    }

    todoRepository.deleteById(id);

    userLogService.logData(
        getUserId(),
        SuccessMessages.TodoSuccessMessages.DELETE_TODO + " " + id,
        ApiRoutes.TodoSubRoute.DELETE_ITEM);
  }

  @Override
  public void toggleTodo(long id) {
    String authenticatedUser = getUser();

    Todo todo =
        todoRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.TodoErrorMessages.INVALID_ID));

    todo.setIsDone(!todo.getIsDone());
    todo.setModifiedUser(authenticatedUser);

    todoRepository.save(todo);

    userLogService.logData(
        getUserId(),
        SuccessMessages.TodoSuccessMessages.TOGGLE_TODO + " " + id,
        ApiRoutes.TodoSubRoute.TOGGLE_DONE);
  }

  @Override
  public List<TodoResponseModel> getTodoItems() {
    List<Todo> todos = todoRepository.findAllByUserIdOrderByTodoIdDesc(getUserId());

    if (todos == null) {
      throw new BadRequestException(ErrorMessages.TodoErrorMessages.INVALID_REQUEST);
    }

    List<TodoResponseModel> responseModels =
        todos.stream().map(TodoResponseModel::new).collect(Collectors.toCollection(ArrayList::new));

    userLogService.logData(
        getUserId(),
        SuccessMessages.TodoSuccessMessages.GET_TODO_ITEMS,
        ApiRoutes.TodoSubRoute.GET_ITEMS);

    return responseModels;
  }
}
