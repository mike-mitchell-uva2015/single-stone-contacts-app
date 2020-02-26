package contacts.services;

import contacts.exceptions.ContactAlreadyExistsException;
import contacts.exceptions.ContactNotFoundException;
import contacts.models.Contact;
import contacts.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

  private final ContactRepository contactRepo;

  @Autowired
  public ContactService(ContactRepository contactRepo) {
    this.contactRepo = contactRepo;
  }

  public List<Contact> getAllContacts() {
    return contactRepo.findAll();
  }

  public Contact createContact(Contact newContact) {
    if(newContact.getId() != null && contactRepo.findById(newContact.getId()).isPresent()) {
      throw new ContactAlreadyExistsException();
    }

    return contactRepo.save(newContact);
  }

  public Contact updateContact(long id, Contact newContact) {
    if(!contactRepo.findById(id).isPresent()) {
      throw new ContactNotFoundException();
    }

    return contactRepo.save(newContact);
  }

  public Contact getContact(long id) {
    return contactRepo.findById(id).orElseThrow(ContactNotFoundException::new);
  }

  public void deleteContact(long id) {
    if(!contactRepo.findById(id).isPresent()) {
      throw new ContactNotFoundException();
    }

    contactRepo.deleteById(id);
  }

  public void deleteAll() {
    contactRepo.deleteAll();
  }

}
