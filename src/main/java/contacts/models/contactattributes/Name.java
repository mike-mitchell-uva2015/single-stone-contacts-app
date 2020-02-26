package contacts.models.contactattributes;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Name {

  @NotNull
  private String first;
  private String middle;

  @NotNull
  private String last;

  @Override
  public String toString() {
    return String.format("%s %s", first, last);
  }

}
