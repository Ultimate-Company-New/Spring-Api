package springapi.exceptions;

/** Represents the not found exception component. */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
