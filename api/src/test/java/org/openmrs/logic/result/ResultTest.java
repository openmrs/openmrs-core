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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;

/**
 * Tests all methods on the {@link Result} object
 * 
 * @see Result
 */
public class ResultTest {
	
	@SuppressWarnings( { "MismatchedQueryAndUpdateOfCollection" })
	@Test
	public void toObject_shouldReturnResultObjectForSingleResults() throws Exception {
		Result firstResult = new Result(new Date(), "some value", new Encounter(123));
		
		Assert.assertEquals(123, ((Encounter) firstResult.toObject()).getId().intValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Result_shouldNotFailWithNullList() throws Exception {
		new Result((List) null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Result_shouldNotFailWithEmptyList() throws Exception {
		new Result(new ArrayList());
	}
	
	@Test
	public void Result_shouldNotFailWithNullResult() throws Exception {
		new Result((Result) null);
	}
	
	@Test
	public void earliest_shouldGetTheFirstResultGivenMultipleResults() throws Exception {
		Result parentResult = new Result();
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some other value", new Encounter(124));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some value", new Encounter(123));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Assert.assertEquals("some value", parentResult.earliest().toString());
	}
	
	@Test
	public void earliest_shouldGetTheResultGivenASingleResult() throws Exception {
		Result parentResult = new Result();
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some other value", new Encounter(124));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some value", new Encounter(123));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Assert.assertEquals("some value", parentResult.earliest().toString());
	}
	
	@Test
	public void earliest_shouldGetAnEmptyResultGivenAnEmptyResult() throws Exception {
		Result parentResult = new EmptyResult();
		Assert.assertEquals(new EmptyResult(), parentResult.earliest());
	}
	
	@Test
	public void earliest_shouldNotGetTheResultWithNullResultDateGivenOtherResults() throws Exception {
		Result parentResult = new Result();
		Result secondResult = new Result(null, "some value", new Encounter(123));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Assert.assertEquals("some other value", parentResult.earliest().toString());
	}
	
	@Test
	public void earliest_shouldGetOneResultWithNullResultDatesForAllResults() throws Exception {
		Result parentResult = new Result();
		Result firstResult = new Result(null, "some value", new Encounter(123));
		Result secondResult = new Result(null, "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Assert.assertEquals("some value", parentResult.earliest().toString());
	}
	
	@Test
	public void equals_shouldReturnTrueOnTwoEmptyResults() throws Exception {
		Assert.assertTrue(new EmptyResult().equals(new Result()));
	}
	
	@Test
	public void get_shouldGetEmptyResultForIndexesOutOfRange() throws Exception {
		Result parentResult = new Result();
		Result secondResult = new Result(null, "some value", new Encounter(123));
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		// 3 is greater than the number of entries in the parentResult
		Assert.assertEquals(new EmptyResult(), parentResult.get(3));
	}
	
	@Test
	public void isNull_shouldReturnFalse() throws Exception {
		Assert.assertFalse(new Result().isNull());
	}
	
	@Test
	public void latest_shouldGetTheMostRecentResultGivenMultipleResults() throws Exception {
		Result parentResult = new Result();
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some value", new Encounter(123));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Assert.assertEquals("some value", parentResult.latest().toString());
	}
	
	@Test
	public void latest_shouldGetTheResultGivenASingleResult() throws Exception {
		Result result = new Result(Context.getDateFormat().parse("12/08/2008"), "some other value", new Encounter(124));
		
		Assert.assertEquals("some other value", result.latest().toString());
	}
	
	@Test
	public void latest_shouldGetAnEmptyResultGivenAnEmptyResult() throws Exception {
		Assert.assertEquals(new EmptyResult(), new Result().latest());
	}
	
	@Test
	public void latest_shouldGetTheResultWithNullResultDate() throws Exception {
		Result parentResult = new Result();
		Result firstResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some value", new Encounter(123));
		Result secondResult = new Result(null, "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Assert.assertEquals("some value", parentResult.latest().toString());
	}
	
	@Test(expected = LogicException.class)
	public void toObject_shouldFailWhenContainsMultipleResults() throws Exception {
		Result parentResult = new Result();
		Result firstResult = new Result(Context.getDateFormat().parse("12/08/2008"), "some value", new Encounter(123));
		Result secondResult = new Result(Context.getDateFormat().parse("15/08/2008"), "some other value", new Encounter(124));
		
		parentResult.add(firstResult);
		parentResult.add(secondResult);
		
		Object toObject = parentResult.toObject();
		Assert.assertNull(toObject);
	}
}
