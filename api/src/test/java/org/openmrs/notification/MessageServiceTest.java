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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.notification.impl.MessageServiceImpl;
import org.openmrs.notification.mail.MailMessageSender;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the MessageService.
 */
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class MessageServiceTest extends BaseContextSensitiveTest {


	@Mock
	private MailMessageSender mailMessageSender;

	@InjectMocks
	private MessageServiceImpl messageService;

	@Mock
	private UserService userService;

	private List<User> mockedUserList;

	@Mock
	private MessagePreparator messagePreparator;

	@Mock
	private TemplateDAO mockTemplateDAO;

	/**
	 * Initializes mocks and test data before each test method is run.
	 * It sets up the message service with mock dependencies and configures the
	 * test user context and template DAO.
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		MockitoAnnotations.openMocks(this);
		messageService.setMessageSender(mailMessageSender);

		messageService = new MessageServiceImpl();
		messageService.setMessageSender(mailMessageSender);
		messageService.setMessagePreparator(messagePreparator);
		messageService.setTemplateDAO(mockTemplateDAO);


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
	public void createMessage_shouldCreateMessage() throws MessageException {
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

	@Test
	public void createMessage_shouldCreateMessageWithAllFields() throws MessageException {
		Message message = messageService.createMessage(
			"foo@bar.com",
			"me@domain.com",
			"Subject",
			"Content",
			"filedata",
			"text/plain",
			"a.txt"
		);

		assertNotNull(message);
		assertEquals("foo@bar.com", message.getRecipients());
		assertEquals("me@domain.com", message.getSender());
		assertEquals("Subject", message.getSubject());
		assertEquals("Content", message.getContent());
		assertEquals("filedata", message.getAttachment());
		assertEquals("text/plain", message.getAttachmentContentType());
		assertEquals("a.txt", message.getAttachmentFileName());
	}

	@Test
	public void createMessage_shouldCreateMessageWithRecipientsSenderSubjectAndContent() throws MessageException {
		Message msg = messageService.createMessage("foo@bar.com", "me@domain.com", "Subject", "Content");

		assertNotNull(msg);
		assertEquals("foo@bar.com", msg.getRecipients());
		assertEquals("me@domain.com", msg.getSender());
		assertEquals("Subject", msg.getSubject());
		assertEquals("Content", msg.getContent());

		// these should be null because this overload does not accept attachments
		assertNull(msg.getAttachment());
		assertNull(msg.getAttachmentContentType());
		assertNull(msg.getAttachmentFileName());
	}

	@Test
	public void createMessage_shouldCreateMessageWithSenderSubjectAndContent() throws MessageException {
		Message msg = messageService.createMessage("me@domain.com", "Subject", "Content");

		assertNotNull(msg);
		assertEquals("me@domain.com", msg.getSender());
		assertEquals("Subject", msg.getSubject());
		assertEquals("Content", msg.getContent());

		// defaults
		assertNull(msg.getRecipients());
		assertNull(msg.getAttachment());
	}

	@Test
	public void createMessage_shouldCreateMessageWithSubjectAndContent() throws MessageException {
		Message msg = messageService.createMessage("Subject", "Content");

		assertNotNull(msg);
		assertEquals("Subject", msg.getSubject());
		assertEquals("Content", msg.getContent());

		// defaults
		assertNull(msg.getSender());
		assertNull(msg.getRecipients());
		assertNull(msg.getAttachment());
	}

	/**
	 * @throws MessageException
	 * @see MessageService#sendMessage(Message)
	 */
	@Test
	public void sendMessage_shouldSendMessageSuccessfully() throws MessageException {
		// Arrange
		Message message = new Message();
		message.setRecipients("recipient@example.com");
		message.setSender("sender@example.com");
		message.setSubject("Test Subject");
		message.setContent("Test Content");

		// Act
		messageService.sendMessage(message);

		// Assert
		verify(mailMessageSender, times(1)).send(message);
	}

	/**
	 * Tests sending a message to a user and verifies that the mock sender is invoked.
	 *
	 * @throws Exception if sending fails
	 */
	@Test
	public void sendMessage_shouldSendMessageToRecipientId() throws MessageException {
		// Arrange
		Message message = new Message();
		message.setSender("sender@example.com");
		message.setSubject("Test Subject");
		message.setContent("Test Content");

		Integer recipientId = 5;
		User mockUser = new User();
		mockUser.setUserId(recipientId);

		when(userService.getUser(recipientId)).thenReturn(mockUser);

		// Act
		messageService.sendMessage(message, recipientId);

		// Assert
		verify(mailMessageSender, times(1)).send(message);
		verify(userService).getUser(recipientId);
	}

	@Test
	public void sendMessage_shouldSendMessageToUserObject() throws MessageException {
		// Arrange
		User mockUser = new User();
		mockUser.setUserId(10);

		Message message = new Message();
		message.setSender("sender@example.com");
		message.setSubject("Hello User");
		message.setContent("Test message to user");

		// Act
		messageService.sendMessage(message, mockUser);

		// Assert
		verify(mailMessageSender, times(1)).send(message);
	}

	@Test
	public void sendMessage_shouldSendMessageToMultipleUsers() throws MessageException {
		// Arrange
		User user1 = new User();
		user1.setUserId(1);
		User user2 = new User();
		user2.setUserId(2);

		Collection<User> users = Arrays.asList(user1, user2);

		Message message = new Message();
		message.setSender("sender@example.com");
		message.setSubject("Group Message");
		message.setContent("Test multicast");

		// Act
		messageService.sendMessage(message, users);

		// Assert
		verify(mailMessageSender, times(1)).send(message);
	}

	@Test
	public void sendMessage_shouldSendToRoleName() throws MessageException {
		// Arrange
		String roleName = "Provider";

		Role mockRole = new Role(roleName);

		Message message = new Message();
		message.setSender("sender@example.com");
		message.setSubject("Role Message");
		message.setContent("Message to role users");

		when(userService.getRole(roleName)).thenReturn(mockRole);

		// Act
		messageService.sendMessage(message, roleName);

		// Assert
		verify(mailMessageSender, times(1)).send(message);
		verify(userService).getRole(roleName);
	}

	@Test
	public void sendMessage_shouldSendToRoleObject() throws MessageException {
		// Arrange
		Role mockRole = new Role("Clinician");

		Message message = new Message();
		message.setSender("sender@example.com");
		message.setSubject("Role Object Message");
		message.setContent("Test for role object");

		// Act
		messageService.sendMessage(message, mockRole);

		// Assert
		verify(mailMessageSender, times(1)).send(message);
	}


	/**
	 * Ensures that the message service can prepare a message from a given template.
	 *
	 * @throws Exception if preparation fails
	 */
	@Test
	public void prepareMessage_shouldDelegateToMessagePreparator() throws MessageException {
		// Arrange
		Template template = new Template();
		template.setName("Test Template");

		Message expectedMessage = new Message();
		expectedMessage.setRecipients("user@example.com");

		when(messagePreparator.prepare(template)).thenReturn(expectedMessage);

		// Act
		Message actualMessage = messageService.prepareMessage(template);

		// Assert
		assertNotNull(actualMessage);
		assertEquals("user@example.com", actualMessage.getRecipients());
		verify(messagePreparator, times(1)).prepare(template);
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
