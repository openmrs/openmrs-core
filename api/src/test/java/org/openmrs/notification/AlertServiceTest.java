package org.openmrs.notification;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.impl.AlertServiceImpl;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class AlertServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link AlertService#notifySuperUsers(String,Exception,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should add an alert to the database", method = "notifySuperUsers(String,Exception,null)")
	public void notifySuperUsers_shouldAddAnAlertToTheDatabase() throws Exception {
		// Check there are no alerts before the method is called
		Assert.assertEquals(0, Context.getAlertService().getAlertsByUser(null).size());
		
		//Call the method to be tested
		AlertServiceImpl alert = new AlertServiceImpl();
		alert.notifySuperUsers("Module.startupError.notification.message", null, "test");
		
		// Check that there is exactly one alert after the message is called
		Assert.assertEquals(1, Context.getAlertService().getAlertsByUser(null).size());
		
		// Set alertOne to be that one alert
		Alert alertOne = Context.getAlertService().getAlert(1);
		
		//Test that alert contains the expected content
		Assert.assertTrue(alertOne.getText().equals("Module.startupError.notification.message"));
	}
}
