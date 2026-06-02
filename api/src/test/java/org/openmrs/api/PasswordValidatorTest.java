package org.openmrs.api;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PasswordValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private UserService userService;
	
	@Test
	public void shouldInvokeRegisteredValidators() throws Exception{
	
		PasswordValidator mockValidator = mock(PasswordValidator.class);
		userService.setPasswordValidator(Collections.singletonList(mockValidator));
		
		User user = new User();
		user.setUsername("testuser");

		Person person = new Person();
		person.addName(new PersonName("Test", "User", "Validator"));
		person.setGender("M");
		user.setPerson(person);
		String password = "Password123";
		
		userService.createUser(user , password);
		verify(mockValidator).validate(user , password);
	}
	
	@Test
	public void shouldThrowExceptionWhenValidatorFails(){
		PasswordValidator failingValidator = (user , pw) -> {
			throw new WeakPasswordException("Custom failure");
		};
		
		userService.setPasswordValidator(Collections.singleton(failingValidator));
		
		User user = new User();
		user.setUsername("testuser");
		
		Person person = new Person();
		person.addName(new PersonName("Test", "User", "Validator"));
		person.setGender("M");
		user.setPerson(person);
		
		assertThrows(WeakPasswordException.class, () -> userService.createUser(user, "anyPassword"));
	}
	
	
}
