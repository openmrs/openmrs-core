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
import org.openmrs.Location;
import org.openmrs.Retireable;
import org.openmrs.User;
import org.openmrs.test.Verifies;

/**
 * Tests for the {@link BaseRetireHandler} class.
 */
public class BaseRetireHandlerTest {
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set the retired bit", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldSetTheRetiredBit() throws Exception {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(false); // make sure isRetired is false
		handler.handle(retireable, null, null, " ");
		Assert.assertTrue(retireable.isRetired());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set the retireReason", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldSetTheRetireReason() throws Exception {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		handler.handle(retireable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", retireable.getRetireReason());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set retired by", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldSetRetiredBy() throws Exception {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		handler.handle(retireable, new User(2), null, " ");
		Assert.assertEquals(2, retireable.getRetiredBy().getId().intValue());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set retired by if non null", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldNotSetRetiredByIfNonNull() throws Exception {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetiredBy(new User(3));
		handler.handle(retireable, new User(2), null, " ");
		Assert.assertEquals(3, retireable.getRetiredBy().getId().intValue());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set dateRetired", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldSetDateRetired() throws Exception {
		Date d = new Date();
		
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		handler.handle(retireable, null, d, " ");
		Assert.assertEquals(d, retireable.getDateRetired());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set dateRetired if non null", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldNotSetDateRetiredIfNonNull() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that is not "now"
		
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setDateRetired(d); // make dateRetired non null
		
		handler.handle(retireable, null, new Date(), " ");
		Assert.assertEquals(d, retireable.getDateRetired());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set the retireReason if already voided", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldNotSetTheRetireReasonIfAlreadyVoided() throws Exception {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setRetiredBy(new User());
		handler.handle(retireable, null, null, "THE REASON");
		Assert.assertNull(retireable.getRetireReason());
	}
	
	/**
	 * @see {@link BaseRetireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set retiredBy even if retired bit is set but retiredBy is null", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldSetRetiredByEvenIfRetiredBitIsSetButRetiredByIsNull() throws Exception {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		handler.handle(retireable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", retireable.getRetireReason());
	}
	
}
