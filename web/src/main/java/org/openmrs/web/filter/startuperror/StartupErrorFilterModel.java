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
package org.openmrs.web.filter.startuperror;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.update.UpdateFilter;

/**
 * The {@link UpdateFilter} uses this model object to hold all properties that are edited by the
 * user in the wizard. All attributes on this model object are added to all templates rendered by
 * the {@link StartupFilter}.
 */
public class StartupErrorFilterModel {
	
	// automatically given to the .vm files and used there
	public String headerTemplate = "org/openmrs/web/filter/startuperror/header.vm";
	
	// automatically given to the .vm files and used there
	public String footerTemplate = "org/openmrs/web/filter/startuperror/footer.vm";
	
	public Throwable errorAtStartup = null;
	
	public String stacktrace = null;
	
	/**
	 * Default constructor that sets up some of the properties
	 */
	public StartupErrorFilterModel(Throwable t) {
		errorAtStartup = t;
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		stacktrace = sw.toString();
	}
	
}
