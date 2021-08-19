/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;

/**
 * Tests all methods on the {@link Result} object
 * 
 * @see Result
 */
public class ResultTest {
	
	@Test
	public void toObject_shouldReturnResultObjectForSingleResults() {
		Result firstResult = new Result(new Date(), "some value", new Encounter(123));
		
		assertEquals(123, ((Encounter) firstResult.toObject()).getId().intValue());
	}
	
	@Test
	public void Result_shouldNotFailWithNullList() {
		new Result((List<Result>) null);
	}
	
	@Test
	public void Result_shouldNotFailWithEmptyList() {
		new Result(new ArrayList<Result>());
	}
	
	@Test
	public void Result_shouldNotFailWithNullResult() {
		new Result((Result) null);
	}
	
	@Test
	public void earliest_shouldGetTheFirstResultGivenMultipleResults() throws ParseException {
		Result parentResult = new Result();
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some other value", new Encounter(124));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some value", new Encounter(123));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertEquals("some value", parentResult.earliest().toString());
	}
	
	@Test
	public void earliest_shouldGetTheResultGivenASingleResult() throws ParseException {
		Result parentResult = new Result();
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some other value", new Encounter(124));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some value", new Encounter(123));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertEquals("some value", parentResult.earliest().toString());
	}
	
	@Test
	public void earliest_shouldGetAnEmptyResultGivenAnEmptyResult() {
		Result parentResult = new EmptyResult();
		assertEquals(new EmptyResult(), parentResult.earliest());
	}
	
	@Test
	public void earliest_shouldNotGetTheResultWithNullResultDateGivenOtherResults() throws ParseException {
		Result parentResult = new Result();
		Result secondResult = new Result(null, "some value", new Encounter(123));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertEquals("some other value", parentResult.earliest().toString());
	}
	
	@Test
	public void earliest_shouldGetOneResultWithNullResultDatesForAllResults() {
		Result parentResult = new Result();
		Result firstResult = new Result(null, "some value", new Encounter(123));
		Result secondResult = new Result(null, "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertEquals("some value", parentResult.earliest().toString());
	}
	
	@Test
	public void equals_shouldReturnTrueOnTwoEmptyResults() {
		assertTrue(new EmptyResult().equals(new Result()));
	}
	
	@Test
	public void get_shouldGetEmptyResultForIndexesOutOfRange() throws ParseException {
		Result parentResult = new Result();
		Result secondResult = new Result(null, "some value", new Encounter(123));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		// 3 is greater than the number of entries in the parentResult
		assertEquals(new EmptyResult(), parentResult.get(3));
	}
	
	@Test
	public void isNull_shouldReturnFalse() {
		assertFalse(new Result().isNull());
	}
	
	@Test
	public void latest_shouldGetTheMostRecentResultGivenMultipleResults() throws ParseException {
		Result parentResult = new Result();
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some value", new Encounter(123));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertEquals("some value", parentResult.latest().toString());
	}
	
	@Test
	public void latest_shouldGetTheResultGivenASingleResult() throws ParseException {
		Result result = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		
		assertEquals("some other value", result.latest().toString());
	}
	
	@Test
	public void latest_shouldGetAnEmptyResultGivenAnEmptyResult() {
		assertEquals(new EmptyResult(), new Result().latest());
	}
	
	@Test
	public void latest_shouldGetTheResultWithNullResultDate() throws ParseException {
		Result parentResult = new Result();
		Result firstResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some value", new Encounter(123));
		Result secondResult = new Result(null, "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertEquals("some value", parentResult.latest().toString());
	}
	
	@Test
	public void toObject_shouldFailWhenContainsMultipleResults() throws ParseException {
		Result parentResult = new Result();
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some value", new Encounter(123));
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		assertThrows(LogicException.class, () -> parentResult.toObject());
	}
}
