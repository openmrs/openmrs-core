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

import org.hibernate.search.annotations.DocumentId;

/**
 * ConceptClass
 */
public class ConceptClass extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 33473L;
	
	//UUIDs for core concept classes
	
	public static final String TEST_UUID = "8d4907b2-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String PROCEDURE_UUID = "8d490bf4-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String DRUG_UUID = "8d490dfc-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String DIAGNOSIS_UUID = "8d4918b0-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String FINDING_UUID = "8d491a9a-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String ANATOMY_UUID = "8d491c7a-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String QUESTION_UUID = "8d491e50-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String LABSET_UUID = "8d492026-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String MEDSET_UUID = "8d4923b4-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String CONVSET_UUID = "8d492594-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String MISC_UUID = "8d492774-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String SYMPTOM_UUID = "8d492954-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String SYMPTOM_FINDING_UUID = "8d492b2a-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String SPECIMEN_UUID = "8d492d0a-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String MISC_ORDER_UUID = "8d492ee0-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String ORDER_SET_UUID = "baa7a1b8-a1ba-11e0-9616-705ab6a580e0";
	
	public static final String FREQUENCY_UUID = "8e071bfe-520c-44c0-a89b-538e9129b42a";
	
	// Fields
	@DocumentId
	private Integer conceptClassId;
	
	// Constructors
	
	/** default constructor */
	public ConceptClass() {
	}
	
	/** constructor with id */
	public ConceptClass(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}
	
	// Property accessors
	
	public Integer getConceptClassId() {
		return this.conceptClassId;
	}
	
	public void setConceptClassId(Integer conceptClassId) {
		this.conceptClassId = conceptClassId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptClassId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptClassId(id);
		
	}
	
}
