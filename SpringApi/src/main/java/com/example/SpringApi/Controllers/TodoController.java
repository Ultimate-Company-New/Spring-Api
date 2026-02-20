package com.example.SpringApi.Controllers;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Services.Interface.ITodoSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
public class TodoController {

  private static final ContextualLogger logger = ContextualLogger.getLogger(TodoController.class);
  private final ITodoSubTranslator todoService;

  @Autowired
  public TodoController(ITodoSubTranslator todoService) {
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
