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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Order.Urgency;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.Reflect;

/**
 * This class tests all methods that are not getter or setters in the Order java object TODO: finish
 * this test class for Order
 * 
 * @see Order
 */
public class OrderTest extends BaseContextSensitiveTest {
	
	
	private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private DateFormat ymd;

	private Order o;

	@BeforeEach
	public void setUp() throws Exception {
		ymd = new SimpleDateFormat("yyyy-MM-dd");
		o = new Order();
	}

	
	protected static void assertThatAllFieldsAreCopied(Order original, String methodName, String... otherfieldsToExclude)
	        throws Exception {
		if (methodName == null) {
			methodName = "copy";
		}
		List<String> fieldsToExclude = new ArrayList<>(Arrays.asList("log", "serialVersionUID", "orderId", "uuid"));
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
			assertNotNull(copyValue, "Order." + methodName + " should set " + field.getName() + " on the new Order");
			assertEquals(field.get(original), copyValue, "Order." + methodName + " should set " + field.getName() + " on the new Order");
		}
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldNotDiscontinueGivenOrderWithoutDates() throws Exception {
		assertFalse(o.isDiscontinued(ymd.parse("2007-10-26")), "order without dates shouldn't be discontinued");
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldNotDiscontinueBeforeActivationDate() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));

		assertFalse(o.isDiscontinued(ymd.parse("2006-10-26")), "shouldn't be discontinued before date activated");
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldNotDiscontinueGivenActivatedOrderWithoutEndDate() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));

		assertFalse(o.isDiscontinued(ymd.parse("2007-10-26")), "order without no end dates shouldn't be discontinued");
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldNotDiscontinueActivatedOrderBeforeAutoExpireDate() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));
		o.setAutoExpireDate(ymd.parse("2007-12-31"));

		assertFalse(o.isDiscontinued(ymd.parse("2007-10-26")), "shouldn't be discontinued before autoExpireDate");
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldNotDiscontinueActivatedOrderAfterAutoExpireDate() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));
		o.setAutoExpireDate(ymd.parse("2007-12-31"));

		assertFalse(o.isDiscontinued(ymd.parse("2008-10-26")), "shouldn't be discontinued after autoExpireDate");
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldNotDiscontinueActivatedOrderBeforeDateStopped() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));

		assertFalse(o.isDiscontinued(ymd.parse("2007-10-26")), "shouldn't be discontinued before dateStopped");
	}

	/**
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldDiscontinueActivatedOrderAfterDateStopped() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));

		assertTrue(o.isDiscontinued(ymd.parse("2007-11-26")), "should be discontinued after dateStopped");
	}

	/**
	 * @see Order#isActive()
	 */
	@Test
	public void isActive_shouldBeCurrentAfterDateActivated() throws Exception {
		//assertTrue("dateActivated==null && no end date should always be current", o.isActive(ymd.parse("2007-10-26")));
		o.setDateActivated(ymd.parse("2007-01-01"));

		assertFalse(o.isActive(ymd.parse("2006-10-26")), "shouldn't be current before dateActivated");
		assertTrue(o.isActive(ymd.parse("2007-10-26")), "should be current after dateActivated");
	}

	/**
	 * @see Order#isActive()
	 */
	@Test
	public void isActive_shouldBeCurrentBetweenDateActivatedAndAutoExpireDate() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));
		o.setAutoExpireDate(ymd.parse("2007-12-31"));

		assertFalse(o.isActive(ymd.parse("2006-10-26")), "shouldn't be current before dateActivated");
		assertTrue(o.isActive(ymd.parse("2007-10-26")), "should be current between dateActivated and autoExpireDate");
		assertFalse(o.isActive(ymd.parse("2008-10-26")), "shouldn't be current after autoExpireDate");
	}

	/**
	 * @see Order#isActive()
	 */
	@Test
	public void isActive_shouldBeCurrentBetweenDateActivatedAndDateStopped() throws Exception {
		o.setDateActivated(ymd.parse("2007-01-01"));
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));

		assertFalse(o.isActive(ymd.parse("2006-10-26")), "shouldn't be current before dateActivated");
		assertTrue(o.isActive(ymd.parse("2007-10-26")), "should be current between dateActivated and dateStopped");
		assertFalse(o.isActive(ymd.parse("2007-11-26")), "shouldn't be current after dateStopped");
	}

	/**
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
		
		assertEquals(anOrder, orderThatCanDiscontinueTheOrder.getPreviousOrder(), "should set previous order to anOrder");
		
		assertEquals(orderThatCanDiscontinueTheOrder.getAction(), Order.Action.DISCONTINUE, "should set new order action to new");
		
		assertEquals(anOrder.getCareSetting(), orderThatCanDiscontinueTheOrder.getCareSetting());
		
		assertEquals(anOrder.getOrderType(), orderThatCanDiscontinueTheOrder.getOrderType());
		
		assertNull(orderThatCanDiscontinueTheOrder.getOrderGroup(), "Discontinued order should not have orderGroup");
		
	}
	
	/**
	 * @see Order#copy()
	 */
	@Test
	public void copy_shouldCopyAllFields() throws Exception {
		assertThatAllFieldsAreCopied(new Order(), null);
		assertThatAllFieldsAreCopied(new TestOrder(), null);
	}
	
	/**
	 * @see Order#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetAllTheRelevantFields() throws Exception {
		Order newOrder = new Order();
		
		OrderGroup orderGroup = new OrderGroup();
		newOrder.setOrderGroup(orderGroup);
		
		Order revisedOrder = newOrder.cloneForRevision();
		
		assertThatAllFieldsAreCopied(revisedOrder, "cloneForRevision", "creator", "dateCreated", "action", "changedBy",
		    "dateChanged", "voided", "dateVoided", "voidedBy", "voidReason", "encounter", "orderNumber", "orderer",
		    "previousOrder", "dateActivated", "dateStopped", "accessionNumber");
	}
	
	/**
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
	 * @see Order#isType(OrderType)
	 */
	@Test
	public void isType_shouldFalseIfItNeitherTheSameNorASubtype() throws Exception {
		Order order = new Order();
		order.setOrderType(new OrderType());
		
		assertFalse(order.isType(new OrderType()));
	}
	
	/**
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
	 * @see Order#hasSameOrderableAs(Order)
	 */
	@Test
	public void hasSameOrderableAs_shouldReturnFalseIfOtherOrderIsNull() throws Exception {
		Order order = new Order();
		order.setConcept(new Concept());
		
		assertFalse(order.hasSameOrderableAs(null));
	}
	
	/**
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
	 * @see Order#isFuture(java.util.Date)
	 */
	@Test
	public void isFuture_shouldReturnFalseForAVoidedOrder() throws Exception {
		Order order = new Order();
		order.setVoided(true);
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
	 * @see Order#isFuture(java.util.Date)
	 */
	@Test
	public void isFuture_shouldReturnFalseIfDateActivatedIsNull() throws Exception {
		Order order = new Order();
		assertNull(order.getDateActivated());
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
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
	 * @see Order#isDiscontinued(java.util.Date)
	 */
	@Test
	public void isDiscontinued_shouldFailIfDateStoppedIsAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		APIException exception = assertThrows(APIException.class, () -> order.isDiscontinued(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT)));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("Order.error.invalidDateStoppedAndAutoExpireDate")));
	}
	
	/**
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
	 * @see Order#isDiscontinued(Date)
	 */
	@Test
	public void isDiscontinued_shouldReturnTrueIfTheOrderIsScheduledForTheFutureAndActivatedOnCheckDateButTheCheckDateIsAfterDateStopped()
	        throws Exception {
		// tests the case when a scheduled order is revised:
		// in that case its original order is stopped.
		// the stopped date of the original order is set to a moment before the activated date of the revised order.
		// the order here represents such an original order
		Order order = new Order();
		order.setUrgency(Urgency.ON_SCHEDULED_DATE);
		Date today = new Date();
		Date scheduledDateInFuture = DateUtils.addMonths(today, 2);
		order.setScheduledDate(scheduledDateInFuture);
		Date activationDate = DateUtils.addDays(today, -2);
		order.setDateActivated(activationDate);
		Date stopDate = new Date();
		OrderUtilTest.setDateStopped(order, stopDate);
		assertNotNull(order.getDateActivated());
		assertNotNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertTrue(order.isDiscontinued(DateUtils.addSeconds(stopDate, 1)));
	}
	
	/**
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
	 * @see Order#isExpired(java.util.Date)
	 */
	@Test
	public void isExpired_shouldFailIfDateStoppedIsAfterAutoExpireDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		order.setAutoExpireDate(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT));
		OrderUtilTest.setDateStopped(order, DateUtils.parseDate("2014-11-01 11:11:12", DATE_FORMAT));
		APIException exception = assertThrows(APIException.class, () -> order.isExpired(DateUtils.parseDate("2014-11-01 11:11:13", DATE_FORMAT)));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("Order.error.invalidDateStoppedAndAutoExpireDate")));
	}
	
	/**
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
	 * @see Order#isActivated(Date)
	 */
	@Test
	public void isActivated_shouldReturnTrueIfAnOrderWasActivatedOnTheCheckDate() throws Exception {
		Order order = new Order();
		Date activationDate = DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT);
		order.setDateActivated(activationDate);
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertTrue(order.isActivated(activationDate));
	}
	
	/**
	 * @see Order#isActivated(Date)
	 */
	@Test
	public void isActivated_shouldReturnTrueIfAnOrderWasActivatedBeforeTheCheckDate() throws Exception {
		Order order = new Order();
		Date activationDate = DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT);
		order.setDateActivated(activationDate);
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertTrue(order.isActivated(DateUtils.addMonths(activationDate, 2)));
	}
	
	/**
	 * @see Order#isActivated(Date)
	 */
	@Test
	public void isActivated_shouldReturnFalseIfDateActivatedIsNull() throws Exception {
		Order order = new Order();
		assertNull(order.getDateActivated());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT)));
	}
	
	/**
	 * @see Order#isActivated(Date)
	 */
	@Test
	public void isActivated_shouldReturnFalseForAnOrderActivatedAfterTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertFalse(order.isActivated(DateUtils.addMonths(order.getDateActivated(), -2)));
	}
	
	/**
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
	 * @see Order#isActive(Date)
	 */
	@Test
	public void isActive_shouldReturnTrueIfAnOrderWasActivatedOnTheCheckDateButScheduledForTheFuture() throws Exception {
		Order order = new Order();
		order.setUrgency(Urgency.ON_SCHEDULED_DATE);
		Date scheduledDateInFuture = DateUtils.addMonths(new Date(), 2);
		order.setScheduledDate(scheduledDateInFuture);
		Date activationDate = DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT);
		order.setDateActivated(activationDate);
		assertNull(order.getDateStopped());
		assertNull(order.getAutoExpireDate());
		assertTrue(order.isActive(activationDate));
	}
	
	/**
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
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnFalseIfDateActivatedIsNull() throws Exception {
		Order order = new Order();
		assertNull(order.getDateActivated());
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
	/**
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnFalseIfTheOrderIsNotYetActivatedAsOfTheCheckDate() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertFalse(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:09", DATE_FORMAT)));
	}
	
	/**
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
	 * @see Order#isStarted(java.util.Date)
	 */
	@Test
	public void isStarted_shouldReturnTrueIfTheOrderIsStartedAndNotScheduled() throws Exception {
		Order order = new Order();
		order.setDateActivated(DateUtils.parseDate("2014-11-01 11:11:10", DATE_FORMAT));
		assertTrue(order.isStarted(DateUtils.parseDate("2014-11-01 11:11:11", DATE_FORMAT)));
	}
	
}
