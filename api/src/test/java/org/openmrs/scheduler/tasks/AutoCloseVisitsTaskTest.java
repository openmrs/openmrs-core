package org.openmrs.scheduler.tasks;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.util.Clock;
import org.openmrs.util.OpenmrsConstants;

import java.text.ParseException;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AutoCloseVisitsTaskTest {
	
	@Mock
	private AdministrationService administrationService;
	
	@Mock
	private VisitService visitService;
	
	@Mock
	private Clock clock;
	
	private AutoCloseVisitsTask autoCloseVisitsTask;
	
	@Before
	public void setUp() {
		initMocks(this);
		autoCloseVisitsTask = new AutoCloseVisitsTask(administrationService, visitService, clock);
	}
	
	@Test
	public void shouldCloseAllVisitsBeforeConfiguredNumberOfDaysWhenGlobalPropertyIsSet() throws Exception {
		when(administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS))
		        .thenReturn("2");
		when(clock.getCurrentTime()).thenReturn(getDate("2014-02-11"));
		
		autoCloseVisitsTask.execute();
		
		verifyStopsVisitsStartedBeforeDate("2014-02-09");
	}
	
	@Test
	public void shouldCloseAllVisitsBeforeCurrentTimeWhenGlobalPropertyIsNotSet() throws Exception {
		when(administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS))
		        .thenReturn("");
		when(clock.getCurrentTime()).thenReturn(getDate("2014-02-11"));
		
		autoCloseVisitsTask.execute();
		
		verifyStopsVisitsStartedBeforeDate("2014-02-11");
	}
	
	@Test
	public void shouldCloseAllVisitsBeforeCurrentTimeWhenGlobalPropertyIsSetToZero() throws Exception {
		when(administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS))
		        .thenReturn("0");
		when(clock.getCurrentTime()).thenReturn(getDate("2014-02-11"));
		
		autoCloseVisitsTask.execute();
		
		verifyStopsVisitsStartedBeforeDate("2014-02-11");
	}
	
	@Test
	public void shouldCloseAllVisitsBeforeCurrentTimeWhenGlobalPropertyIsSetToNonIntegerValue() throws Exception {
		when(administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS))
		        .thenReturn("foo");
		when(clock.getCurrentTime()).thenReturn(getDate("2014-02-11"));
		
		autoCloseVisitsTask.execute();
		
		verifyStopsVisitsStartedBeforeDate("2014-02-11");
	}
	
	private void verifyStopsVisitsStartedBeforeDate(String dateString) throws ParseException {
		Date expectedMaximumVisitStartDate = getDate(dateString);
		verify(visitService).stopVisits(expectedMaximumVisitStartDate);
	}
	
	private Date getDate(String str) throws ParseException {
		return DateUtils.parseDate(str, "yyyy-mm-dd");
	}
}
