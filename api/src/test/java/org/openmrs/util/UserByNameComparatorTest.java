/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.test.Verifies;

/**
 * This test class (should) contain tests for all of the {@link UserByNameComparator} methods.
 */
public class UserByNameComparatorTest {
	
	/**
	 * This tests sorting with the {@link UserByNameComparator} given a set of users with
	 * personNames
	 * 
	 * @see {@link UserByNameComparator#compare(User,User)}
	 */
	@Test
	@Verifies(value = "should sort users by personNames", method = "compare(User,User)")
	public void compare_shouldSortUsersByPersonNames() throws Exception {
		
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		User user1 = new User(person1);
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		User user2 = new User(person2);
		Person person3 = new Person();
		person3.addName(new PersonName("givenName", "middleNamg", "familyName"));
		User user3 = new User(person3);
		Person person4 = new Person();
		person4.addName(new PersonName("givenName", "middleNamh", "familyName"));
		User user4 = new User(person4);
		
		List<User> listToSort = new ArrayList<User>();
		// add the users randomly
		listToSort.add(user3);
		listToSort.add(user1);
		listToSort.add(user4);
		listToSort.add(user2);
		
		// sort the list with userByNameComparator
		Collections.sort(listToSort, new UserByNameComparator());
		
		// make sure that the users are sorted in the expected order
		Iterator<User> it = listToSort.iterator();
		Assert.assertTrue("Expected user1 to be the first in the sorted user list but wasn't", user1.equals(it.next()));
		Assert.assertTrue("Expected user2 to be the second in the sorted user list but wasn't", user2.equals(it.next()));
		Assert.assertTrue("Expected user3 to be the third in the sorted user list but wasn't", user3.equals(it.next()));
		Assert.assertTrue("Expected user4 to be the fourth in the sorted user list but wasn't", user4.equals(it.next()));
		;
	}
}
