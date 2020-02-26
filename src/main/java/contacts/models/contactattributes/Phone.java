package contacts.models.contactattributes;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Phone {

  @NotNull
  private String number;
  private PhoneType type;

  enum PhoneType {
    home,
    work,
    mobile;
  }

}
