/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.notification.db;

import java.util.List;

import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.notification.Alert;

/**
 * Database methods for the AlertService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.notification.AlertService
 */
public interface AlertDAO {
	
	/**
	 * @see org.openmrs.notification.AlertService#saveAlert(org.openmrs.notification.Alert)
	 */
	public Alert saveAlert(Alert alert) throws DAOException;
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlert(Integer)
	 */
	public Alert getAlert(Integer alertId) throws DAOException;
	
	/**
	 * @see org.openmrs.notification.AlertService#getAlerts(org.openmrs.User, boolean, boolean)
	 */
	public List<Alert> getAlerts(User user, boolean includeRead, boolean includeVoided) throws DAOException;
	
	/**
	 * @see org.openmrs.notification.AlertService#purgeAlert(org.openmrs.notification.Alert)
	 */
	public void deleteAlert(Alert alert) throws DAOException;
	
	/**
	 * @see org.openmrs.notification.AlertService#getAllAlerts(boolean)
	 */
	public List<Alert> getAllAlerts(boolean includeExpired);
	
}
