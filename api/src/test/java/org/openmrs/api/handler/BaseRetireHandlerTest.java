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

/**
 * Tests for the {@link BaseRetireHandler} class.
 */
public class BaseRetireHandlerTest {
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetTheRetiredBit() {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(false); // make sure isRetired is false
		handler.handle(retireable, null, null, " ");
		Assert.assertTrue(retireable.getRetired());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetTheRetireReason() {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		handler.handle(retireable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", retireable.getRetireReason());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetRetiredBy() {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		handler.handle(retireable, new User(2), null, " ");
		Assert.assertEquals(2, retireable.getRetiredBy().getId().intValue());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetRetiredByIfNonNull() {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetiredBy(new User(3));
		handler.handle(retireable, new User(2), null, " ");
		Assert.assertEquals(3, retireable.getRetiredBy().getId().intValue());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetDateRetired() {
		Date d = new Date();
		
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		handler.handle(retireable, null, d, " ");
		Assert.assertEquals(d, retireable.getDateRetired());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetDateRetiredIfNonNull() {
		Date d = new Date(new Date().getTime() - 1000); // a time that is not "now"
		
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setDateRetired(d); // make dateRetired non null
		
		handler.handle(retireable, null, new Date(), " ");
		Assert.assertEquals(d, retireable.getDateRetired());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetTheRetireReasonIfAlreadyVoided() {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setRetiredBy(new User());
		handler.handle(retireable, null, null, "THE REASON");
		Assert.assertNull(retireable.getRetireReason());
	}
	
	/**
	 * @see BaseRetireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldSetRetiredByEvenIfRetiredBitIsSetButRetiredByIsNull() {
		RetireHandler<Retireable> handler = new BaseRetireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		handler.handle(retireable, null, null, "THE REASON");
		Assert.assertEquals("THE REASON", retireable.getRetireReason());
	}
	
}
