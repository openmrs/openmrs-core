/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.type.Type;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Auditable;
import org.openmrs.ConceptNumeric;
import org.openmrs.User;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.scheduler.timer.TimerSchedulerTask;
import org.openmrs.test.BaseContextSensitiveTest;

public class AuditableInterceptorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see AuditableInterceptor#onFlushDirty(Object,Serializable,Object[],Object[],String[],Type[])
	 * @verifies return false for non Auditable objects
	 */
	@Test
	public void onFlushDirty_shouldReturnFalseForNonAuditableObjects() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		Object o = new Object();
		
		boolean returnValue = interceptor.onFlushDirty(o, null, null, null, null, null);
		
		Assert.assertFalse("false should have been returned because we didn't pass in an Auditable or OpenmrsObject",
		    returnValue);
	}
	
	/**
	 * @see AuditableInterceptor#onFlushDirty(Object,Serializable,Object[],Object[],String[],Type[])
	 * @verifies set the changedBy field
	 */
	@Test
	public void onFlushDirty_shouldSetTheChangedByField() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		User u = new User();
		
		// sanity check
		Assert.assertTrue(u instanceof Auditable);
		
		String[] propertyNames = new String[] { "changedBy", "dateChanged" };
		Object[] currentState = new Object[] { "", null };
		Object[] previousState = new Object[] { "", null };
		
		interceptor.onFlushDirty(u, null, currentState, previousState, propertyNames, null);
		
		Assert.assertNotNull(currentState[0]);
	}
	
	/**
	 * @see AuditableInterceptor#onFlushDirty(Object,Serializable,Object[],Object[],String[],Type[])
	 * @verifies set the dateChanged field
	 */
	@Test
	public void onFlushDirty_shouldSetTheDateChangedField() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		User u = new User();
		
		// sanity check
		Assert.assertTrue(u instanceof Auditable);
		
		String[] propertyNames = new String[] { "changedBy", "dateChanged" };
		Object[] currentState = new Object[] { "", null };
		Object[] previousState = new Object[] { "", null };
		
		interceptor.onFlushDirty(u, null, currentState, previousState, propertyNames, null);
		
		Assert.assertNotNull(currentState[1]);
	}
	
	@Test
	public void onFlushDirty_shouldAddPersonChangedByForPerson() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		Person person = new Person();
		
		String[] propertyNames = new String[] { "personChangedBy" };
		Object[] currentState = new Object[] { null };
		
		interceptor.onFlushDirty(person, null, currentState, null, propertyNames, null);
		Assert.assertNotNull(currentState[0]);
	}
	
	@Test
	public void onFlushDirty_shouldAddPersonDateChangedForPerson() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		Person person = new Person();
		
		String[] propertyNames = new String[] { "personDateChanged" };
		
		Object[] currentState = new Object[] { null };
		
		interceptor.onFlushDirty(person, null, currentState, null, propertyNames, null);
		Assert.assertNotNull(currentState[0]);
	}
	
	/**
	 * @see AuditableInterceptor#onFlushDirty(Object,Serializable,Object[],Object[],String[],Type[])
	 * @verifies set the dateChanged field
	 */
	@Test
	public void onFlushDirty_shouldNotFailWithNullPreviousState() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		User u = new User();
		
		// sanity check
		Assert.assertTrue(u instanceof Auditable);
		
		String[] propertyNames = new String[] { "changedBy", "dateChanged" };
		Object[] currentState = new Object[] { "", null };
		
		interceptor.onFlushDirty(u, null, currentState, null, propertyNames, null);
	}
	
	/**
	 * This test makes sure that the AuditableInterceptor is registered on the session
	 * 
	 * @see AuditableInterceptor#onFlushDirty(Object,Serializable,Object[],Object[],String[],Type[])
	 * @verifies be called when saving an Auditable
	 */
	@Test
	public void onFlushDirty_shouldBeCalledWhenSavingAnAuditable() throws Exception {
		User u = Context.getUserService().getUser(1);
		
		u.setUsername("asdf");
		
		Date beforeDate = u.getDateChanged();
		
		Context.getUserService().saveUser(u, null);
		
		Date afterDate = u.getDateChanged();
		
		Assert.assertNotSame(beforeDate, afterDate);
	}
	
	/**
	 * @see {@link AuditableInterceptor#onFlushDirty(Object,Serializable,null,null,null,null)}
	 * @verifies should not fail when the daemon user modifies something
	 */
	@Test
	public void onFlushDirty_shouldNotFailWhenTheDaemonUserModifiesSomething() throws Throwable {
		new AsDaemonTask(new AbstractTask() {
			
			@Override
			public void execute() {
				ConceptNumeric weight = Context.getConceptService().getConceptNumeric(5089);
				Date dateChangedBefore = weight.getDateChanged();
				weight.setHiAbsolute(75d);
				Context.getConceptService().saveConcept(weight);
				Assert.assertNotSame(dateChangedBefore, weight.getDateChanged());
			}
		}).runTheTask();
	}
	
	private class AsDaemonTask extends TimerSchedulerTask {
		
		private Task task;
		
		public AsDaemonTask(Task task) {
			super(task);
			this.task = task;
		}
		
		public boolean runTheTask() throws Throwable {
			Daemon.executeScheduledTask(this.task);
			return true;
		}
	}
	
	/**
	 * @see AuditableInterceptor#onSave(Object,Serializable,Object[],String[],Type[])
	 * @verifies return true if dateCreated was null
	 */
	@Test
	public void onSave_shouldReturnTrueIfDateCreatedWasNull() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		User u = new User();
		
		String[] propertyNames = new String[] { "creator", "dateCreated" };
		Object[] currentState = new Object[] { 0, null };
		
		boolean result = interceptor.onSave(u, 0, currentState, propertyNames, null);
		Assert.assertTrue(result);
	}
	
	/**
	 * @see AuditableInterceptor#onSave(Object,Serializable,Object[],String[],Type[])
	 * @verifies return true if creator was null
	 */
	@Test
	public void onSave_shouldReturnTrueIfCreatorWasNull() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		User u = new User();
		
		String[] propertyNames = new String[] { "creator", "dateCreated" };
		Object[] currentState = new Object[] { null, new Date() };
		
		boolean result = interceptor.onSave(u, 0, currentState, propertyNames, null);
		Assert.assertTrue(result);
	}
	
	/**
	 * @see AuditableInterceptor#onSave(Object,Serializable,Object[],String[],Type[])
	 * @verifies return false if dateCreated and creator was not null
	 */
	@Test
	public void onSave_shouldReturnFalseIfDateCreatedAndCreatorWasNotNull() throws Exception {
		AuditableInterceptor interceptor = new AuditableInterceptor();
		
		User u = new User();
		
		String[] propertyNames = new String[] { "creator", "dateCreated" };
		Object[] currentState = new Object[] { 0, new Date() };
		
		boolean result = interceptor.onSave(u, 0, currentState, propertyNames, null);
		Assert.assertFalse(result);
	}
	
	/**
	 * @see AuditableInterceptor#onSave(Object,Serializable,Object[],String[],Type[])
	 * @verifies be called when saving OpenmrsObject
	 */
	@Test
	public void onSave_shouldBeCalledWhenSavingOpenmrsObject() throws Exception {
		User u = new User();
		
		u.setSystemId("user");
		u.setPerson(Context.getPersonService().getPerson(1));
		
		Context.getUserService().saveUser(u, "Admin123");
		
		Assert.assertSame(Context.getAuthenticatedUser(), u.getCreator());
		Assert.assertNotNull(u.getDateCreated());
	}
	
	@Test
	public void onSave_shouldPopulateDateCreatedForPersonIfNull() {
		Person person = createPersonWithNameAndAddress();
		Context.getPersonService().savePerson(person);
		Assert.assertNotNull(person.getDateCreated());
		Assert.assertNotNull(person.getPersonDateCreated());
	}
	
	@Test
	public void onSave_shouldPopulateCreatorForPersonIfNull() {
		Person person = createPersonWithNameAndAddress();
		Context.getPersonService().savePerson(person);
		Assert.assertNotNull(person.getCreator());
		Assert.assertNotNull(person.getPersonCreator());
	}
	
	@Test
	public void onSave_shouldPopulatePersonChangedByandPersonDateChangedIfPersonAlreadyExists() throws Exception {
		Person person = Context.getPersonService().getPerson(1);
		
		Assert.assertNull(person.getPersonChangedBy());
		Assert.assertNull(person.getPersonDateChanged());
		
		person.setGender("F");
		Context.flushSession();
		Context.getPersonService().savePerson(person);
		
		Assert.assertNotNull(person.getPersonChangedBy());
		Assert.assertNotNull(person.getPersonDateChanged());
	}
	
	private Person createPersonWithNameAndAddress() {
		Person person = new Person();
		person.setGender("M");
		PersonName name = new PersonName("givenName", "middleName", "familyName");
		person.addName(name);
		PersonAddress address = new PersonAddress();
		address.setAddress1("some address");
		person.addAddress(address);
		return person;
	}
}
