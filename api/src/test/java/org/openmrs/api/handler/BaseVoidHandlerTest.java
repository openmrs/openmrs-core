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
 * Tests for the {@link BaseVoidHandler} class.
 */
public class BaseVoidHandlerTest {
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set the voided bit", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldSetTheVoidedBit() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(false); // make sure isVoided is false
		handler.handle(voidable, null, null, " ");
		Assert.assertTrue(voidable.isVoided());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set the voidReason", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldSetTheVoidReason() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		handler.handle(voidable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", voidable.getVoidReason());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set voidedBy", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldSetVoidedBy() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		handler.handle(voidable, new User(2), null, " ");
		Assert.assertEquals(2, voidable.getVoidedBy().getId().intValue());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set voidedBy if non null", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotSetVoidedByIfNonNull() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoidedBy(new User(3));
		handler.handle(voidable, new User(2), null, " ");
		Assert.assertEquals(3, voidable.getVoidedBy().getId().intValue());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set dateVoided", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldSetDateVoided() throws Exception {
		Date d = new Date();
		
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		handler.handle(voidable, null, d, " ");
		Assert.assertEquals(d, voidable.getDateVoided());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set dateVoided if non null", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotSetDateVoidedIfNonNull() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that is not "now"
		
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setDateVoided(d); // make dateVoided non null
		
		handler.handle(voidable, null, new Date(), " ");
		Assert.assertEquals(d, voidable.getDateVoided());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set the voidReason if already voided", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotSetTheVoidReasonIfAlreadyVoided() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		voidable.setVoidedBy(new User(1));
		handler.handle(voidable, null, null, "THE REASON");
		Assert.assertNull(voidable.getVoidReason());
	}
	
	/**
	 * @see {@link BaseVoidHandler#handle(Voidable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set voidedBy even if voided bit is set but voidedBy is null", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldSetVoidedByEvenIfVoidedBitIsSetButVoidedByIsNull() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		Voidable voidable = new Person();
		voidable.setVoided(true);
		
		handler.handle(voidable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", voidable.getVoidReason());
	}
}
