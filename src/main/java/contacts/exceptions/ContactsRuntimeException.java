package contacts.exceptions;

public class ContactsRuntimeException extends RuntimeException {
  public ContactsRuntimeException(String message) {
    super(message);
  }

  public ContactsRuntimeException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
