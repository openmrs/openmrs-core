package org.openmrs.notification.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.notification.*;
import org.openmrs.util.OpenmrsConstants;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @InjectMocks
    private MessageServiceImpl messageService;

    @Mock
    private TemplateDAO templateDAO;

    @Mock
    private MessageSender messageSender;

    @Mock
    private MessagePreparator messagePreparator;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private User mockUser;

    @Mock
    private Role mockRole;

    private Message message;

    @BeforeEach
    void setUp() {
        message = new Message();
        message.setRecipients("recipient@example.com");
        message.setSender("sender@example.com");
        message.setSubject("Test Subject");
        message.setContent("Test Content");
    }

    @Test
    void testSendMessage_Success() throws MessageException {
        doNothing().when(messageSender).send(message);
        
        assertDoesNotThrow(() -> messageService.sendMessage(message));
        verify(messageSender, times(1)).send(message);
    }

    @Test
    void testSendMessage_Failure() throws MessageException {
        doThrow(new MessageException("Sending failed")).when(messageSender).send(message);
        
        assertThrows(MessageException.class, () -> messageService.sendMessage(message));
    }

    @Test
    void testCreateMessage_WithAllFields() throws MessageException {
        Message createdMessage = messageService.createMessage("recipient@example.com", "sender@example.com", "Subject", "Content");
        assertNotNull(createdMessage);
        assertEquals("recipient@example.com", createdMessage.getRecipients());
        assertEquals("sender@example.com", createdMessage.getSender());
        assertEquals("Subject", createdMessage.getSubject());
        assertEquals("Content", createdMessage.getContent());
    }

    @Test
    void testSendMessage_ToUser() throws MessageException {
        when(mockUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS)).thenReturn("user@example.com");
        doNothing().when(messageSender).send(message);
        
        messageService.sendMessage(message, mockUser);
        assertTrue(message.getRecipients().contains("user@example.com"));
    }

    @Test
    void testSendMessage_ToRole() throws MessageException {
        List<Role> roles = Collections.singletonList(mockRole);
        List<User> users = Collections.singletonList(mockUser);
        when(Context.getUserService().getUsers(null, roles, false)).thenReturn(users);
        when(mockUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_NOTIFICATION_ADDRESS)).thenReturn("user@example.com");
        
        messageService.sendMessage(message, mockRole);
        
        verify(Context.getMessageService(), times(1)).sendMessage(eq(message), eq(users));
    }

    @Test
    void testPrepareMessage_Success() throws MessageException {
        Template template = mock(Template.class);
        when(messagePreparator.prepare(template)).thenReturn(message);
        
        Message preparedMessage = messageService.prepareMessage(template);
        
        assertNotNull(preparedMessage);
        assertEquals(message, preparedMessage);
    }

    @Test
    void testGetAllTemplates() throws MessageException {
        List<Template> templates = new ArrayList<>();
        when(templateDAO.getTemplates()).thenReturn(templates);
        
        List result = messageService.getAllTemplates();
        
        assertNotNull(result);
        assertEquals(templates, result);
    }
}
