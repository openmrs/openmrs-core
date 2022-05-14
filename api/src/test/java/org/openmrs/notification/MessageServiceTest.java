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

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.notification.impl.MessageServiceImpl;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Unit tests for the MessageService.
 */
public class MessageServiceTest extends BaseContextSensitiveTest {
	
	// Most messages are sent from this email address
	private static final String MAIL_SENDER = "sender@example.com";
	
	// Most messages, to random mailboxes, are sent to this domain
	private static final String RECIPIENT_DOMAIN = "@test.net";

	/**
	 * MessageService used during these tests. 
	 * Will replace the original MessageService - which is not suitable - defined in applicationContext-service.xml
 	 */
	private MessageService testMessageService;

	/**
	 * We keep the original MessageService and restore it after each test.
	 */
	private MessageService originalMessageService;
	
	/**
	* One GreenMail server is shared for all test methods of this test class.
	* To ensure tests are independent and don't fail if a previous test sends an email to the same inbox, 
	* use random email addresses.
	 */
	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
		.withPerMethodLifecycle(true);

	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() throws MessageException {
		executeDataSet("org/openmrs/notification/include/MessageServiceTest-initial.xml");
		
		setupMessageService();
	}

	@AfterEach
	public void cleanup() {
		restoreOriginalMessageService();
	}

	/**
	 * @throws MessageException
	 * @see MessageService#createMessage(String,String,String,String)
	 */
	@Test
	public void createMessage_shouldCreateMessage() throws MessageException {
		String recipients = "foo@bar.com,marco@polo.com";
		String sender = "me@mydomain.com";
		String subject = "foo";
		String message = "content";
		String attachment = "inga";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "inga.txt";
		
		Message msg1 = testMessageService.createMessage(recipients, sender, subject, message);
		Message msg2 = testMessageService.createMessage(subject, message);
		Message msg3 = testMessageService.createMessage(sender, subject, message);
		Message msg4 = testMessageService.createMessage(recipients, sender, subject, message, attachment, attachmentContentType,
		    attachmentFileName);
		
		assertEquals(recipients, msg1.getRecipients());
		assertEquals(recipients, msg4.getRecipients());
		
		assertEquals(sender, msg1.getSender());
		assertEquals(sender, msg3.getSender());
		assertEquals(sender, msg4.getSender());
		
		assertEquals(subject, msg1.getSubject());
		assertEquals(subject, msg2.getSubject());
		assertEquals(subject, msg3.getSubject());
		assertEquals(subject, msg4.getSubject());
		
		assertEquals(message, msg1.getContent());
		assertEquals(message, msg2.getContent());
		assertEquals(message, msg3.getContent());
		assertEquals(message, msg4.getContent());
		
		assertEquals(attachment, msg4.getAttachment());
		assertEquals(attachmentContentType, msg4.getAttachmentContentType());
		assertEquals(attachmentFileName, msg4.getAttachmentFileName());
	}
	
	/**
	 * @throws MessageException
	 * @see MessageService#sendMessage(Message)
	 */
	@Test
	public void sendMessage_shouldSendMessage() throws MessageException, MessagingException {
		String recipientOfFirstMessage = "first@example.com";
		String contentOfFirstMessage = GreenMailUtil.random();
		String subjectOfFirstMessage = GreenMailUtil.random();
		Message firstMessageToSend = testMessageService.createMessage(recipientOfFirstMessage, MAIL_SENDER, subjectOfFirstMessage, contentOfFirstMessage);
		
		testMessageService.sendMessage(firstMessageToSend);
		
		String recipientsOfSecondMessage = "second@example.com,copy@example.com";
		String contentOfSecondMessage = GreenMailUtil.random();
		String subjectOfSecondMessage = GreenMailUtil.random();		
		String attachment = "test";
		String attachmentContentType = "text/plain";
		String attachmentFileName = "filename.txt";
		Message secondMessageToSend = testMessageService.createMessage(recipientsOfSecondMessage, MAIL_SENDER,
				subjectOfSecondMessage, contentOfSecondMessage, attachment, attachmentContentType, attachmentFileName);
		
		testMessageService.sendMessage(secondMessageToSend);
		
		MimeMessage firstMessageReceived = greenMail.getReceivedMessages()[0];
		assertEquals(contentOfFirstMessage, GreenMailUtil.getBody(firstMessageReceived));
		assertEquals(1, firstMessageReceived.getAllRecipients().length);
		assertEquals(recipientOfFirstMessage, firstMessageReceived.getAllRecipients()[0].toString());
		
		MimeMessage secondMessageReceived = greenMail.getReceivedMessages()[1];
		Address[] actualRecipients = secondMessageReceived.getAllRecipients();
		assertEquals(2, actualRecipients.length);
		assertEquals(recipientsOfSecondMessage, formatRecipients(actualRecipients));
	}
	@Test
	public void sendMessage_withGivenParameters_shouldSendMessage() throws MessageException, MessagingException {
		String recipients = "foo@bar.com,marco@polo.com";
		String subject = "foo";
		String content = "content";
		
		testMessageService.sendMessage(recipients, MAIL_SENDER, subject, content);

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		Address[] allMessageRecipients = receivedMessage.getAllRecipients();
		
		assertEquals(2, allMessageRecipients.length);
		assertEquals(recipients, formatRecipients(allMessageRecipients));
	}

