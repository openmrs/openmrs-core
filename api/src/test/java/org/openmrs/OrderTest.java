/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.Reflect;

/**
 * This class tests all methods that are not getter or setters in the Order java object TODO: finish
 * this test class for Order
 * 
 * @see Order
 */
public class OrderTest extends BaseContextSensitiveTest {
	
	private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	protected static void assertThatAllFieldsAreCopied(Order original, String methodName, String... otherfieldsToExclude)
	        throws Exception {
		if (methodName == null) {
			methodName = "copy";
		}
		List<String> fieldsToExclude = new ArrayList<String>();
		fieldsToExclude.addAll(Arrays.asList("log", "serialVersionUID", "orderId", "uuid"));
		if (otherfieldsToExclude != null) {
			fieldsToExclude.addAll(Arrays.asList(otherfieldsToExclude));
		}
		List<Field> fields = Reflect.getAllFields(original.getClass());
		for (Field field : fields) {
			if (fieldsToExclude.contains(field.getName())) {
				continue;
			}
			field.setAccessible(true);
			Object fieldValue = field.get(original);
			
			if (fieldValue == null) {
				if (field.getType().isEnum()) {
					fieldValue = field.getType().getEnumConstants()[0];
				} else if (field.getType().equals(Boolean.class)) {
					fieldValue = true;
				} else if (field.getType().equals(Integer.class)) {
					fieldValue = 10;
				} else if (field.getType().equals(Double.class)) {
					fieldValue = 5.0;
				} else {
					fieldValue = field.getType().newInstance();
				}
				field.set(original, fieldValue);
			}
		}
		
		Order copy = (Order) MethodUtils.invokeExactMethod(original, methodName, null);
		for (Field field : fields) {
			Object copyValue = field.get(copy);
			if (fieldsToExclude.contains(field.getName())) {
				continue;
			}
			assertNotNull("Order." + methodName + " should set " + field.getName() + " on the new Order", copyValue);
			assertEquals("Order." + methodName + " should set " + field.getName() + " on the new Order",
			    field.get(original), copyValue);
		}
	}
	
	/**
	 * Tests the {@link Order#isDiscontinuedRightNow()} method TODO this should be split into many
	 * different tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldIsDiscontinued() throws Exception {
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Order o = new Order();
		assertFalse("order without dates shouldn't be discontinued", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setDateActivated(ymd.parse("2007-01-01"));
		assertFalse("shouldn't be discontinued before date activated", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("order without no end dates shouldn't be discontinued", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		assertFalse("shouldn't be discontinued before date activated", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be discontinued before autoExpireDate", o.isDiscontinued(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be discontinued after autoExpireDate", o.isDiscontinued(ymd.parse("2008-10-26")));
		
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));
		assertFalse("shouldn't be discontinued before date activated", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be discontinued before dateStopped", o.isDiscontinued(ymd.parse("2007-10-26")));
		assertTrue("should be discontinued after dateStopped", o.isDiscontinued(ymd.parse("2007-11-26")));
		
	}
	
	/**
	 * Tests the {@link Order#isActive()} method TODO this should be split into many different tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCheckIfOrderIsActive() throws Exception {
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Order o = new Order();
		assertTrue("dateActivated==null && no end date should always be current", o.isActive(ymd.parse("2007-10-26")));
		
		o.setDateActivated(ymd.parse("2007-01-01"));
		assertFalse("shouldn't be current before dateActivated", o.isActive(ymd.parse("2006-10-26")));
		assertTrue("should be current after dateActivated", o.isActive(ymd.parse("2007-10-26")));
		
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		assertFalse("shouldn't be current before dateActivated", o.isActive(ymd.parse("2006-10-26")));
		assertTrue("should be current between dateActivated and autoExpireDate", o.isActive(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after autoExpireDate", o.isActive(ymd.parse("2008-10-26")));
		
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before dateActivated", o.isActive(ymd.parse("2006-10-26")));
		assertTrue("should be current between dateActivated and dateStopped", o.isActive(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after dateStopped", o.isActive(ymd.parse("2007-11-26")));
		
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before dateActivated", o.isActive(ymd.parse("2006-10-26")));
		assertTrue("should be current between dateActivated and dateStopped", o.isActive(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after dateStopped", o.isActive(ymd.parse("2007-11-26")));
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see Order#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldSetAllTheRelevantFields() throws Exception {
		
		Order anOrder = new Order();
		anOrder.setPatient(new Patient());
		anOrder.setCareSetting(new CareSetting());
		anOrder.setConcept(new Concept());
		anOrder.setOrderType(new OrderType());
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals(anOrder.getPatient(), orderThatCanDiscontinueTheOrder.getPatient());
		
		assertEquals(anOrder.getConcept(), orderThatCanDiscontinueTheOrder.getConcept());
		
		assertEquals("should set previous order to anOrder", anOrder, orderThatCanDiscontinueTheOrder.getPreviousOrder());
		
		assertEquals("should set new order action to new", orderThatCanDiscontinueTheOrder.getAction(),
		    Order.Action.DISCONTINUE);
		
		assertEquals(anOrder.getCareSetting(), orderThatCanDiscontinueTheOrder.getCareSetting());
		
		assertEquals(anOrder.getOrderType(), orderThatCanDiscontinueTheOrder.getOrderType());
	}
	
	/**
	 * @verifies copy all fields
	 * @see Order#copy()
	 */
	@Test
	public void copy_shouldCopyAllFields() throws Exception {
		assertThatAllFieldsAreCopied(new Order(), null);
		assertThatAllFieldsAreCopied(new TestOrder(), null);
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see Order#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetAllTheRelevantFields() throws Exception {
		assertThatAllFieldsAreCopied(new Order(), "cloneForRevision", "creator", "dateCreated", "action", "changedBy",
		    "dateChanged", "voided", "dateVoided", "voidedBy", "voidReason", "encounter", "orderNumber", "orderer",
		    "previousOrder", "dateActivated", "dateStopped", "accessionNumber");
	}
	
