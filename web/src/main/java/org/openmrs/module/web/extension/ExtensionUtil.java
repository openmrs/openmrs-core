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
package org.openmrs.module.web.extension;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Form;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.FormEntryContext;

/**
 * Facilitates processing extensions.
 */
public class ExtensionUtil {
	
	/**
	 * Searches for all modules implementing {@link AddEncounterToVisitExtension} and returns the
	 * set of supported forms.
	 * 
	 * @return the set of Forms
	 */
	public Set<Form> getFormsModulesCanAddEncounterToVisit(FormEntryContext context) {
		List<Extension> extensions = ModuleFactory
		        .getExtensions("org.openmrs.module.web.extension.AddEncounterToVisitExtension");
		
		if (extensions == null) {
			return Collections.emptySet();
		}
		
		Set<Form> forms = new TreeSet<Form>(new Comparator<Form>() {
			
			@Override
			public int compare(Form o1, Form o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for (Extension extension : extensions) {
			AddEncounterToVisitExtension ext = (AddEncounterToVisitExtension) extension;
			
			Set<Form> tmpForms = ext.getFormsModuleCanAddEncounterToVisit(context);
			if (tmpForms != null) {
				forms.addAll(tmpForms);
			}
		}
		
		return forms;
	}
}