	@Test
	public void sendMessage_withRecipientId_shouldSendMessage() throws MessageException, MessagingException {
		List<User> users = getSomeUsers(5);

		List<String> subjects = IntStream.range(0, users.size()).mapToObj(i -> GreenMailUtil.random())
			.collect(toList());
		
		List<String> contents = IntStream.range(0, users.size()).mapToObj(i -> GreenMailUtil.random())
			.collect(toList());

		List<Message> messages = new ArrayList<>();
		for(User user : users) {
			int i = users.indexOf(user);
			messages.add(testMessageService.createMessage(subjects.get(i), contents.get(i)));
			testMessageService.sendMessage(messages.get(i), user.getId());
		}

		assertEquals(users.size(), greenMail.getReceivedMessages().length);

		contents.forEach(content -> {
			int i = contents.indexOf(content);
			MimeMessage mimeMessage = greenMail.getReceivedMessages()[i];
			assertEquals(content, GreenMailUtil.getBody(mimeMessage));
		});
		
		for(User user : users) {
			int i = users.indexOf(user);
			MimeMessage mimeMessage = greenMail.getReceivedMessages()[i];
			assertEquals(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS), 
				mimeMessage.getAllRecipients()[0].toString());
		}
	}

	@Test
	void sendMessage_withUser_shouldSendMessage() throws MessageException, MessagingException {
		User user = getOneUser();
		Message message = testMessageService.createMessage(GreenMailUtil.random(), GreenMailUtil.random());
		
		testMessageService.sendMessage(message, user);

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		String recipient = receivedMessage.getAllRecipients()[0].toString();
		assertEquals(getNotificationAddress(user), recipient);
	}

	@Test
	void sendMessage_withCollectionOfUsers_shouldSendMessage() throws MessageException, MessagingException {
		List<User> users = getSomeUsers(5);
		Message message = testMessageService.createMessage(GreenMailUtil.random(), GreenMailUtil.random());
		
		testMessageService.sendMessage(message, users);

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		Address[] recipients = receivedMessage.getAllRecipients();
		String emailAddresses = String.join(",",
			users.stream().map(this::getNotificationAddress).collect(toList()));
		
		assertEquals(emailAddresses, formatRecipients(recipients));
	}

	@Test
	void sendMessage_withRoleName_shouldSendMessage() throws MessageException, MessagingException {
		User user = getOneUser();
		String roleName = new ArrayList<>(user.getAllRoles()).get(0).getName();
		Message message = testMessageService.createMessage(GreenMailUtil.random(), GreenMailUtil.random());

		testMessageService.sendMessage(message, roleName);

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		String recipient = receivedMessage.getAllRecipients()[0].toString();
		assertEquals(getNotificationAddress(user), recipient);
	}

	@Test
	void sendMessage_withRole_shouldSendMessage() throws MessageException, MessagingException {
		User user = getOneUser();
		Role role = new ArrayList<>(user.getAllRoles()).get(0);
		Message message = testMessageService.createMessage(GreenMailUtil.random(), GreenMailUtil.random());

		testMessageService.sendMessage(message, role);

		MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
		String recipient = receivedMessage.getAllRecipients()[0].toString();
		assertEquals(getNotificationAddress(user), recipient);
	}

	/**
	 * Private utility method
	 *
	 * @param user the user whom notification email address we want
	 * @return String representation of an email address
	 */
	private String getNotificationAddress(User user) {
		return user.getUserProperties().get(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS);
	}

	/**
	 * Private utility method
	 *
	 * @param maxNumber the maximum number of users to return
	 * @return List of users with random notification email address
	 */
	private List<User> getSomeUsers(int maxNumber) {
		UserService userService = Context.getUserService();
		List<User> allUsers = userService.getAllUsers();
		int maxUsers = Math.min(allUsers.size(), maxNumber);
		List<User> users = allUsers.subList(0, maxUsers);

		users.forEach(user -> {
			user.getUserProperties()
				.put(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS,
					GreenMailUtil.random() + RECIPIENT_DOMAIN);
		});

		return users;
	}

	/**
	 * Private utility method
	 *
	 * @return User with a random notification email address
	 */
	private User getOneUser() {
		User user = Context.getUserService().getAllUsers().get(0);
		user.getUserProperties().put(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS, 
			GreenMailUtil.random() + RECIPIENT_DOMAIN);
		return user;
	}

	/**
	 * Private utility method
	 *
	 * @param allRecipients an array of Address
	 * @return String which concatenates all addresses
	 */
	private String formatRecipients(Address[] allRecipients) {
		return Arrays.stream(allRecipients).map(Address::toString).collect(joining(","));
	}

	/**
	 * Private utility method
	 *
	 * A mail <code>Session</code> is defined in <code>MessageServiceTest-context.xml</code>
	 * That mail Session is used by <code>messageSender</code> in <code>testMessageService</code>.
	 * We put <code>testMessageService</code> in the Openmrs context; because, strangely, <code>MessageServiceImpl</code> 
	 * does not always use its field <code>messageSender</code> to send messages. <code>MessageServiceImpl</code> often 
	 * retrieves a <code>MessageService</code> from the Openmrs context and calls <code>sendMessage</code> on it.
	 * Short of using a  <code>@TestExecutionListeners</code> as it's done in <code>BaseContextSensitiveTest</code>, 
	 * we needed a way to replace beans.
	 */
	private void setupMessageService() {
		originalMessageService = ServiceContext.getInstance().getMessageService();

		String[] xmlFiles = new String[]{"/org/openmrs/notification/MessageServiceTest-context.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(xmlFiles, applicationContext);
		testMessageService = ctx.getBean(MessageServiceImpl.class);
		ServiceContext.getInstance().setMessageService(testMessageService);
	}

	/**
	 * Private utility method
	 *
	 * Restores the original <code>MessageService</code> to the Openmrs context
	 */
	private void restoreOriginalMessageService() {
		if (nonNull(originalMessageService)) {
			ServiceContext.getInstance().setMessageService(originalMessageService);
		}
	}
}