	/**
	 * @verifies true if it is the same or is a subtype
	 * @see Order#isType(OrderType)
	 */
	@Test
	public void isType_shouldTrueIfItIsTheSameOrIsASubtype() throws Exception {
		Order order = new Order();
		OrderType orderType = new OrderType();
		OrderType subType1 = new OrderType();
		OrderType subType2 = new OrderType();
		subType2.setParent(subType1);
		subType1.setParent(orderType);
		order.setOrderType(subType2);
		
		assertTrue(order.isType(subType2));
		assertTrue(order.isType(subType1));
		assertTrue(order.isType(orderType));
	}
	
	/**
	 * @verifies false if it neither the same nor a subtype
	 * @see Order#isType(OrderType)
	 */
	@Test
	public void isType_shouldFalseIfItNeitherTheSameNorASubtype() throws Exception {
		Order order = new Order();
		order.setOrderType(new OrderType());
		
		assertFalse(order.isType(new OrderType()));
	}
	
	/**
	 * @verifies set the relevant fields for a DC order
	 * @see Order#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetTheRelevantFieldsForADCOrder() throws Exception {
		Order order = new Order();
		order.setAction(Order.Action.DISCONTINUE);
		Date date = new Date();
		order.setDateActivated(date);
		order.setPreviousOrder(new Order());
		order.setAutoExpireDate(date);
		order.setAccessionNumber("some number");
		OrderUtilTest.setDateStopped(order, date);
		
		Order clone = order.cloneForRevision();
		assertEquals(Order.Action.DISCONTINUE, clone.getAction());
		assertEquals(order.getDateActivated(), clone.getDateActivated());
		assertEquals(order.getPreviousOrder(), clone.getPreviousOrder());
		assertNull(clone.getAutoExpireDate());
		assertNull(clone.getDateStopped());
		assertNull(clone.getAccessionNumber());
	}
	
	/**
	 * @verifies return false if other order is null
	 * @see Order#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfOtherOrderIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(new Concept());
		
		assertFalse(order.hasSameOrderableAs(null));
	}
	
	/**
	 * @verifies return false if the concept of the orders do not match
	 * @see Order#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfTheConceptOfTheOrdersDoNotMatch() throws Exception {
		Order order = new Order();
		order.setConcept(new Concept());
		
		Order otherOrder = new Order();
		otherOrder.setConcept(new Concept());
		
		assertFalse(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @verifies return true if the orders have the same concept
	 * @see Order#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnTrueIfTheOrdersHaveTheSameConcept() throws Exception {
		Order order = new Order();
		Concept concept = new Concept();
		order.setConcept(concept);
		
		Order otherOrder = new Order();
		otherOrder.setConcept(concept);
		assertTrue(order.hasSameOrderableAs(otherOrder));
	}
	
	/**
	 * @verifies return scheduledDate if Urgency is Scheduled
	 * @see Order#getEffectiveStartDate()
	 */
	@Test
	public void getEffectiveStartDate_shouldReturnScheduledDateIfUrgencyIsScheduled() throws Exception {
		Order order = new Order();
		Date date = DateUtils.addDays(new Date(), 2);
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(date);
		order.setDateActivated(new Date());
		
		assertEquals(date, order.getEffectiveStartDate());
	}
	
