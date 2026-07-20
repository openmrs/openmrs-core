package org.openmrs.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.APIException;

public class BootstrapPasswordTest {
    
    private User user;
    private String testSalt = "test-salt-123";
    
    @BeforeEach
    public void setUp() {
        System.setProperty("openmrs.bootstrap.systemSalt", testSalt);
        
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
    public void testGenerateBootstrapPassword_shouldThrowExceptionWhenSystemSaltMissing() {
        System.clearProperty("openmrs.bootstrap.systemSalt");
        // Also ensure global property is not set (would require mocking Context)
        assertThrows(APIException.class, () -> Security.generateBootstrapPassword(user));
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