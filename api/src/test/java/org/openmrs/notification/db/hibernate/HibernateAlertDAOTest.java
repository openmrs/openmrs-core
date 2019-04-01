/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.db.hibernate;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.api.context.Context;
import org.openmrs.notification.AlertService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.notification.Alert;
import org.openmrs.User;
import org.hibernate.SessionFactory;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.openmrs.api.UserService;
import org.openmrs.Role;
import java.util.Date;
import java.util.List;
/**
 * This class tests the hibernate alert data access. TODO Consider changing this and all subsequent
 * tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {
	 
	private HibernateAlertDAO hibernateAlertDAO;
	@Before
	public void runBeforeEachTest() {
		Context.openSession();
		authenticate();
		hibernateAlertDAO=new HibernateAlertDAO();
		hibernateAlertDAO.setSessionFactory((SessionFactory) applicationContext.getBean("sessionFactory"));
	}

	 /**
     * Creates and Returns alert with Id 12345
     * 
     * @return Alert object
     */
	private Alert createAlert()
	{
		Alert alert=new Alert(12345);
		User user=createUser();
		alert.setCreator(user);
		alert.setDateCreated(new Date());
		alert.setDateToExpire(new Date(System.currentTimeMillis()+60*10000));
		alert.setText("hey,This is hibernate alert dao test");
		alert.setSatisfiedByAny(true);
		alert.setAlertRead(true);
		alert.setChangedBy(user);
		alert.setDateChanged(new Date(System.currentTimeMillis()+60*1000));
		alert.addRecipient(user);
		alert.setId(12345);
		return alert;
	}
	
	 /**
     * Returns user with userId 1
     * 
     * @return User object
     */
	private User createUser()
	{
		    UserService userService=Context.getUserService();
	        User user=userService.getUser(1);
			return user;
	}
	
	 /**
     * @see HibernateAlertDAO#saveAlert
     */
	@Test
	public void saveAlert_ShouldReturnNotNull()
	{
		Alert alert=createAlert();
		User user=createUser();
		Alert save=hibernateAlertDAO.saveAlert(alert);
		assertNotNull(Integer.toString(save.getAlertId()));
	}
	
	 /**
     * @see HibernateAlertDAO#getAlert
     */
	@Test
	public void getAlert_ShouldReturnAlert() {
		Alert alert=createAlert();
		Alert save=hibernateAlertDAO.saveAlert(alert);
	    alert=hibernateAlertDAO.getAlert(12345);
	    assertEquals(alert.getText(),"hey,This is hibernate alert dao test");
	}
	
	 /**
     * @see HibernateAlertDAO#deleteAlert
     */
	@Test
	@Ignore
	public void deleteAlert_ShouldReturnNull() {
		Alert alert=createAlert();
		Alert save=hibernateAlertDAO.saveAlert(alert);
		hibernateAlertDAO.deleteAlert(save);
		assertNull(save.getId());
		
	}
	
	 /**
     * @see HibernateAlertDAO#getAllAlerts
     */
	@Test
	public void getAllAlerts_ShouldReturnNotNull() {
	    List<Alert> alerts=hibernateAlertDAO.getAllAlerts(true);
	    assertNotNull(alerts);
	}
	
	 /**
     * @see HibernateAlertDAO#getAlerts
     */
	@Test
	public void getAlerts_ShouldReturnNotNull()	{
		User user=createUser();
	    List<Alert> alert=hibernateAlertDAO.getAlerts(user,true,true);
	    assertNotNull(alert);
	}
	
}