	/**
	 * @verifies return dateActivated if Urgency is not Scheduled
	 * @see Order#getEffectiveStartDate()
	 */
	@Test
	public void getEffectiveStartDate_shouldReturnDateActivatedIfUrgencyIsNotScheduled() throws Exception {
		Order order = new Order();
		Date date = new Date();
		order.setScheduledDate(DateUtils.addDays(date, 2));
		order.setDateActivated(date);
		
		assertEquals(date, order.getEffectiveStartDate());
	}
	
	/**
	 * @verifies return dateStopped if dateStopped is not null
	 * @see Order#getEffectiveStopDate()
	 */
	@Test
	public void getEffectiveStopDate_shouldReturnDateStoppedIfDateStoppedIsNotNull() throws Exception {
		Order order = new Order();
		Date dateStopped = DateUtils.addDays(new Date(), 4);
		OrderUtilTest.setDateStopped(order, dateStopped);
		order.setAutoExpireDate(new Date());
		
		assertEquals(dateStopped, order.getEffectiveStopDate());
	}
	
	/**
	 * @verifies return autoExpireDate if dateStopped is null
	 * @see Order#getEffectiveStopDate()
	 */
	@Test
	public void getEffectiveStopDate_shouldReturnAutoExpireDateIfDateStoppedIsNull() throws Exception {
		Order order = new Order();
		Date date = DateUtils.addDays(new Date(), 4);
		order.setAutoExpireDate(date);
		
		assertEquals(date, order.getEffectiveStopDate());
	}
	
