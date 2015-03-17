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
 * Tests the {@link BaseUnretireHandler} class.
 */
public class BaseUnretireHandlerTest {
	
	/**
	 * @see {@link BaseUnretireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the retired bit", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldUnsetTheRetiredBit() throws Exception {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true); // make sure isRetired is set
		handler.handle(retireable, null, null, null);
		Assert.assertFalse(retireable.isRetired());
	}
	
	/**
	 * @see {@link BaseUnretireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the retirer", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldUnsetTheRetirer() throws Exception {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setRetiredBy(new User(1));
		handler.handle(retireable, null, null, null);
		Assert.assertNull(retireable.getRetiredBy());
	}
	
	/**
	 * @see {@link BaseUnretireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the date retired", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldUnsetTheDateRetired() throws Exception {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setDateRetired(new Date());
		handler.handle(retireable, null, null, null);
		Assert.assertNull(retireable.getDateRetired());
	}
	
	/**
	 * @see {@link BaseUnretireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the retire reason", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldUnsetTheRetireReason() throws Exception {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setRetireReason("SOME REASON");
		handler.handle(retireable, null, null, null);
		Assert.assertNull(retireable.getRetireReason());
	}
	
	/**
	 * @see {@link BaseUnretireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not act on already unretired objects", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldNotActOnAlreadyUnretiredObjects() throws Exception {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(false);
		handler.handle(retireable, null, null, "SOME REASON");
		Assert.assertNull(retireable.getRetireReason());
	}
	
	/**
	 * @see {@link BaseUnretireHandler#handle(Retireable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not act on retired objects with a different dateRetired", method = "handle(Retireable,User,Date,String)")
	public void handle_shouldNotActOnRetiredObjectsWithADifferentDateRetired() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't right now
		
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setDateRetired(d);
		
		handler.handle(retireable, null, new Date(), "SOME REASON");
		Assert.assertTrue(retireable.isRetired());
	}
	
}
