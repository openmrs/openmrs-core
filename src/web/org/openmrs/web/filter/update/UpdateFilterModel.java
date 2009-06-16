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
package org.openmrs.web.filter.update;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.DatabaseUpdater.OpenMRSChangeSet;
import org.openmrs.web.filter.StartupFilter;

/**
 * The {@link UpdateFilter} uses this model object to hold all properties that are edited by the
 * user in the wizard. All attributes on this model object are added to all templates rendered by
 * the {@link StartupFilter}.
 */
public class UpdateFilterModel {
	
	// automatically given to the .vm files and used there
	public String headerTemplate = "org/openmrs/web/filter/update/header.vm";
	
	// automatically given to the .vm files and used there
	public String footerTemplate = "org/openmrs/web/filter/update/footer.vm";
	
	public List<OpenMRSChangeSet> changes = null;
	
	public String superuserrole = OpenmrsConstants.SUPERUSER_ROLE;
	
	/**
	 * Default constructor that sets up some of the properties
	 */
	public UpdateFilterModel() {
		updateChanges();
	}
	
	/**
	 * Convenience method that reads from liquibase again to get the most recent list of changesets
	 * that still need to be run.
	 */
	public void updateChanges() {
		Log log = LogFactory.getLog(getClass());
		
		try {
			changes = DatabaseUpdater.getUnrunDatabaseChanges();
		}
		catch (Exception e) {
			log.error("Unable to get the database changes", e);
		}
	}
	
}
