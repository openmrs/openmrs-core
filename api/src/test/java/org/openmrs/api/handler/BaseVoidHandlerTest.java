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

/**
 * Tests for the {@link BaseVoidHandler} class.
 */
public class BaseVoidHandlerTest {
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetTheVoidedBit() {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(false);
		handler.handle(voidable, null, null, " ");
		Assert.assertTrue(voidable.getVoided());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetTheVoidReason() {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		handler.handle(voidable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", voidable.getVoidReason());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetVoidedBy() {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		handler.handle(voidable, new User(2), null, " ");
		Assert.assertEquals(2, voidable.getVoidedBy().getId().intValue());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetVoidedByIfNonNull() {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoidedBy(new User(3));
		handler.handle(voidable, new User(2), null, " ");
		Assert.assertEquals(3, voidable.getVoidedBy().getId().intValue());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetDateVoided() {
		Date d = new Date();
		
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		handler.handle(voidable, null, d, " ");
		Assert.assertEquals(d, voidable.getDateVoided());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetDateVoidedIfNonNull() {
		Date d = new Date(new Date().getTime() - 1000); // a time that is not "now"
		
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setDateVoided(d); // make dateVoided non null
		
		handler.handle(voidable, null, new Date(), " ");
		Assert.assertEquals(d, voidable.getDateVoided());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetTheVoidReasonIfAlreadyVoided() {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		voidable.setVoidedBy(new User(1));
		handler.handle(voidable, null, null, "THE REASON");
		Assert.assertNull(voidable.getVoidReason());
	}
	
	/**
	 * @see BaseVoidHandler#handle(Voidable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetVoidedByEvenIfVoidedBitIsSetButVoidedByIsNull() {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		
		handler.handle(voidable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", voidable.getVoidReason());
	}
}
