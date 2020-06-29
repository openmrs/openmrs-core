/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This test class (should) contain tests for all of the {@link UserByNameComparator} methods.
 */
public class UserByNameComparatorTest {

	private User user1;
	private User user2;
	private User user3;
	private User nullUser1;
	private User nullUser2;

	@Before
	public void setUp() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		user1 = new User(person1);
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		user2 = new User(person2);
		Person person3 = new Person();
		person3.addName(new PersonName("givenName", "middleNamf", "familyNaae"));
		user3 = new User(person3);
		nullUser1 = null;
		nullUser2 = null;
	}

	/**
	 * This tests sorting with the {@link UserByNameComparator} given a set of users with
	 * personNames
	 *
	 * @see UserByNameComparator#compare(User, User)
	 */
	@Test
	public void compare_shouldSortUsersByPersonNames() {

		Person person3 = new Person();
		person3.addName(new PersonName("givenName", "middleNamg", "familyName"));
		Person person4 = new Person();
		person4.addName(new PersonName("givenName", "middleNamh", "familyName"));
		User User4 = new User(person4);

		List<User> listToSort = new ArrayList<>();
		// add the users randomly
		listToSort.add(user3);
		listToSort.add(user1);
		listToSort.add(User4);
		listToSort.add(user2);

		// sort the list with userByNameComparator
		listToSort.sort(new UserByNameComparator());

		// make sure that the users are sorted in the expected order
		Iterator<User> it = listToSort.iterator();
		Assert.assertTrue("Expected user3 to be the first in the sorted user list but wasn't", user3.equals(it.next()));
		Assert.assertTrue("Expected user1 to be the second in the sorted user list but wasn't", user1.equals(it.next()));
		Assert.assertTrue("Expected user2 to be the third in the sorted user list but wasn't", user2.equals(it.next()));
		Assert.assertTrue("Expected user4 to be the fourth in the sorted user list but wasn't", User4.equals(it.next()));
	}

	/**
	 * This tests sorting with the {@link UserByNameComparator} given a set of users with
	 * personNames (first parameter null and second parameter as valid user)
	 *
	 * @see UserByNameComparator#compare(User, User)
	 */
	@Test
	public void compare_shouldReturnSortedListOfUsersWithNullUsersAtLast() {
		List<User> listToSort = new ArrayList<>();
		// added the users randomly
		listToSort.add(nullUser1);
		listToSort.add(user2);
		listToSort.add(nullUser2);
		listToSort.add(user1);
		listToSort.add(user3);

		listToSort.sort(new UserByNameComparator());
		Iterator<User> sortedListIterator = listToSort.iterator();

		Assert.assertTrue("Expected user3 to be the first in the sorted user list but wasn't",
			user3.equals(sortedListIterator.next()));
		Assert.assertTrue("Expected user1 to be the second in the sorted user list but wasn't",
			user1.equals(sortedListIterator.next()));
		Assert.assertTrue("Expected user2 to be the third in the sorted user list but wasn't",
			user2.equals(sortedListIterator.next()));

	}
}
