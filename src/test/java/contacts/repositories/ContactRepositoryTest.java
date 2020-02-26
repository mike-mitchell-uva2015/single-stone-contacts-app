package contacts.repositories;

import contacts.ContactFixture;
import contacts.TestProfile;
import contacts.models.Contact;
import contacts.models.contactattributes.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class ContactRepositoryTest {

  @Autowired
  private ContactRepository contactRepo;

  @BeforeEach
  private void clearAndLoadTable() {
    contactRepo.deleteAll();
    contactRepo.saveAll(ContactFixture.getContactsListFixture());
  }

  @Test
  void testSave() {
    Contact newContact = ContactFixture.getContactFixture();
    newContact.setId(null);
    newContact.setName(new Name("Mike", "Michael", "Mitchell"));

    Contact returnedContact = contactRepo.save(newContact);

    newContact.setId(returnedContact.getId());
    assertThat(returnedContact).isEqualTo(newContact);
  }

  @Test
  void testFindById() {
    Contact contact = contactRepo.findAll().get(0);

    Contact returnedContact = contactRepo.findById(contact.getId()).get();
    assertThat(returnedContact).isEqualTo(contact);
  }

  @Test
  void testFindAll() {
    List<Contact> returnedContacts = contactRepo.findAll();
    assertThat(returnedContacts.size()).isEqualTo(3);
  }

  @Test
  void testDelete() {
    Contact contact = contactRepo.findAll().get(0);

    contactRepo.delete(contact);

    assertThat(contactRepo.findById(contact.getId()).isPresent()).isFalse();
  }

}
