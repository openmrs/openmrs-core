/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs;

/**
 * The ConditionClinicalStatus is what we've historically called "condition status" and what FHIR refers to as
 * ConditionClinicalStatus . This is a clinical condition that has risen to a level of concern.
 * The custom status and subset of FHIR statuses are defined as follows:
 * <li>{@link #ACTIVE}</li>
 * <li>{@link #INACTIVE}</li>
 * <li>{@link #HISTORY_OF}</li>
 *
 * @since 2.2
 * */
public enum ConditionClinicalStatus {
	/**
	 * This is where the patient is currently experiencing the symptoms of the condition or there is evidence of the 
	 * condition.
	 * */
	ACTIVE,
	
	/**
	 * There is where the patient is no longer experiencing the symptoms of the condition or there is no longer 
	 * evidence of the condition.
	 * */
	INACTIVE,
	
	/**
	 * This maps most closely to the "remission" status in FHIR, but we want to be more clear about
	 * the common OpenMRS use case.
	 * Remission is where the patient is no longer experiencing the symptoms of the condition, 
	 * but there is a risk of the symptoms returning.
	 * 
	 * @deprecated as of 2.6.0
	 * */
	HISTORY_OF,
	
	/**
	 * The Patient is experiencing a re-occurrence or repeating of a previously resolved condition, 
	 * e.g. urinary tract infection, pancreatitis, cholangitis, conjunctivitis.
	 */
	RECURRENCE,
	
	/**
	 * The Patient is experiencing a return of a condition, or signs and symptoms after a period of 
	 * improvement or remission, e.g. relapse of cancer, multiple sclerosis, rheumatoid arthritis, 
	 * systemic lupus erythematosus, bipolar disorder, [psychotic relapse of] schizophrenia, etc.
	 */
	RELAPSE,
	
	/**
	 * The Patient is no longer experiencing the symptoms of the condition, but there is a risk 
	 * of the symptoms returning.
	 */
	REMISSION,

	/**
	 * The Patient is no longer experiencing the symptoms of the condition and there is a 
	 * negligible perceived risk of the symptoms returning.
	 */
	RESOLVED
	
}
