/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jfree.chart.JFreeChart;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests for the {@link ShowGraphServlet} class.
 */
public class ShowGraphServletTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ShowGraphServlet#getChart(HttpServletRequest)}
	 */
	@Test
	@Verifies(value = "should set value axis label to given units", method = "getChart(HttpServletRequest)")
	public void getChart_shouldSetValueAxisLabelToGivenUnits() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", "7");
		request.setParameter("conceptId", "5497"); // cd4
		request.setParameter("units", "CUSTOM UNITS");
		
		JFreeChart chart = new ShowGraphServlet().getChart(request);
		
		Assert.assertEquals("CUSTOM UNITS", chart.getXYPlot().getRangeAxis().getLabel());
	}
	
	/**
	 * @see {@link ShowGraphServlet#getChart(HttpServletRequest)}
	 */
	@Test
	@Verifies(value = "should set value axis label to concept numeric units if given units is null", method = "getChart(HttpServletRequest)")
	public void getChart_shouldSetValueAxisLabelToConceptNumericUnitsIfGivenUnitsIsNull() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patientId", "7");
		request.setParameter("conceptId", "5497"); // cd4
		
		JFreeChart chart = new ShowGraphServlet().getChart(request);
		
		Assert.assertEquals("cells/mmL", chart.getXYPlot().getRangeAxis().getLabel());
	}
	
	/**
	 * @see {@link ShowGraphServlet#getFromDate(String)}
	 */
	@Test
	@Verifies(value = "should return one year previous to today if parameter is null", method = "getFromDate(String)")
	public void getFromDate_shouldReturnOneYearPreviousToTodayIfParameterIsNull() throws Exception {
		Calendar lastYear = Calendar.getInstance();
		lastYear.set(lastYear.get(Calendar.YEAR) - 1, lastYear.get(Calendar.MONTH), lastYear.get(Calendar.DAY_OF_MONTH), 0,
		    0, 0);
		
		Date fromDate = new ShowGraphServlet().getFromDate(null);
		
		Assert.assertEquals(lastYear.getTimeInMillis(), fromDate.getTime(), 1000);
	}
	
	/**
	 * @see {@link ShowGraphServlet#getFromDate(String)}
	 */
	@Test
	@Verifies(value = "should return same date as given string parameter", method = "getFromDate(String)")
	public void getFromDate_shouldReturnSameDateAsGivenStringParameter() throws Exception {
		Long time = new Date().getTime() - 100000;
		Date fromDate = new ShowGraphServlet().getFromDate(Long.toString(time));
		Assert.assertEquals(time.longValue(), fromDate.getTime());
	}
	
	/**
	 * @see {@link ShowGraphServlet#getToDate(String)}
	 */
	@Test
	@Verifies(value = "should return next months date if parameter is null", method = "getToDate(String)")
	public void getToDate_shouldReturnNextMonthsDateIfParameterIsNull() throws Exception {
		Calendar nxtMonth = Calendar.getInstance();
		nxtMonth.set(nxtMonth.get(Calendar.YEAR), nxtMonth.get(Calendar.MONTH), nxtMonth.get(Calendar.DAY_OF_MONTH) + 1, 0,
		    0, 0);
		
		Date toDate = new ShowGraphServlet().getToDate(null);
		
		Assert.assertEquals(nxtMonth.getTimeInMillis(), toDate.getTime(), 1000);
	}
	
	/**
	 * @see {@link ShowGraphServlet#getToDate(String)}
	 */
	@Test
	@Verifies(value = "should return date one day after given string date", method = "getToDate(String)")
	public void getToDate_shouldReturnDateOneDayAfterGivenStringDate() throws Exception {
		Long time = new Date().getTime() - 100000;
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTimeInMillis(time);
		timeCal
		        .set(timeCal.get(Calendar.YEAR), timeCal.get(Calendar.MONTH), timeCal.get(Calendar.DAY_OF_MONTH) + 1, 0, 0,
		            0);
		
		Date toDate = new ShowGraphServlet().getToDate(Long.toString(time));
		
		Assert.assertEquals(timeCal.getTimeInMillis(), toDate.getTime());
	}
	
	/**
	 * @see {@link ShowGraphServlet#getToDate(String)}
	 */
	@Test
	@Verifies(value = "should set hour minute and second to zero", method = "getToDate(String)")
	public void getToDate_shouldSetHourMinuteAndSecondToZero() throws Exception {
		Date toDate = new ShowGraphServlet().getToDate(Long.toString(new Date().getTime()));
		Assert.assertEquals(0, toDate.getHours());
		Assert.assertEquals(0, toDate.getMinutes());
		Assert.assertEquals(0, toDate.getSeconds());
	}
	
}
