package contacts.models.converters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import contacts.exceptions.ContactsRuntimeException;
import contacts.models.contactattributes.Phone;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class PhoneArrayConverter implements AttributeConverter<List<Phone>, String> {

  @Override
  public List<Phone> convertToEntityAttribute(String dbData) {
    return toJsonArray(dbData);
  }

  @Override
  public String convertToDatabaseColumn(List<Phone> phones) {
    return toDatabaseColumn(phones);
  }

  public static List<Phone> toJsonArray(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return null;
    }

    try {
      return new Gson().fromJson(dbData, new TypeToken<List<Phone>>(){}.getType());
    } catch (JsonSyntaxException e) {
      throw new ContactsRuntimeException(String.format("Failed to write database column '%s' to JsonArray due to %s", dbData, e.getMessage()));
    }
  }

  public static String toDatabaseColumn(List<Phone> phones) {
    if (phones == null) {
      return null;
    }
    return new Gson().toJson(phones);

  }
}
