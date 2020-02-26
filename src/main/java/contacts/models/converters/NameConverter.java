package contacts.models.converters;

import com.google.gson.Gson;
import contacts.models.contactattributes.Name;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class NameConverter implements AttributeConverter<Name, String> {

  @Override
  public String convertToDatabaseColumn(Name name) {
    return toDatabaseColumn(name);
  }

  @Override
  public Name convertToEntityAttribute(String dbData) {
    return fromDatabaseColumn(dbData);
  }

  public static String toDatabaseColumn(Name name) {
    if(name == null) {
      return null;
    }

    return new Gson().toJson(name);
  }

  public static Name fromDatabaseColumn(String dbData) {
    if(dbData == null || dbData.isEmpty()) {
      return null;
    }

    return new Gson().fromJson(dbData, Name.class);
  }

}
