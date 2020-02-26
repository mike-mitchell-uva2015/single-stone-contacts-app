package contacts.models.converters;

import com.google.gson.Gson;
import contacts.models.contactattributes.Address;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AddressConverter implements AttributeConverter<Address, String> {

  @Override
  public String convertToDatabaseColumn(Address address) {
    return toDatabaseColumn(address);
  }

  @Override
  public Address convertToEntityAttribute(String dbData) {
    return fromDatabaseColumn(dbData);
  }

  public static String toDatabaseColumn(Address address) {
    if(address == null) {
      return null;
    }

    return new Gson().toJson(address);
  }

  public static Address fromDatabaseColumn(String dbData) {
    if(dbData == null || dbData.isEmpty()) {
      return null;
    }

    return new Gson().fromJson(dbData, Address.class);
  }

}
