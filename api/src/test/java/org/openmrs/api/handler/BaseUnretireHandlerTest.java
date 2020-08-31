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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.Retireable;
import org.openmrs.User;

/**
 * Tests the {@link BaseUnretireHandler} class.
 */
public class BaseUnretireHandlerTest {
	
	/**
	 * @see BaseUnretireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetTheRetiredBit() {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true); // make sure isRetired is set
		handler.handle(retireable, null, null, null);
		assertFalse(retireable.getRetired());
	}
	
	/**
	 * @see BaseUnretireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetTheRetirer() {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setRetiredBy(new User(1));
		handler.handle(retireable, null, null, null);
		assertNull(retireable.getRetiredBy());
	}
	
	/**
	 * @see BaseUnretireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetTheDateRetired() {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setDateRetired(new Date());
		handler.handle(retireable, null, null, null);
		assertNull(retireable.getDateRetired());
	}
	
	/**
	 * @see BaseUnretireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetTheRetireReason() {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setRetireReason("SOME REASON");
		handler.handle(retireable, null, null, null);
		assertNull(retireable.getRetireReason());
	}
	
	/**
	 * @see BaseUnretireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotActOnAlreadyUnretiredObjects() {
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(false);
		handler.handle(retireable, null, null, "SOME REASON");
		assertNull(retireable.getRetireReason());
	}
	
	/**
	 * @see BaseUnretireHandler#handle(Retireable,User,Date,String)
	 */
	@Test
	public void handle_shouldNotActOnRetiredObjectsWithADifferentDateRetired() {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't right now
		
		UnretireHandler<Retireable> handler = new BaseUnretireHandler();
		Retireable retireable = new Location();
		retireable.setRetired(true);
		retireable.setDateRetired(d);
		
		handler.handle(retireable, null, new Date(), "SOME REASON");
		assertTrue(retireable.getRetired());
	}
	
}