	/**
	 * @verifies return false for a voided order
	 * @see Order#isFuture(java.util.Date)
	 */
	@Test
	public void isFuture_shouldReturnFalseForAVoidedOrder() throws Exception {
		Order order = new Order();
		order.setVoided(true);
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertFalse(order.isFuture(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if dateActivated is null
	 * @see Order#isFuture(java.util.Date)
	 */
	@Test
	public void isFuture_shouldReturnFalseIfDateActivatedIsNull() throws Exception {
		Order order = new Order();
		assertNull(order.getDateActivated());
		assertFalse(order.isFuture(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if order was activated on the check date
	 * @see Order#isFuture(java.util.Date)
	 */
	@Test
	public void isFuture_shouldReturnFalseIfOrderWasActivatedOnTheCheckDate() throws Exception {
		Order order = new Order();
		Date dateActivated = DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT);
		order.setDateActivated(dateActivated);
		assertFalse(order.isFuture(dateActivated));
	}
	
	/**
	 * @verifies return true if order was activated after the check date
	 * @see Order#isFuture(java.util.Date)
	 */
	@Test
	public void isFuture_shouldReturnTrueIfOrderWasActivatedAfterTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertTrue(order.isFuture(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false for a voided order
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnFalseForAVoidedOrder() throws Exception {
		Order order = new Order();
		order.setVoided(true);
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if date stopped and auto expire date are both null
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnFalseIfDateStoppedAndAutoExpireDateAreBothNull() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if auto expire date is null and date stopped is equal to check date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnFalseIfAutoExpireDateIsNullAndDateStoppedIsEqualToCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		Date checkDate = DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT);
		OrderUtilTest.setDateStopped(order, checkDate);
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isDiscontinued(checkDate));
	}
	
	/**
	 * @verifies return false if auto expire date is null and date stopped is after check date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnFalseIfAutoExpireDateIsNullAndDateStoppedIsAfterCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if dateActivated is after check date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnFalseIfDateActivatedIsAfterCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT));
		assertFalse(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return true if auto expire date is null and date stopped is before check date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnTrueIfAutoExpireDateIsNullAndDateStoppedIsBeforeCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertNull(order.getAutoExpireDate());
		assertTrue(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies fail if date stopped is after auto expire date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldFailIfDateStoppedIsAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.error.invalidDateStoppedAndAutoExpireDate");
		order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT));
	}
	
	/**
	 * @verifies return true if check date is after date stopped but before auto expire date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnTrueIfCheckDateIsAfterDateStoppedButBeforeAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT));
		assertTrue(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return true if check date is after both date stopped auto expire date
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnTrueIfCheckDateIsAfterBothDateStoppedAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		assertTrue(order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false for a voided order
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseForAVoidedOrder() throws Exception {
		Order order = new Order();
		order.setVoided(true);
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertFalse(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if date stopped and auto expire date are both null
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseIfDateStoppedAndAutoExpireDateAreBothNull() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if date stopped is null and auto expire date is equal to check date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseIfDateStoppedIsNullAndAutoExpireDateIsEqualToCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		Date checkDate = DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT);
		order.setAutoExpireDate(checkDate);
		assertNull(order.getDateStopped());
		assertFalse(order.isExpired(checkDate));
	}
	
	/**
	 * @verifies return false if date stopped is null and auto expire date is after check date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseIfDateStoppedIsNullAndAutoExpireDateIsAfterCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertFalse(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if check date is after both date stopped auto expire date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseIfCheckDateIsAfterBothDateStoppedAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		assertFalse(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if dateActivated is after check date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseIfDateActivatedIsAfterCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertFalse(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if check date is after date stopped but before auto expire date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnFalseIfCheckDateIsAfterDateStoppedButBeforeAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT));
		assertFalse(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies fail if date stopped is after auto expire date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldFailIfDateStoppedIsAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Order.error.invalidDateStoppedAndAutoExpireDate");
		order.isExpired(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT));
	}
	
	/**
	 * @verifies return true if date stopped is null and auto expire date is before check date
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldReturnTrueIfDateStoppedIsNullAndAutoExpireDateIsBeforeCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertTrue(order.isExpired(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return true if an order expired on the check date
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnTrueIfAnOrderExpiredOnTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		Date checkDate = DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT);
		order.setAutoExpireDate(checkDate);
		assertNull(order.getDateStopped());
		assertTrue(order.isActive(checkDate));
	}
	
	/**
	 * @verifies return true if an order was activated on the check date
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnTrueIfAnOrderWasActivatedOnTheCheckDate() throws Exception {
		Order order = new Order();
		Date activationDate = DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT);
		order.setDateActivated(activationDate);
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertTrue(order.isActive(activationDate));
	}
	
	/**
	 * @verifies return true if an order was discontinued on the check date
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnTrueIfAnOrderWasDiscontinuedOnTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		Date dateStopped = DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT);
		OrderUtilTest.setDateStopped(order, dateStopped);
		assertTrue(order.isActive(dateStopped));
	}
	
	/**
	 * @verifies return false for a discontinued order
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnFalseForADiscontinuedOrder() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertFalse(order.isActive(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false for an expired order
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnFalseForAnExpiredOrder() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertFalse(order.isActive(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false for an order activated after the check date
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnFalseForAnOrderActivatedAfterTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isActive(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false for a voided order
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnFalseForAVoidedOrder() throws Exception {
		Order order = new Order();
		order.setVoided(true);
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isActive(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false for a discontinuation order
	 * @see Order#isActive(java.util.Date)
	 */
	@Test
	public void isActive_shouldReturnFalseForADiscontinuationOrder() throws Exception {
		Order order = new Order();
		Date activationDate = DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT);
		order.setDateActivated(activationDate);
		order.setAction(Order.Action.DISCONTINUE);
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isActive(activationDate));
	}
	
	/**
	 * @verifies return false for a voided order
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnFalseForAVoidedOrder() throws Exception {
		Order order = new Order();
		order.setVoided(true);
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if dateActivated is null
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnFalseIfDateActivatedIsNull() throws Exception {
		Order order = new Order();
		assertNull(order.getDateActivated());
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if the order is not yet activated as of the check date
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnFalseIfTheOrderIsNotYetActivatedAsOfTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return false if the order was scheduled to start after the check date
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnFalseIfTheOrderWasScheduledToStartAfterTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return true if the order was scheduled to start on the check date
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnTrueIfTheOrderWasScheduledToStartOnTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertTrue(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return true if the order was scheduled to start before the check date
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnTrueIfTheOrderWasScheduledToStartBeforeTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		order.setScheduledDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		assertTrue(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT)));
	}
	
	/**
	 * @verifies return true if the order is started and not scheduled
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnTrueIfTheOrderIsStartedAndNotScheduled() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertTrue(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
}
