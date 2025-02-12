package org.openmrs.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.notification.MessageServiceImpl;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.Message;
import org.openmrs.notification.Template;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;

public class MessageServiceTest extends BaseContextSensitiveTest {
	
	private MessageServiceImpl messageService;
	
	@BeforeEach
	public void runBeforeEachTest() {
		executeDataSet("org/openmrs/notification/include/MessageServiceTest-initial.xml");
		messageService = new MessageServiceImpl();
	}

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

	@Test
	public void sendMessage_shouldSendMessageSuccessfully() throws MessageException {
		// Arrange
		Message message = messageService.createMessage("recipient@example.com", "sender@example.com", "Subject", "Content");
		
		// Act
		try {
			messageService.sendMessage(message);
		} catch (MessageException e) {
			// Assert
			if (!e.getMessage().contains("Could not connect to SMTP host:")) {
				e.printStackTrace();
				fail("Message sending failed: " + e.getMessage());
			}
		}
	}

	@Test
	public void sendMessage_shouldSendMessageToUser() throws MessageException {
		// Arrange
		Message message = messageService.createMessage("recipient@example.com", "sender@example.com", "Subject", "Content");
		Integer recipientId = 1;  // Assuming user with ID 1 exists in the context.
		
		// Act
		try {
			messageService.sendMessage(message, recipientId);
		} catch (MessageException e) {
			// Assert
			if (!e.getMessage().contains("Could not connect to SMTP host:")) {
				e.printStackTrace();
				fail("Message sending failed: " + e.getMessage());
			}
		}
	}

	@Test
	public void sendMessage_shouldSendMessageToRole() throws MessageException {
		// Arrange
		Message message = messageService.createMessage("recipient@example.com", "sender@example.com", "Subject", "Content");
		String roleName = "Admin";
		
		// Act
		try {
			messageService.sendMessage(message, roleName);
		} catch (MessageException e) {
			// Assert
			if (!e.getMessage().contains("Could not connect to SMTP host:")) {
				e.printStackTrace();
				fail("Message sending failed: " + e.getMessage());
			}
		}
	}

	@Test
	public void prepareMessage_shouldPrepareMessageFromTemplate() throws MessageException {
		// Arrange
		Template template = new Template();  // Assume template is created and populated
		template.setName("Test Template");
		template.setContent("Hello, ${user}");
		
		// Act
		Message message = messageService.prepareMessage(template);
		
		// Assert
		assertNotNull(message);
		assertEquals("Hello, ${user}", message.getContent());  // Assuming content processing is done later
	}

	@Test
	public void prepareMessageWithData_shouldPrepareMessageWithVariableSubstitution() throws MessageException {
		// Arrange
		String templateName = "Test Template";
		Map<String, String> data = Map.of("user", "John");
		
		// Act
		Message message = messageService.prepareMessage(templateName, data);
		
		// Assert
		assertNotNull(message);
		assertEquals("Hello, John", message.getContent());  // Assuming the template content is processed
	}
	
	@Test
	public void getAllTemplates_shouldReturnTemplates() throws MessageException {
		// Act
		var templates = messageService.getAllTemplates();
		
		// Assert
		assertNotNull(templates);
		assertEquals(1, templates.size());  // Assuming there's at least one template in the dataset
	}
	
	@Test
	public void getTemplate_shouldReturnTemplateById() throws MessageException {
		// Arrange
		Integer templateId = 1; // Assuming template with ID 1 exists
		
		// Act
		Template template = messageService.getTemplate(templateId);
		
		// Assert
		assertNotNull(template);
		assertEquals(templateId, template.getId());
	}
	
	@Test
	public void getTemplatesByName_shouldReturnTemplatesByName() throws MessageException {
		// Arrange
		String templateName = "Test Template";
		
		// Act
		var templates = messageService.getTemplatesByName(templateName);
		
		// Assert
		assertNotNull(templates);
		assertEquals(1, templates.size());  // Assuming there's at least one template matching the name
	}
}
