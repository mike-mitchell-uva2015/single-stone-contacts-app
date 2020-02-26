package contacts.services;

import contacts.ContactFixture;
import contacts.TestProfile;
import contacts.exceptions.ContactNotFoundException;
import contacts.models.Contact;
import contacts.models.contactattributes.Name;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("functional")
@TestProfile
public class ContactServiceTest {

  private static List<Contact> expectedContacts;

  @Autowired
  private ContactService contactService;

  @BeforeAll
  private static void readTestJson() {
    expectedContacts = ContactFixture.getContactsListFixture();
  }

  @BeforeEach
  private void clearAndLoadTable() {
    contactService.deleteAll();
    ContactFixture.getContactsListFixture().forEach(curContact -> contactService.createContact(curContact));
  }

  @Test
  void testGetAllContacts() {
    List<Contact> foundContacts = contactService.getAllContacts();

    setContactIds(expectedContacts, foundContacts); //ids will likely have changed after clearing/re-loading table

    assertThat(foundContacts).isEqualTo(expectedContacts);
  }

  @Test
  void testCreateContact() {
    Contact newContact = ContactFixture.getContactFixture();
    newContact.setId(null);
    newContact.setName(new Name("Mike", "Michael", "Mitchell"));

    Contact returnedContact = contactService.createContact(newContact);

    assertThat(returnedContact).isEqualTo(newContact);
  }

  @Test
  void testUpdateContact() {
    Contact originalContact = contactService.getAllContacts().get(0);

    Contact updatedContact = new Contact();
    updatedContact.setId(originalContact.getId());
    updatedContact.setName(originalContact.getName());
    updatedContact.setAddress(originalContact.getAddress());
    updatedContact.setPhone(originalContact.getPhone());
    updatedContact.setEmail("awesomeNewEmail@email.gov");

    Contact result = contactService.updateContact(updatedContact.getId(), updatedContact);
    assertThat(result).isEqualTo(updatedContact);
  }

  @Test
  void testGetContact() {
    Contact contact = contactService.getAllContacts().get(0);

    Contact returnedContact = contactService.getContact(contact.getId());
    assertThat(returnedContact).isEqualTo(contact);
  }

  @Test
  void testDeleteContact() {
    Contact contact = contactService.getAllContacts().get(0);

    contactService.deleteContact(contact.getId());

    Assertions.assertThrows(ContactNotFoundException.class, () -> contactService.getContact(contact.getId()));
  }

  private void setContactIds(List<Contact> contactsWithoutIds, List<Contact> contactsWithIds) {
    for(int i=0; i < contactsWithIds.size(); i++) {
      contactsWithoutIds.get(i).setId(
        contactsWithIds.get(i).getId()
      );
    }
  }

}
