package org.openmrs.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

public class BootstrapPasswordTest {
    
    private User user;
    private String testSalt = "test-salt-123";
    
    @BeforeEach
    public void setUp() {
        // Set runtime property for testing
        Properties props = new Properties();
        props.setProperty("openmrs.bootstrap.pepper", "test-pepper-123");
        Context.setRuntimeProperties(props);
        
        user = new User();
        user.setUuid("123e4567-e89b-12d3-a456-426614174000");
        user.setUsername("testuser");
    }
    
    @Test
    public void testGenerateDeterministicHash_shouldReturnSameOutputForSameInput() {
        String input = "test-input";
        String hash1 = Security.generateDeterministicHash(input);
        String hash2 = Security.generateDeterministicHash(input);
        assertEquals(hash1, hash2, "Same input should produce same hash");
    }
    
    @Test
    public void testGenerateDeterministicHash_shouldReturnDifferentOutputForDifferentInputs() {
        String hash1 = Security.generateDeterministicHash("input-1");
        String hash2 = Security.generateDeterministicHash("input-2");
        assertNotEquals(hash1, hash2, "Different inputs should produce different hashes");
    }
    
    @Test
    public void testGenerateBootstrapPassword_shouldReturnPassword() {
        String password = Security.generateBootstrapPassword(user);
        assertNotNull(password);
        assertFalse(password.isEmpty());
        assertTrue(password.length() <= 20, "Password should be truncated to 20 characters");
    }
    
    @Test
    public void testGenerateBootstrapPassword_shouldBeDeterministic() {
        String password1 = Security.generateBootstrapPassword(user);
        String password2 = Security.generateBootstrapPassword(user);
        assertEquals(password1, password2, "Bootstrap password should be deterministic");
    }
    
    @Test
    public void testGenerateBootstrapPassword_shouldReturnDifferentForDifferentUsers() {
        User user2 = new User();
        user2.setUuid("9876f543-e21d-12a3-b456-426614174000");
        
        String password1 = Security.generateBootstrapPassword(user);
        String password2 = Security.generateBootstrapPassword(user2);
        assertNotEquals(password1, password2, "Different users should have different passwords");
    }
    
    @Test
    public void testValidateBootstrapPassword_shouldReturnTrueForCorrectPassword() {
        String password = Security.generateBootstrapPassword(user);
        assertTrue(Security.validateBootstrapPassword(user, password));
    }
    
    @Test
    public void testValidateBootstrapPassword_shouldReturnFalseForIncorrectPassword() {
        String password = Security.generateBootstrapPassword(user);
        assertFalse(Security.validateBootstrapPassword(user, password + "wrong"));
    }
    
    @Test
    public void testValidateBootstrapPassword_shouldReturnFalseForNullUser() {
        assertFalse(Security.validateBootstrapPassword(null, "anyPassword"));
    }
    
    @Test
    public void testValidateBootstrapPassword_shouldReturnFalseForNullPassword() {
        assertFalse(Security.validateBootstrapPassword(user, null));
    }
    
    @Test
    public void testGenerateBootstrapPassword_shouldThrowExceptionForNullUser() {
        assertThrows(APIException.class, () -> Security.generateBootstrapPassword(null));
    }
    
    @Test
    public void testGenerateBootstrapPassword_shouldThrowExceptionForUserWithoutUuid() {
        User userNoUuid = new User();
        userNoUuid.setUuid(null); // <-- Explicitly clear the UUID
        assertThrows(APIException.class, () -> Security.generateBootstrapPassword(userNoUuid));
    }
    
    @Test
    public void testGenerateBootstrapPassword_shouldThrowExceptionWhenPepperMissing() {
        // Save original runtime properties
        Properties originalProps = Context.getRuntimeProperties();
        try {
            // Set empty runtime properties
            Properties emptyProps = new Properties();
            Context.setRuntimeProperties(emptyProps);
            
            assertThrows(APIException.class, () -> Security.generateBootstrapPassword(user));
        } finally {
            // Restore original runtime properties
            Context.setRuntimeProperties(originalProps);
        }
    }
    
    @Test
    public void testIsBootstrapPasswordExpired_shouldReturnTrueWhenUserHasChangePasswordFlag() {
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "true");
        assertTrue(Security.isBootstrapPasswordExpired(user));
    }
    
    @Test
    public void testIsBootstrapPasswordExpired_shouldReturnFalseWhenUserDoesNotHaveChangePasswordFlag() {
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "false");
        assertFalse(Security.isBootstrapPasswordExpired(user));
    }
    
    @Test
    public void testIsBootstrapPasswordExpired_shouldReturnFalseForNullUser() {
        assertFalse(Security.isBootstrapPasswordExpired(null));
    }
}