package contacts.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import contacts.ContactFixture;
import contacts.TestProfile;
import contacts.exceptions.ContactAlreadyExistsException;
import contacts.exceptions.ContactNotFoundException;
import contacts.models.Contact;
import contacts.services.ContactService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ContactController.class)
@TestProfile
@Tag("unit")
public class ContactControllerTest {

  private static Contact expectedContact;
  private static List<Contact> expectedContacts;

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ContactService contactService;

  @BeforeAll
  private static void readTestJson() {
    expectedContacts = ContactFixture.getContactsListFixture();
    expectedContact = expectedContacts.get(0);
  }

  @BeforeEach
  private void clearAndLoadTable() {
    contactService.deleteAll();
    ContactFixture.getContactsListFixture().forEach(curContact -> contactService.createContact(curContact));
  }

  @Test
  void testGetAllContacts() throws Exception {
    // given
    given(contactService.getAllContacts()).willReturn(expectedContacts);

    // when
    MvcResult result = mvc
      .perform(get("/contacts")
        .accept(APPLICATION_JSON)
        .header("Authorization", "abc123")).andReturn();

    // then
    then(contactService).should().getAllContacts();

    List<Contact> returnedContacts = new Gson().fromJson(
      result.getResponse().getContentAsString(),
      new TypeToken<List<Contact>>(){}.getType()
    );

    assertThat(returnedContacts).isEqualTo(expectedContacts);
  }

  @Test
  void testCreateContact() throws Exception {
    expectedContact.setId(999L); //make the id something new
    
    // given
    given(contactService.createContact(any(Contact.class))).willReturn(expectedContact);

    //when
    RequestBuilder requestBuilder =
      post("/contacts")
        .header("Authorization", "abc123")
        .accept(APPLICATION_JSON_UTF8)
        .contentType(APPLICATION_JSON_UTF8)
        .content(new Gson().toJson(expectedContact));

    MvcResult result = mvc.perform(requestBuilder).andReturn();

    Contact returnedContact = new Gson().fromJson(
      result.getResponse().getContentAsString(), Contact.class
    );

    //then
    then(contactService).should().createContact(expectedContact);
    
    assertThat(returnedContact).isEqualTo(expectedContact);
  }

  @Test
  void testUpdateContact() throws Exception {
    expectedContact.setEmail("newEmail@email.gov");
    
    // given
    given(contactService.updateContact(anyLong(), any(Contact.class))).willReturn(expectedContact);

    //when
    RequestBuilder requestBuilder =
      put(String.format("/contacts/%d", expectedContact.getId()))
        .header("Authorization", "abc123")
        .accept(APPLICATION_JSON_UTF8)
        .contentType(APPLICATION_JSON_UTF8)
        .content(new Gson().toJson(expectedContact));

    MvcResult result = mvc.perform(requestBuilder).andReturn();

    Contact returnedContact = new Gson().fromJson(
      result.getResponse().getContentAsString(), Contact.class
    );

    //then
    then(contactService).should().updateContact(expectedContact.getId(), expectedContact);
    
    assertThat(returnedContact).isEqualTo(expectedContact);
  }

  @Test
  void testGetContact() throws Exception {
    // given
    given(contactService.getContact(anyLong())).willReturn(expectedContact);

    // when
    MvcResult result = mvc
      .perform(get(String.format("/contacts/%d", expectedContact.getId()))
        .accept(APPLICATION_JSON)
        .header("Authorization", "abc123")).andReturn();

    // then
    then(contactService).should().getContact(expectedContact.getId());

    Contact returnedContact = new Gson().fromJson(
      result.getResponse().getContentAsString(), Contact.class
    );

    assertThat(returnedContact).isEqualTo(expectedContact);
  }

  @Test
  void testDeleteContact() throws Exception {
    doNothing().when(contactService).deleteContact(anyLong());

    MvcResult result = mvc
      .perform(delete(String.format("/contacts/%d", expectedContact.getId()))
        .accept(APPLICATION_JSON)
        .header("Authorization", "abc123")).andReturn();

    then(contactService).should().deleteContact(expectedContact.getId());

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCreateContactThrowsContactAlreadyExistsException() throws Exception {
    // given
    given(contactService.createContact(any(Contact.class))).willThrow(ContactAlreadyExistsException.class);

    // when
    ResultActions resultActions = mvc.perform(
      post("/contacts")
        .accept(APPLICATION_JSON_UTF8)
        .contentType(APPLICATION_JSON_UTF8)
        .header("Authorization", "abc123")
        .content(new Gson().toJson(expectedContact))
    );

    resultActions.andExpect(status().isBadRequest());
    resultActions.andExpect(content().string("{\"status\":400,\"message\":\"Contact already exists\",\"url\":\"/contacts\",\"params\":{}}"));
  }

  @Test
  void testUpdateContactThrowsContactNotFoundException() throws Exception {
    // given
    given(contactService.updateContact(anyLong(), any(Contact.class))).willThrow(ContactNotFoundException.class);

    // when
    ResultActions resultActions = mvc.perform(
      put(String.format("/contacts/%d", expectedContact.getId()))
        .accept(APPLICATION_JSON_UTF8)
        .contentType(APPLICATION_JSON_UTF8)
        .header("Authorization", "abc123")
        .content(new Gson().toJson(expectedContact))
    );

    resultActions.andExpect(status().isNotFound());
    resultActions.andExpect(content().string("{\"status\":404,\"message\":\"Contact not found\",\"url\":\"/contacts/1\",\"params\":{}}"));
  }

  @Test
  void testDeleteContactThrowsContactNotFoundException() throws Exception {
    // given
    doThrow(ContactNotFoundException.class).when(contactService).deleteContact(anyLong());

    // when
    ResultActions resultActions = mvc.perform(
      delete(String.format("/contacts/%d", expectedContact.getId()))
        .accept(APPLICATION_JSON_UTF8)
        .contentType(APPLICATION_JSON_UTF8)
        .header("Authorization", "abc123")
        .content(new Gson().toJson(expectedContact))
    );

    resultActions.andExpect(status().isNotFound());
    resultActions.andExpect(content().string("{\"status\":404,\"message\":\"Contact not found\",\"url\":\"/contacts/1\",\"params\":{}}"));
  }

}
