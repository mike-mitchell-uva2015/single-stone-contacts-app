package contacts.models.contactattributes;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Address {
  private String street;
  private String city;

  @NotNull
  private String state;

  @NotNull
  private String zip;
}
