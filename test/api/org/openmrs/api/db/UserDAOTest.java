package org.openmrs.api.db;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class UserDAOTest extends BaseContextSensitiveTest {
	
	private UserDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {		
		
		if (dao == null)
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (UserDAO) applicationContext.getBean("userDAO");
	}
	
	/**
	 * @verifies {@link UserDAO#getUsers(String,List<QRole;>,null)} test = should escape sql
	 *           wildcards in searchPhrase
	 */
	@Test
	@Verifies(value = "should escape sql wildcards in searchPhrase", method = "getUsers(String, List, Boolean)")
	public void getUsers_shouldEscapeSqlWildcardsInSearchPhrase() throws Exception {
		
		User u = new User();		
		u.setPerson(new Person());
		u.getPerson().setGender("M");
		
		String wildcards[] = new String[] { "%", "_" };
		//for each of the wildcards in the array, insert a user with a username or names
		//with the wildcards and carry out a search for that user
		for (String wildcard : wildcards) {
			
			PersonName name = new PersonName("\\" + wildcard + "cats", wildcard + "and", wildcard + "dogs");
			name.setDateCreated(new Date());
			u.addName(name);
			u.setUsername(wildcard + "test" + wildcard);
			Context.getUserService().saveUser(u, "Openmr5xy");			
			
			//we expect only one matching name or or systemId  to be returned		
			int size = dao.getUsers(wildcard + "ca", null, false).size();
			Assert.assertEquals(1, size);
			
			//if actually the search returned the matching name or system id	
			String userName = (dao.getUsers(wildcard + "ca", null, false).get(0).getUsername());
			Assert.assertEquals("Test failed since no user containing the character " + wildcard + " was found, ", wildcard
			        + "test" + wildcard, userName);
			
		}
	}
}
