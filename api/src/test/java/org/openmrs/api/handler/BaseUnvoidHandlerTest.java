/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.Voidable;
import org.openmrs.test.Verifies;

/**
 * Tests the {@link BaseUnvoidHandler} class.
 */
public class BaseUnvoidHandlerTest {
	
	/**
	 * @see {@link BaseUnvoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the voided bit", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetTheVoidedBit() throws Exception {
		UnvoidHandler<Voidable> handler = new BaseUnvoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true); // make sure isVoided is set
		handler.handle(voidable, null, null, null);
		Assert.assertFalse(voidable.isVoided());
	}
	
	/**
	 * @see {@link BaseUnvoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the voider", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetTheVoider() throws Exception {
		UnvoidHandler<Voidable> handler = new BaseUnvoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		voidable.setVoidedBy(new User(1));
		handler.handle(voidable, null, null, null);
		Assert.assertNull(voidable.getVoidedBy());
	}
	
	/**
	 * @see {@link BaseUnvoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the dateVoided", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetTheDateVoided() throws Exception {
		UnvoidHandler<Voidable> handler = new BaseUnvoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		voidable.setDateVoided(new Date());
		handler.handle(voidable, null, null, null);
		Assert.assertNull(voidable.getDateVoided());
	}
	
	/**
	 * @see {@link BaseUnvoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the VoidReason", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetTheVoidReason() throws Exception {
		UnvoidHandler<Voidable> handler = new BaseUnvoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		voidable.setVoidReason("SOME REASON");
		handler.handle(voidable, null, null, null);
		Assert.assertNull(voidable.getVoidReason());
	}
	
	/**
	 * @see {@link BaseUnvoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should only act on already voided objects", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldOnlyActOnAlreadyVoidedObjects() throws Exception {
		UnvoidHandler<Voidable> handler = new BaseUnvoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(false);
		handler.handle(voidable, null, null, "SOME REASON");
		Assert.assertNull(voidable.getVoidReason());
	}
	
	/**
	 * @see {@link BaseUnvoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not act on objects with a different dateVoided", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotActOnObjectsWithADifferentDateVoided() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't right now
		
		UnvoidHandler<Voidable> handler = new BaseUnvoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		voidable.setDateVoided(d);
		
		handler.handle(voidable, null, new Date(), "SOME REASON");
		Assert.assertTrue(voidable.isVoided());
	}
}
