package contacts.models;

import contacts.models.contactattributes.Address;
import contacts.models.contactattributes.Name;
import contacts.models.contactattributes.Phone;
import contacts.models.converters.AddressConverter;
import contacts.models.converters.NameConverter;
import contacts.models.converters.PhoneArrayConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "contact")
public class Contact {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotNull
  @Column(name = "name")
  @Convert(converter = NameConverter.class)
  private Name name;

  @NotNull
  @Column(name = "address")
  @Convert(converter = AddressConverter.class)
  private Address address;

  @NotNull
  @Column(name = "phone")
  @Convert(converter = PhoneArrayConverter.class)
  private List<Phone> phone;

  @NotNull
  @Column(name = "email")
  private String email;

  @Override
  public String toString() {
    if(id == null) {
      return String.format(
        "Contact(name=%s, email=%s)", name.toString(), email
      );
    }

    return String.format(
      "Contact(id=%d, name=%s, email=%s)",
      id, name.toString(), email
    );
  }

}
