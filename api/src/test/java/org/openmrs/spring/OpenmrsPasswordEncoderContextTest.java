/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.UserDAO;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.Security;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the real Spring context with {@code security.passwordEncoder=argon2} set, exactly the
 * configuration an administrator opts in to via openmrs-runtime.properties, and pins what that
 * property must do through the {@code openmrsPasswordEncoder} bean wired in
 * applicationContext-service.xml: newly written passwords are argon2-prefixed while previously
 * stored unprefixed legacy hashes keep authenticating.
 */
@TestPropertySource(properties = "security.passwordEncoder=argon2")
class OpenmrsPasswordEncoderContextTest extends BaseContextSensitiveTest {
	
	private static final String PASSWORD = "Openmr5xy";
	
	/**
	 * The bean in applicationContext-service.xml and the encoder set
	 * {@link PasswordEncoders#noContextEncoder()} falls back on are two separate definitions of the
	 * same policy. They are compared here because nothing else holds them together: the bean takes
	 * its work factors from {@code security.argon2.*} placeholders and the fallback set hard-codes
	 * them, so a change to one that is not mirrored in the other would only show up as a hash the
	 * update wizard believes needs upgrading.
	 */
	@Test
	void theBeanAndTheNoContextEncoderSet_shouldAgreeOnTheArgon2WorkFactors() {
		PasswordEncoder bean = applicationContext.getBean("openmrsPasswordEncoder", PasswordEncoder.class);
		PasswordEncoder noContextSet = PasswordEncoders.supportedEncoders().get(PasswordEncoders.ARGON2_ID);

		assertEquals(workFactorsOf(bean.encode("password")), workFactorsOf(noContextSet.encode("password")));
	}

	/**
	 * The parameter segment of a PHC string, e.g. {@code m=19456,t=2,p=1}.
	 */
	private static String workFactorsOf(String phcHash) {
		int start = phcHash.indexOf("v=19$") + "v=19$".length();
		return phcHash.substring(start, phcHash.indexOf('$', start));
	}

	@Test
	void optingInToArgon2_shouldWriteArgon2PrefixedHashesWhileLegacyHashesStillAuthenticate() {
		PasswordEncoder encoder = applicationContext.getBean("openmrsPasswordEncoder", PasswordEncoder.class);
		
		String prefixed = encoder.encode("password");
		assertTrue(prefixed.startsWith("{argon2}$argon2id$"), "expected an argon2-prefixed PHC hash, got: " + prefixed);
		assertTrue(encoder.matches("password", prefixed));
		assertFalse(encoder.matches("wrongPassword", prefixed));
		assertFalse(encoder.upgradeEncoding(prefixed), "a freshly written hash must not be offered for re-encoding");
		
		String legacyHash = new LegacyOpenmrsPasswordEncoder().encode("password");
		assertFalse(legacyHash.startsWith("{"), "sanity: the generated legacy hash must be unprefixed");
		assertTrue(encoder.matches("password", legacyHash), "a legacy hash stored before the opt-in must still authenticate");
		assertFalse(encoder.matches("wrongPassword", legacyHash));
		assertTrue(encoder.upgradeEncoding(legacyHash), "a legacy hash must be flagged for re-encoding after the opt-in");
	}

	/**
	 * The other tests here stop at the encoder, which leaves the hop from the DAO unaccounted for.
	 * The value that ends up in {@code login_credentials.password} is written by
	 * {@code HibernateUserDAO.changePassword}, so if that call ever went back to hashing locally
	 * instead of through {@code Security.encodePassword}, the opt-in would silently stop applying
	 * to real password changes while every encoder test kept passing.
	 */
	@Test
	void changingAPassword_shouldStoreAnArgon2HashThroughTheOpenmrsPasswordEncoderBean() {
		// A user of this test's own, rather than one of the seeded accounts: the in-memory database
		// is shared across the classes in a fork, and a seeded row is not this test to depend on.
		Person person = new Person();
		person.setDateCreated(new Date());
		person.setPersonDateCreated(person.getDateCreated());
		person.setGender("M");
		PersonName name = new PersonName("Argon", "H", "Tester");
		name.setDateCreated(new Date());
		person.addName(name);
		User user = new User();
		user.setSystemId("100-99");
		user.setPerson(person);
		user.setUsername("argon-hop-tester");
		user.addName(name);
		user.setDateCreated(new Date());
		Context.getUserService().createUser(user, PASSWORD);

		Context.getUserService().changePassword(user, "Openmr6zz");

		String stored = dao().getLoginCredential(user).getHashedPassword();
		assertTrue(stored.startsWith("{argon2}$argon2id$"),
			"changePassword must write through the configured encoder bean, but stored: " + stored);
		assertTrue(Security.checkPassword(stored, "Openmr6zz" + dao().getLoginCredential(user).getSalt()),
			"the stored hash must verify against the new password");
		assertFalse(Security.checkPassword(stored, "Openmr5xy" + dao().getLoginCredential(user).getSalt()));
	}

	/**
	 * Named for the bean in applicationContext-service.xml, matching the convention the existing
	 * UserDAOTest cases use when they need this DAO.
	 */
	private UserDAO dao() {
		return Context.getRegisteredComponent("userDAO", UserDAO.class);
	}
}
