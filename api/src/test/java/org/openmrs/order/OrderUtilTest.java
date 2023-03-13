/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.Order;
import org.openmrs.OrderType;

/**
 * Contains test for OrderUtil
 */
public class OrderUtilTest {
	
	public static boolean isActiveOrder(Order order, Date asOfDate) {
		return order.isActive(asOfDate) && order.getAction() != Order.Action.DISCONTINUE;
	}
	
	public static void setDateStopped(Order targetOrder, Date dateStopped) {
		try {
			Field field = null;
			Boolean isAccessible = null;
			try {
				field = Order.class.getDeclaredField("dateStopped");
				isAccessible = field.isAccessible();
				if (!isAccessible) {
					field.setAccessible(true);
				}
				field.set(targetOrder, dateStopped);
			}
			finally {
				if (field != null && isAccessible != null) {
					field.setAccessible(isAccessible);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldTrueIfOrderType2IsTheSameOrIsASubtypeOfOrderType1() {
		OrderType orderType = new OrderType();
		OrderType subType1 = new OrderType();
		OrderType subType2 = new OrderType();
		subType2.setParent(subType1);
		subType1.setParent(orderType);
		assertTrue(OrderUtil.isType(subType2, subType2));
		assertTrue(OrderUtil.isType(subType1, subType2));
		assertTrue(OrderUtil.isType(orderType, subType2));
	}
	
	/**
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldReturnFalseIfTheyAreBothNull() {
		assertFalse(OrderUtil.isType(null, null));
	}
	
	/**
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldReturnFalseIfAnyIsNullAndTheOtherIsNot() {
		assertFalse(OrderUtil.isType(new OrderType(), null));
		assertFalse(OrderUtil.isType(null, new OrderType()));
	}
	
	/**
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldFalseIfOrderType2IsNeitherTheSameNorASubtypeOfOrderType1() {
		assertFalse(OrderUtil.isType(new OrderType(), new OrderType()));
	}
	
	/**
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1AndOrder2DoNotHaveEndDate() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setScheduledDate(DateUtils.addDays(date, 4)); //Order1 scheduled after 4 days without stop date
		order1.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		Order order2 = new Order();
		order2.setDateActivated(date);
		order2.setScheduledDate(DateUtils.addDays(date, 6)); //Order2 scheduled after 6 days without stop date
		order2.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1)); //Checks vice versa
	}
	
	/**
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1AndOrder2HaveSameStartDates() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setDateActivated(date);
		order1.setScheduledDate(DateUtils.addDays(date, 6)); //Order1 scheduled after 6 days
		order1.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		Order order2 = new Order();
		order2.setDateActivated(date);
		order2.setScheduledDate(DateUtils.addDays(date, 6)); //Order2 also scheduled after 6 days
		order2.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order2));
	}
	
	/**
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnFalseIfOrder1EndsBeforeOrder2Starts() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setDateActivated(date);
		order1.setAutoExpireDate(DateUtils.addDays(date, 2)); //Order1 getting expired after 2 days
		
		Order order2 = new Order();
		order2.setDateActivated(date);
		order2.setScheduledDate(DateUtils.addDays(date, 4)); //Order2 scheduled after 4 days
		order2.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		assertFalse(OrderUtil.checkScheduleOverlap(order1, order2));
		assertFalse(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnFalseIfOrder1StartsAfterOrder2() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setScheduledDate(DateUtils.addDays(date, 11)); //Order1 getting started after existing order's stop
		order1.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		Order order2 = new Order();
		order2.setDateActivated(date);
		order2.setAutoExpireDate(DateUtils.addDays(date, 2)); //Order2 expiring after 2 days
		
		assertFalse(OrderUtil.checkScheduleOverlap(order1, order2));
		assertFalse(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 *           versa
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1StopsAfterTheOrder2HasAlreadyBeenActivated() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setDateActivated(date); //Order1 scheduled today getting expired after 5 days
		order1.setAutoExpireDate(DateUtils.addDays(date, 5));
		
		Order order2 = new Order();
		order2.setDateActivated(date);
		order2.setScheduledDate(DateUtils.addDays(date, 4)); //Order2 scheduled after 4 days
		order2.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1StartsWhenTheOrder2IsActive() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setScheduledDate(DateUtils.addDays(date, 3)); //Order1 scheduled after 3 days
		order1.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		Order order2 = new Order();
		order2.setDateActivated(date); //Order2 scheduled today getting expired after 4 days
		order2.setAutoExpireDate(DateUtils.addDays(date, 4));
		
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @see OrderUtil#checkScheduleOverlap(Order,Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1StartsBeforeOrder2AndEndsAfterOrder2() {
		Date date = new Date();
		Order order1 = new Order();
		order1.setScheduledDate(DateUtils.addDays(date, 4)); //Order1 scheduled after 4 days getting expired after 14 days
		order1.setAutoExpireDate(DateUtils.addDays(date, 14));
		order1.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		Order order2 = new Order();
		order2.setDateActivated(date);
		order2.setScheduledDate(DateUtils.addDays(date, 6)); //Order2 scheduled after 6 days getting expired after 10 days
		order2.setAutoExpireDate(DateUtils.addDays(date, 10));
		order2.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @throws ParseException
	 * @see OrderUtil#checkScheduleOverlap(org.openmrs.Order, org.openmrs.Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder2StartsBeforeOrder1AndEndsAfterOrder1() throws ParseException {
		DateFormat df = new SimpleDateFormat("dd/MM/yyy");
		Order order1 = new Order();
		order1.setDateActivated(df.parse("03/08/2014"));
		order1.setAutoExpireDate(df.parse("05/08/2014"));
		Order order2 = new Order();
		order2.setDateActivated(df.parse("01/08/2014"));
		order2.setAutoExpireDate(df.parse("07/08/2014"));
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @throws ParseException
	 * @see OrderUtil#checkScheduleOverlap(org.openmrs.Order, org.openmrs.Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1StartsOnTheStopDateOfOrder2() throws ParseException {
		DateFormat df = new SimpleDateFormat("dd/MM/yyy");
		Order order1 = new Order();
		order1.setDateActivated(df.parse("13/08/2014"));
		Order order2 = new Order();
		order2.setDateActivated(df.parse("05/08/2014"));
		order2.setAutoExpireDate(order1.getDateActivated());
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
		
		//Assuming order1 has an end date
		order1.setAutoExpireDate(df.parse("15/08/2014"));
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @throws ParseException
	 * @see OrderUtil#checkScheduleOverlap(org.openmrs.Order, org.openmrs.Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfOrder1EndsOnTheStartDateOfOrder2() throws ParseException {
		DateFormat df = new SimpleDateFormat("dd/MM/yyy");
		Order order1 = new Order();
		order1.setDateActivated(df.parse("05/08/2014"));
		order1.setAutoExpireDate(df.parse("13/08/2014"));
		Order order2 = new Order();
		order2.setDateActivated(order1.getAutoExpireDate());
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
		
		//Assuming order2 has an end date
		order2.setAutoExpireDate(df.parse("15/08/2014"));
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
	
	/**
	 * @throws ParseException
	 * @see OrderUtil#checkScheduleOverlap(org.openmrs.Order, org.openmrs.Order)
	 */
	@Test
	public void checkScheduleOverlap_shouldReturnTrueIfBothOrdersStartAndEndOnSameDates() throws ParseException {
		DateFormat df = new SimpleDateFormat("dd/MM/yyy");
		Order order1 = new Order();
		order1.setDateActivated(df.parse("05/08/2014"));
		order1.setAutoExpireDate(df.parse("13/08/2014"));
		Order order2 = new Order();
		order2.setDateActivated(order1.getDateActivated());
		order2.setDateActivated(order1.getAutoExpireDate());
		assertTrue(OrderUtil.checkScheduleOverlap(order1, order2));
		assertTrue(OrderUtil.checkScheduleOverlap(order2, order1));
	}
}
