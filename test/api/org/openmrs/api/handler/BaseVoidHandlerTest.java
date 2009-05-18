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
		Assert.assertEquals(new User(2), voidable.getVoidedBy());
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
		Assert.assertEquals(new User(3), voidable.getVoidedBy());
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
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should throw IllegalArgumentException if voidReason is empty", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldThrowIllegalArgumentExceptionIfVoidReasonIsEmpty() throws Exception {
		VoidHandler<Voidable> handler = new BaseVoidHandler();
		handler.handle(new Person(), null, null, null);
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
