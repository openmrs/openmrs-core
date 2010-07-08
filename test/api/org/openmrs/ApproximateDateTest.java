package org.openmrs;


import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

public class ApproximateDateTest {

	/**
	 * @see {@link ApproximateDate#setDate(int,int,int,null,null,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should properly set the date", method = "setDate(int,int,int,null,null,null)")
	public void setDate_shouldProperlySetTheDate() throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see {@link ApproximateDate#setDateFromAge(float)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the date depending on the age", method = "setDateFromAge(float)")
	public void setDateFromAge_shouldSetTheDateDependingOnTheAge()
			throws Exception {
		//TODO auto-generated
		ApproximateDate approximateDate = new ApproximateDate();
		approximateDate.setDateFromAge(18f);
		Date set = approximateDate.getDate();
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see {@link ApproximateDate#getDate()}
	 * 
	 */
	@Test
	@Verifies(value = "should return properly estimated dates", method = "getDate()")
	public void getDate_shouldReturnProperlyEstimatedDates() throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see {@link ApproximateDate#setYear(Integer,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the year value", method = "setYear(Integer,null)")
	public void setYear_shouldSetTheYearValue() throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setYear(2010, false);
		Assert.assertEquals(new Integer(2010), aDate.getYear());
	}

	/**
	 * @see {@link ApproximateDate#setYear(Integer,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the approximate value for year", method = "setYear(Integer,null)")
	public void setYear_shouldSetTheApproximateValueForYear() throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setYear(2010, true);
		Assert.assertEquals(new Boolean(true), aDate.isYearApproximated());
	}

	/**
	 * @see {@link ApproximateDate#setDay(Integer,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the day value", method = "setDay(Integer,null)")
	public void setDay_shouldSetTheDayValue() throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setDay(10, false);
		Assert.assertEquals(new Integer(10), aDate.getDay());
	}

	/**
	 * @see {@link ApproximateDate#setDay(Integer,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the approximate value for day", method = "setDay(Integer,null)")
	public void setDay_shouldSetTheApproximateValueForDay() throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setDay(12, true);
		Assert.assertEquals(new Boolean(true), aDate.isDayApproximated());
	}

	/**
	 * @see {@link ApproximateDate#setMonth(Integer,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the month value", method = "setMonth(Integer,null)")
	public void setMonth_shouldSetTheMonthValue() throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setMonth(2, false);
		Assert.assertEquals(new Integer(2), aDate.getMonth());
	}

	/**
	 * @see {@link ApproximateDate#setMonth(Integer,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should set the approximate value for month", method = "setMonth(Integer,null)")
	public void setMonth_shouldSetTheApproximateValueForMonth()
			throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setMonth(12, false);
		Assert.assertEquals(new Boolean(false), aDate.isMonthApproximated());
	}
}