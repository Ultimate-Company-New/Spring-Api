package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Models.ResponseModels.TodoResponseModel;
import com.example.SpringApi.Repositories.TodoRepository;
import com.example.SpringApi.Services.Interface.ITodoSubTranslator;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Todo operations.
 * 
 * This service handles all business logic related to todo items
 * including CRUD operations and task management functionality.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class TodoService extends BaseService implements ITodoSubTranslator {
    
    private final TodoRepository todoRepository;
    private final UserLogService userLogService;
    
    @Autowired
    public TodoService(TodoRepository todoRepository, UserLogService userLogService, HttpServletRequest request) {
        super();
        this.todoRepository = todoRepository;
        this.userLogService = userLogService;
    }
    
    /**
     * Adds a new todo item.
     * 
     * @param todoRequestModel The todo request model containing todo data
     * @throws BadRequestException if the request is null or invalid
     */
    @Override
    public void addTodo(TodoRequestModel todoRequestModel) {
        String authenticatedUser = getUser();
        Todo todo = new Todo(todoRequestModel, authenticatedUser, getUserId());
        Todo savedTodo = todoRepository.save(todo);
        userLogService.logData(
            getUserId(),
            SuccessMessages.TodoSuccessMessages.InsertTodo + " " + savedTodo.getTodoId(),
            ApiRoutes.TodoSubRoute.ADD_ITEM
        );
    }
    
    /**
     * Updates an existing todo item.
     * 
     * @param todoRequestModel The todo request model containing updated todo data
     * @throws NotFoundException if the todo item is not found
     * @throws BadRequestException if the request is invalid
     */
    @Override
    public void updateTodo(TodoRequestModel todoRequestModel) {
        if (todoRequestModel == null || todoRequestModel.getTodoId() == null) {
            throw new BadRequestException(ErrorMessages.TodoErrorMessages.InvalidId);
        }
        
        String authenticatedUser = getUser();
        
        Todo todoToUpdate = todoRepository.findById(todoRequestModel.getTodoId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.TodoErrorMessages.InvalidId));
        
        Todo updatedTodo = new Todo(todoRequestModel, authenticatedUser, todoToUpdate, getUserId());
        userLogService.logData(
            getUserId(),
            SuccessMessages.TodoSuccessMessages.UpdateTodo + " " + todoRepository.save(updatedTodo).getTodoId(),
            ApiRoutes.TodoSubRoute.UPDATE_ITEM
        );
    }
    
    /**
     * Deletes a todo item by ID.
     * 
     * @param id The ID of the todo item to delete
     * @throws NotFoundException if the todo item is not found
     */
    @Override
    public void deleteTodo(long id) {
        // Check if todo exists
        if (!todoRepository.existsById(id)) {
            throw new NotFoundException(ErrorMessages.TodoErrorMessages.InvalidId);
        }
        
        // Delete the todo
        todoRepository.deleteById(id);
        
        // Log the operation
        userLogService.logData(
            getUserId(),
            SuccessMessages.TodoSuccessMessages.DeleteTodo + " " + id,
            ApiRoutes.TodoSubRoute.DELETE_ITEM
        );
    }
    
    /**
     * Toggles the completion status of a todo item.
     * 
     * @param id The ID of the todo item to toggle
     * @throws NotFoundException if the todo item is not found
     */
    @Override
    public void toggleTodo(long id) {
        String authenticatedUser = getUser();
        
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.TodoErrorMessages.InvalidId));
        
        // Toggle the completion status
        todo.setIsDone(!todo.getIsDone());
        todo.setModifiedUser(authenticatedUser);
        
        // Save the updated todo
        todoRepository.save(todo);
        
        // Log the operation
        userLogService.logData(
            getUserId(),
            SuccessMessages.TodoSuccessMessages.ToggleTodo + " " + id,
            ApiRoutes.TodoSubRoute.TOGGLE_DONE
        );
    }
    
    /**
     * Retrieves all todo items for the current user.
     * 
     * @return List of TodoResponseModel objects for the current user
     */
    @Override
    public List<TodoResponseModel> getTodoItems() {
        List<Todo> todos = todoRepository.findAllByUserIdOrderByTodoIdDesc(getUserId());
        
        // Convert to response models
        List<TodoResponseModel> responseModels = todos.stream()
            .map(TodoResponseModel::new)
            .collect(Collectors.toList());
        
        // Log the operation
        userLogService.logData(
            getUserId(),
            SuccessMessages.TodoSuccessMessages.GetTodoItems,
            ApiRoutes.TodoSubRoute.GET_ITEMS
        );
        
        return responseModels;
    }
}