package org.openmrs.logic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class DurationTest extends BaseContextSensitiveTest {

	@Test
	public void getDurationInDays_shouldReturnTimeConvertedToCorrespondingUnits(){
		
		Duration days = Duration.days(30);
		Assert.assertEquals(30D, days.getDurationInDays(), 0D);
		
		Duration minutes = Duration.minutes(30);
		Assert.assertEquals(30/1440D,minutes.getDurationInDays(),0D);
		
		Duration seconds = Duration.seconds(30);
		Assert.assertEquals(30/86400D,seconds.getDurationInDays(),0D);
		
		Duration hours = Duration.hours(30);
		Assert.assertEquals(30/24D,hours.getDurationInDays(),0D);
		
		Duration weeks = Duration.weeks(30);
		Assert.assertEquals(30*7D,weeks.getDurationInDays(),0D);
		
		Duration months = Duration.months(30);
		Assert.assertEquals(30*30D,months.getDurationInDays(),0D);
		
		Duration years = Duration.years(3);
		Assert.assertEquals(3*365D,years.getDurationInDays(),0D);
		
	}
}
