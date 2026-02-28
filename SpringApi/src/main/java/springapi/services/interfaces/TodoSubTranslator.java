package springapi.services.interfaces;

import java.util.List;
import springapi.models.requestmodels.TodoRequestModel;
import springapi.models.responsemodels.TodoResponseModel;

/**
 * Interface for task item operations and data access.
 *
 * <p>This interface defines the contract for all task-item-related business operations including
 * CRUD operations and task management functionality.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface TodoSubTranslator {

  /**
   * Adds a new task item.
   *
   * @param todoRequestModel The request model containing task item data
   * @throws NotFoundException if required dependencies are not found
   */
  void addTodo(TodoRequestModel todoRequestModel);

  /**
   * Updates an existing task item.
   *
   * @param todoRequestModel The request model containing updated task item data
   * @throws NotFoundException if the task item is not found
   */
  void updateTodo(TodoRequestModel todoRequestModel);

  /**
   * Deletes a task item by ID.
   *
   * @param id The ID of the task item to delete
   * @throws NotFoundException if the task item is not found
   */
  void deleteTodo(long id);

  /**
   * Toggles the completion status of a task item.
   *
   * @param id The ID of the task item to toggle
   * @throws NotFoundException if the task item is not found
   */
  void toggleTodo(long id);

  /**
   * Retrieves all task items for the current user.
   *
   * @return List of task items
   */
  List<TodoResponseModel> getTodoItems();
}
