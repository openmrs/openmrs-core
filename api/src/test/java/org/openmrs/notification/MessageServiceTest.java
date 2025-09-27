/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.api.UserService;
import org.openmrs.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.lang.reflect.Field;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.openmrs.notification.impl.MessageServiceImpl;
import org.openmrs.notification.mail.MailMessageSender;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class MessageServiceTest extends BaseContextSensitiveTest {

	@Mock
	private MessageSender mockSender;

	@InjectMocks
    private MessageServiceImpl messageService;

	private UserService userService;
    private List<User> mockedUserList;

	private UserService originalUserService;
	private MessagePreparator messagePreparator;

	private TemplateDAO mockTemplateDAO;

	private MessagePreparator mockMessagePreparator;

	private Object mockMailSender;

    /**
     * Initializes mocks and test data before each test method is run.
     * It sets up the message service with mock dependencies and configures the
     * test user context and template DAO.
     */
@BeforeEach
public void runBeforeEachTest() {
    MockitoAnnotations.openMocks(this);
    mockSender = mock(MessageSender.class);
	messagePreparator = mock(MessagePreparator.class);
    mockTemplateDAO = mock(TemplateDAO.class);

    messageService = new MessageServiceImpl();
    messageService.setMessageSender(mockSender);
	messageService.setMessagePreparator(messagePreparator);
	((MessageServiceImpl) messageService).setTemplateDAO(mockTemplateDAO);

// ✅ Set 'self' via reflection without modifying production code
try {
	Field selfField = MessageServiceImpl.class.getDeclaredField("self");
    selfField.setAccessible(true);
    selfField.set(messageService, messageService);
 } catch (NoSuchFieldException | IllegalAccessException e) {
     throw new RuntimeException("Failed to set 'self' field via reflection", e);
 }
    // ✅ Store the original service (defensive)
    originalUserService = Context.getService(UserService.class);

    // ✅ Mock only for this test
    userService = mock(UserService.class);
   ServiceContext mockServiceContext = mock(ServiceContext.class);
   Context.setServiceContext(mockServiceContext); // ✅ use this built-in method


    mockedUserList = new ArrayList<>();
    User user1 = new User();
    user1.setEmail("user@example.com");
    mockedUserList.add(user1);

      lenient().when(userService.getUsers(any(), anyList(), anyBoolean()))
    .thenReturn(mockedUserList);


    executeDataSet("org/openmrs/notification/include/MessageServiceTest-initial.xml");
}

    /**
     * Verifies that a message is correctly created with all its fields populated.
     *
     * @throws MessageException if message creation fails
     */
	@Test
	public void createMessage_shouldCreateMessageWithAllFields() throws MessageException {
		// Arrange
		String recipients = "foo@bar.com, marco@polo.com";
		String sender = "me@mydomain.com";
		String subject = "Test Subject";
		String content = "This is a test message.";
		String attachment = "testfile";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "testfile.txt";

		// Act
		Message message = messageService.createMessage(recipients, sender, subject, content, attachment,
			attachmentContentType, attachmentFileName);

		// Assert
		assertNotNull(message);
		assertEquals(recipients, message.getRecipients());
		assertEquals(sender, message.getSender());
		assertEquals(subject, message.getSubject());
		assertEquals(content, message.getContent());
		assertEquals(attachment, message.getAttachment());
		assertEquals(attachmentContentType, message.getAttachmentContentType());
		assertEquals(attachmentFileName, message.getAttachmentFileName());
	}

    /**
     * Tests that the message sender successfully sends a basic message.
     *
     * @throws Exception if sending fails
     */
    @Test
    public void sendMessage_shouldSendMessageSuccessfully() throws Exception {
        // Arrange
        Message message = new Message();
        message.setRecipients("recipient@example.com");
        message.setSender("sender@example.com");
        message.setSubject("Test Subject");
        message.setContent("Test Content");

        // Act
        messageService.sendMessage(message);

        // Assert
        verify(mockSender, times(1)).send(message);
    }

    /**
     * Tests sending a message to a user and verifies that the mock sender is invoked.
     *
     * @throws Exception if sending fails
     */
	@Test
    public void sendMessage_shouldSendMessageToUser() throws Exception {
       // Arrange
      Message message = new Message();
      message.setRecipients("recipient@example.com");
      message.setSender("sender@example.com");
      message.setSubject("Hello");
      message.setContent("Test message");

    // Act
    messageService.sendMessage(message);

    // Assert: verify that the mock sender was used
    verify(mockSender, times(1)).send(message);
}

 /**
     * Ensures that the message service can prepare a message from a given template.
     *
     * @throws Exception if preparation fails
     */
@Test
public void prepareMessage_shouldPrepareMessageFromTemplate() throws Exception {
    Template template = new Template();

    when(messagePreparator.prepare(any(Template.class)))
         .thenAnswer(invocation -> new Message());


    template.setName("Test Template");

    Message expectedMessage = new Message();
    expectedMessage.setRecipients("user@example.com");

    // ✅ Tell the mock what to return
    when(messagePreparator.prepare(template)).thenReturn(expectedMessage);

    Message message = messageService.prepareMessage(template);
    assertNotNull(message);
    assertEquals("user@example.com", message.getRecipients());
}

   /**
     * Verifies that template variables are substituted correctly when preparing a message
     * with dynamic data.
     *
     * @throws MessageException if message preparation fails
     */
@Test
public void prepareMessageWithData_shouldPrepareMessageWithVariableSubstitution() throws MessageException {
    // Arrange
    Template template = new Template();
    template.setName("Test Template");
    template.setContent("Hello, $user");



    when(mockTemplateDAO.getTemplatesByName("Test Template"))
        .thenReturn(Collections.singletonList(template));

    when(messagePreparator.prepare(any(Template.class)))

        .thenAnswer(invocation -> {
            Template t = invocation.getArgument(0);
            String content = t.getContent().replace("$user", (String) t.getData().get("user"));
            Message m = new Message();
            m.setContent(content);
            return m;
        });

    Map<String, String> data = new HashMap<>();
    data.put("user", "John");
	template.setData(data);

    // Act
    Message message = messageService.prepareMessage("Test Template", data);

    // Assert
    assertNotNull(message);
    assertEquals("Hello, John", message.getContent());
}

     /**
     * Tests that the message service correctly retrieves a template by its ID.
     *
     * @throws MessageException if retrieval fails
     */
	 @Test
public void getTemplate_shouldReturnTemplateById() throws MessageException {
    // Arrange
    Integer templateId = 1;
    Template expectedTemplate = new Template();
    expectedTemplate.setId(templateId);
    expectedTemplate.setName("Test Template");

    when(mockTemplateDAO.getTemplate(templateId)).thenReturn(expectedTemplate);

    // Act
    Template template = messageService.getTemplate(templateId);

    // Assert
    assertNotNull(template);
    assertEquals(templateId, template.getId());
}

     /**
     * Ensures that templates can be retrieved by name using the message service.
     *
     * @throws MessageException if retrieval fails
     */
	@Test
public void getTemplatesByName_shouldReturnTemplatesByName() throws MessageException {
    // Arrange
    Template template = new Template();
    template.setName("Welcome Template");

    when(mockTemplateDAO.getTemplatesByName("Welcome Template"))
        .thenReturn(Collections.singletonList(template));

    // Act
    List<Template> templates = messageService.getTemplatesByName("Welcome Template");

    // Assert
    assertEquals(1, templates.size());
    assertEquals("Welcome Template", templates.get(0).getName());
  }
}
