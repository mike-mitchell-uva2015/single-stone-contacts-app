package contacts.controllers;

import contacts.exceptions.ContactAlreadyExistsException;
import contacts.exceptions.ContactNotFoundException;
import contacts.exceptions.ErrorInfo;
import contacts.models.Contact;
import contacts.services.ContactService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@RestController
@RequestMapping("/contacts")
public class ContactController {

  private static final Logger logger = LogManager.getLogger(ContactController.class);

  private final ContactService contactService;

  @Autowired
  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  @GetMapping
  public HttpEntity<List<Contact>> getAllContacts() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);

    return ResponseEntity
      .ok()
      .headers(headers)
      .body(contactService.getAllContacts());
  }

  @PostMapping
  public HttpEntity<Contact> createContact(@RequestBody Contact contact) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);

    return ResponseEntity
      .ok()
      .headers(headers)
      .body(contactService.createContact(contact));
  }

  @PutMapping("/{id}")
  public HttpEntity<Contact> updateContact(@PathVariable(value = "id") long id, @RequestBody Contact contact) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);

    return ResponseEntity
      .ok()
      .headers(headers)
      .body(contactService.updateContact(id, contact));
  }

  @GetMapping("/{id}")
  public HttpEntity<Contact> getContact(@PathVariable(value = "id") long id) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON_UTF8);

    return ResponseEntity
      .ok()
      .headers(headers)
      .body(contactService.getContact(id));
  }

  @DeleteMapping("/{id}")
  public HttpEntity<?> deleteContact(@PathVariable(value = "id") long id) {
    contactService.deleteContact(id);

    return ResponseEntity
      .ok()
      .build();
  }

  @ExceptionHandler(ContactNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  ErrorInfo contactNotFoundHandler(HttpServletRequest request, Exception ex) {
    logger.error("Contact not found due to {}", ex.toString());
    return new ErrorInfo(HttpStatus.NOT_FOUND.value(), "Contact not found", request.getRequestURI(), request.getParameterMap());
  }

  @ExceptionHandler(ContactAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  ErrorInfo contactAlreadyExistsHandler(HttpServletRequest request, Exception ex) {
    logger.error("Cannot create contact due to {}", ex.toString());
    return new ErrorInfo(HttpStatus.BAD_REQUEST.value(), "Contact already exists", request.getRequestURI(), request.getParameterMap());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  ErrorInfo catchAllExceptionHandler(HttpServletRequest request, Exception ex) {
    logger.error("Encountered the following unhandled exception in ContactController: {}. Printing stack trace.", ex.toString());
    ex.printStackTrace();
    return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.toString(), request.getRequestURI(), request.getParameterMap());
  }

}
