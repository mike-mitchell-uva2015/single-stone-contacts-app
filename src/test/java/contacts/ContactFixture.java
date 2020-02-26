package contacts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import contacts.models.Contact;

import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Only used for testing purposes
 */
public class ContactFixture {

  private static List<Contact> loadTestContacts() throws IOException {

    return new Gson().fromJson(
      new FileReader("src/test/resources/testcontacts.json"),
      new TypeToken<List<Contact>>(){}.getType()
    );
  }

  public static Contact getContactFixture() {
    Comparator<Contact> contactComparator = Comparator.comparing(Contact::getId);
    return getContactsListFixture().stream()
      .min(contactComparator)
      .orElseThrow(RuntimeException::new);

  }

  public static List<Contact> getContactsListFixture() {
    List<Contact> contacts = null;
    try {
      contacts = loadTestContacts();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return contacts;
  }

}
