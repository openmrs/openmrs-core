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

import java.util.List;
import java.util.Set;

import org.openmrs.Form;
import org.openmrs.module.Extension;
import org.openmrs.module.web.FormEntryContext;

/**
 * <pre>
 * This class allows modules that provide form entry capabilities (like formentry, htmlformentry, and xforms) to appear in the
 * same place in the UI when the user indicates they want to enter a form, and wants to pick which form.
 * 
 * To be able to enter forms, your module should provide a subclass that overrides the {@link #getFormList(FormEntryContext))},
 * and {@link #getFormEntryUrl()} methods.
 * 
 * If your module wants to be able to take over the view or edit links in the list of existing encounters, then your subclass
 * should override the {@link #getViewFormUrl()} and {@link #getFormsModuleCanView()} methods (for viewing) and/or
 * the {@link #getEditFormUrl()} and {@link #getFormsModuleCanEdit()} methods (for editing).
 * 
 * [WARNING: Do not be surprised if this class changes substantially in OpenMRS 1.6.]
 * </pre>
 */
public abstract class FormEntryHandler extends Extension {
	
	/**
	 * @see org.openmrs.module.Extension#getMediaType()
	 */
	@Override
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * If you want your module to require any privileges before the user can enter forms with this particular module (besides
	 * "Form Entry" which is automatically required by the framework) then return it here.
	 * 
	 * @return privilege to require
	 */
	public String getRequiredPrivilege() {
		return null;
	}
	
	/**
	 * If your module allows filling out forms, override this method. 
	 * 
	 * @param formEntryContext details about the state of the user interface where the user is choosing to enter a form from 
	 * @return the list of forms provided by this module.
	 */
	public List<Form> getFormsModuleCanEnter(FormEntryContext formEntryContext) {
		return null;
	}
	
	/**
	 * If your module allows filling out forms, override this method.
	 * 
	 * @return the url to go to enter a form. (The framework will take care of adding ?personId=xyz&patientId=xyz&formId=abc to it.)
	 */
	public String getFormEntryUrl() {
		return null;
	}
	
	/**
	 * If your module wants to override the View link for encounters, override this method.
	 * 
	 * @return the url to go to to view a form. (The framework will take care of adding ?encounterId=xyz
	 */
	public String getViewFormUrl() {
		return null;
	}
	
	/**
	 * If your module wants to override the View link for encounters, override this method.
	 * 
	 * @return the forms for which this module should override the View link
	 */
	public Set<Form> getFormsModuleCanView() {
		return null;
	}
	
	/**
	 * If your module wants to override the Edit link for encounters, override this method.
	 * 
	 * @return the url to go to to edit a form. (The framework will take care of adding ?encounterId=xyz
	 */
	public String getEditFormUrl() {
		return null;
	}
	
	/**
	 * If your module wants to override the Edit link for encounters, override this method.
	 * 
	 * @return the forms for which this module should override the Edit link
	 */
	public Set<Form> getFormsModuleCanEdit() {
		return null;
	}
	
}
