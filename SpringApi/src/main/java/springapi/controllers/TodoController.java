package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.requestmodels.TodoRequestModel;
import springapi.services.interfaces.TodoSubTranslator;

/**
 * REST controller for task item operations.
 *
 * <p>This controller handles all HTTP requests related to task item management including creating,
 * reading, updating, deleting, and managing task items. All endpoints require token validation for
 * access.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.TODO)
public class TodoController extends BaseController {

  private static final ContextualLogger logger = ContextualLogger.getLogger(TodoController.class);
  private final TodoSubTranslator todoService;

  @Autowired
  public TodoController(TodoSubTranslator todoService) {
    this.todoService = todoService;
  }

  /**
   * Adds a new task item.
   *
   * <p>This endpoint creates a new task item for the authenticated user. The request object should
   * contain the task description and other relevant details.
   *
   * @return ResponseEntity containing the ID of the newly created task item or error
   */
  @PutMapping("/" + ApiRoutes.TodoSubRoute.ADD_ITEM)
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  public ResponseEntity<?> addItem(@RequestBody TodoRequestModel todoRequest) {
    try {
      todoService.addTodo(todoRequest);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Updates an existing task item.
   *
   * <p>This endpoint updates an existing task item for the authenticated user. The request object
   * should contain the ID and updated task details.
   *
   * @param todoRequest The task item to update
   * @return ResponseEntity containing success status or error
   */
  @PostMapping("/" + ApiRoutes.TodoSubRoute.UPDATE_ITEM)
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  public ResponseEntity<?> updateItem(@RequestBody TodoRequestModel todoRequest) {
    try {
      todoService.updateTodo(todoRequest);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Deletes a task item by ID.
   *
   * <p>This endpoint permanently removes a task item from the system. Only the owner of the task
   * item can delete it.
   *
   * @param id The ID of the task item to delete
   * @return ResponseEntity containing success status or error
   */
  @DeleteMapping("/" + ApiRoutes.TodoSubRoute.DELETE_ITEM + "/{id}")
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  public ResponseEntity<?> deleteItem(@PathVariable long id) {
    try {
      todoService.deleteTodo(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Toggles the completion status of a task item.
   *
   * <p>This endpoint changes the isDone status of a task item. If the item is completed, it will be
   * marked as pending and vice versa.
   *
   * @param id The ID of the task item to toggle
   * @return ResponseEntity containing success status or error
   */
  @DeleteMapping("/" + ApiRoutes.TodoSubRoute.TOGGLE_DONE + "/{id}")
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  public ResponseEntity<?> toggleTodo(@PathVariable long id) {
    try {
      todoService.toggleTodo(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves all task items for the authenticated user.
   *
   * <p>This endpoint returns a list of all task items belonging to the current user. Both completed
   * and pending task items are included in the response.
   *
   * @return ResponseEntity containing list of task items or error
   */
  @GetMapping("/" + ApiRoutes.TodoSubRoute.GET_ITEMS)
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  public ResponseEntity<?> getTodoItems() {
    try {
      return ResponseEntity.ok(todoService.getTodoItems());
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
