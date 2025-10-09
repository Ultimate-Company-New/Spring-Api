package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Models.ResponseModels.TodoResponseModel;
import java.util.List;

/**
 * Interface for Todo operations and data access.
 * 
 * This interface defines the contract for all todo-related business operations
 * including CRUD operations and task management functionality.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface ITodoSubTranslator {
    
    /**
     * Adds a new todo item.
     * 
     * @param todoRequestModel The todo request model containing todo data
     * @throws NotFoundException if required dependencies are not found
     */
    void addTodo(TodoRequestModel todoRequestModel);
    
    /**
     * Updates an existing todo item.
     * 
     * @param todoRequestModel The todo request model containing updated todo data
     * @throws NotFoundException if the todo item is not found
     */
    void updateTodo(TodoRequestModel todoRequestModel);
    
    /**
     * Deletes a todo item by ID.
     * 
     * @param id The ID of the todo item to delete
     * @throws NotFoundException if the todo item is not found
     */
    void deleteTodo(long id);
    
    /**
     * Toggles the completion status of a todo item.
     * 
     * @param id The ID of the todo item to toggle
     * @throws NotFoundException if the todo item is not found
     */
    void toggleTodo(long id);
    
    /**
     * Retrieves all todo items for the current user.
     * 
     * @return List of todo items
     */
    List<TodoResponseModel> getTodoItems();
}