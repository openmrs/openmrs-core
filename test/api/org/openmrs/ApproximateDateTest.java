package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

public class ApproximateDateTest {

	/**
	 * @see {@link ApproximateDate#setDate(int,int,int,null,null,null)}
	 */
	@Test
	@Verifies(value = "should properly set the date", method = "setDate(int,int,int,null,null,null)")
	public void setDate_shouldProperlySetTheDate() throws Exception {
		//TODO auto-generated
		ApproximateDate approximateDate = new ApproximateDate();
		approximateDate.setDate(1988, 9, 15, false, false, false);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(1988, 8, 15, 0, 0, 0);
		Assert.assertEquals(cal.getTime(), approximateDate.getDate());
	}

	/**
	 * @see {@link ApproximateDate#setDateFromAge(float)}
	 */
	@Test
	@Verifies(value = "should set the date depending on the age", method = "setDateFromAge(float)")
	public void setDateFromAge_shouldSetTheDateDependingOnTheAge() throws Exception {
		//TODO auto-generated
		ApproximateDate approximateDate = new ApproximateDate();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		// Test standard case and ensure estimated field is set to true
		approximateDate.setDateFromAge(10f, df.parse("2008-05-20"));
		assertEquals(df.parse("1998-05-20"), approximateDate.getDate());
		assertTrue(approximateDate.isApproximated());
		
		// Test boundary cases
		approximateDate.setDateFromAge(52, df.parse("2002-01-01"));
		assertEquals(df.parse("1950-01-01"), approximateDate.getDate());
		approximateDate.setDateFromAge(35, df.parse("2004-12-31"));
		assertEquals(df.parse("1969-12-31"), approximateDate.getDate());
		approximateDate.setDateFromAge(0, df.parse("2008-05-20"));
		assertEquals(df.parse("2008-05-20"), approximateDate.getDate());

	}

	/**
	 * @see {@link ApproximateDate#getDate()}
	 */
	@Test
	@Verifies(value = "should return properly estimated dates", method = "getDate()")
	public void getDate_shouldReturnProperlyEstimatedDates() throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see {@link ApproximateDate#setYear(Integer,null)}
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
	 */
	@Test
	@Verifies(value = "should set the approximate value for month", method = "setMonth(Integer,null)")
	public void setMonth_shouldSetTheApproximateValueForMonth() throws Exception {
		ApproximateDate aDate = new ApproximateDate();
		aDate.setMonth(12, false);
		Assert.assertEquals(new Boolean(false), aDate.isMonthApproximated());
	}

	/**
     * @see {@link ApproximateDate#compareTo(ApproximateDate)}
     * 
     */
    @Test
    @Verifies(value = "should compare two ApproximateDates", method = "compareTo(ApproximateDate)")
    public void compareTo_shouldCompareTwoApproximateDates() throws Exception {
	    //TODO auto-generated
		Assert.fail("Not yet implemented");
    }

	/**
     * @see {@link ApproximateDate#setApproximated(int)}
     * 
     */
    @Test
    @Verifies(value = "should set the approximation level", method = "setApproximated(int)")
    public void setApproximated_shouldSetTheApproximationLevel() throws Exception {
	    //TODO auto-generated
		Assert.fail("Not yet implemented");
    }

	/**
     * @see {@link ApproximateDate#setDateFromAge(float,Date)}
     * 
     */
    @Test
    @Verifies(value = "should set the date depending on the age and a date", method = "setDateFromAge(float,Date)")
    public void setDateFromAge_shouldSetTheDateDependingOnTheAgeAndADate() throws Exception {
	    //TODO auto-generated
		Assert.fail("Not yet implemented");
    }

	/**
     * @see {@link ApproximateDate#setDay(Integer)}
     * 
     */
    @Test
    @Verifies(value = "should set the day", method = "setDay(Integer)")
    public void setDay_shouldSetTheDay() throws Exception {
	    //TODO auto-generated
		ApproximateDate date = new ApproximateDate();
		date.setDay(12);
		Assert.assertTrue(date.getDay().equals(12));
    }

	/**
     * @see {@link ApproximateDate#setMonth(Integer)}
     * 
     */
    @Test
    @Verifies(value = "should set the month", method = "setMonth(Integer)")
    public void setMonth_shouldSetTheMonth() throws Exception {
	    //TODO auto-generated
		ApproximateDate date = new ApproximateDate();
		date.setMonth(12);
		Assert.assertTrue(date.getMonth().equals(12));
    }

	/**
     * @see {@link ApproximateDate#setYear(Integer)}
     * 
     */
    @Test
    @Verifies(value = "should set the year", method = "setYear(Integer)")
    public void setYear_shouldSetTheYear() throws Exception {
	    //TODO auto-generated
		ApproximateDate date = new ApproximateDate();
		date.setYear(1999);
		Assert.assertTrue(date.getDay().equals(1999));
    }

	/**
     * @see {@link ApproximateDate#isApproximated()}
     * 
     */
    @Test
    @Verifies(value = "should check whether the date is approximated", method = "isApproximated()")
    public void isApproximated_shouldCheckWhetherTheDateIsApproximated() throws Exception {
	    //TODO auto-generated
		Assert.fail("Not yet implemented");
    }
}
