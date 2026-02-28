package springapi.exceptions;

/** Represents the permission exception component. */
public class PermissionException extends RuntimeException {

  public PermissionException(String message) {
    super(message);
  }
}
