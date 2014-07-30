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
package org.openmrs;

import org.springframework.validation.Errors;

import java.util.Locale;

/**
 * DosingInstructions required for different types of DosingTypes can
 * be modelled using an implementation of this interface. Any class which
 * implements this interface should have a default constructor.
 * @since 1.10
 */
public interface DosingInstructions {
	
	/**
	 * Get human-readable version of dosing instructions for a particular locale
	 * All dosing instructions can be localized, so the result, especially
	 * any free text may remain in the original language.  In general, it's expect that
	 * most implementations will write orders in a single language and then want to
	 * translate instructions to the patient's preferred language when printing orders
	 * for the patient.  In all other cases, it will want to call this method with
	 * the user's locale (i.e., <tt>context.getLocale()</tt>).
	 * @return localized drug instructions string
	 */
	public String getDosingInstructionsAsString(Locale locale);
	
	/**
	 * Serialize dosing instructions into order
	 * @param order DrugOrder to set dosing instructions
	 */
	public void setDosingInstructions(DrugOrder order);
	
	/**
	 * Get dosing instructions from order
	 * @param order DrugOrder to get dosing instructions
	 * @return DosingInstructions created from DrugOrder
	 * @throws Exception if dosing type of passing order is not matched with dosing type of implementing dosing instruction
	 */
	public DosingInstructions getDosingInstructions(DrugOrder order);
	
	public void validate(DrugOrder order, Errors errors);
}
