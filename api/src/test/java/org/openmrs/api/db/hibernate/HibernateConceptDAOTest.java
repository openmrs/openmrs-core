/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAttributeType;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateConceptDAOTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/db/hibernate/include/HibernateConceptTestDataSet.xml";
	protected static final String CONCEPT_ATTRIBUTE_TYPE_XML = "org/openmrs/api/include/ConceptServiceTest-conceptAttributeType.xml";

	@Autowired
	private HibernateConceptDAO dao;
	
	@BeforeEach
	public void setUp() {
		executeDataSet(PROVIDERS_INITIAL_XML);
		
		updateSearchIndex();
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 */
	@Test
	public void getDrugs_shouldReturnDrugIf_eitherDrugNameOrConceptNameMatchesThePhaseNotBoth() {
		Concept concept = dao.getConcept(3);
		
		// concept has "COUGH SYRUP" as a concept_name and also Drug has
		// Drug_name as "COUGH" so return two distinct drugs that means search
		// either drug name or concept name match the phase
		List<Drug> drugList = dao.getDrugs("COUGH", concept, true, true, false, 0, 10);
		assertEquals(2, drugList.size());
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 */
	@Test
	public void getDrugs_shouldReturnDistinctDrugs() {
		Concept concept1 = dao.getConcept(14);
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG", concept1, true, true, false, 0, 10);
		assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean)
	 */
	@Test
	public void getDrugs_shouldReturnDrugIf_EitherDrugNameIsUpperOrLowerCase() {
		List<Drug> drugList1 = dao.getDrugs("Triomune-30", null, true);
		assertEquals(1, drugList1.size());
		
		List<Drug> drugList2 = dao.getDrugs("triomune-30", null, true);
		assertEquals(1, drugList2.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 */
	@Test
	public void getDrugs_shouldReturnDrugIfPhraseMatchDrugNameNoNeedToMatchBothConceptNameAndDrugName() {
		// This concept does not contain concept_name with "Triomune"
		Concept concept2 = dao.getConcept(3);
		
		// In this test there is no any concept_name match with "Triomune" but
		// Drug name match with "Trimonue" so no need to match both drug_name
		// and the concept_name to find drug
		List<Drug> drugList = dao.getDrugs("Triomune", concept2, true, true, false, 0, 10);
		assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 */
	@Test
	public void getDrugs_shouldReturnDrugIfPhaseMatchConceptNameNoNeedToMatchBothConceptNameAndDrugName() {
		Concept concept4 = dao.getConcept(7);
		
		//In this test, there is no any drug_name with "VOIDED" but concept_name
		//match with "VOIDED" so this prove no need to match both drug_name and the
		//concept_name
		List<Drug> drugList = dao.getDrugs("VOIDED", concept4, true, true, false, 0, 10);
		assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 */
	@Test
	public void getDrugs_shouldReturnDrugWhenPhraseMatchDrugNameEvenSerchDrugConceeptNameIsfalse() {
		
		List<Drug> drugList = dao.getDrugs("Triomune-30", null, true, false, false, 0, 10);
		assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String)
	 */
	@Test
	public void getDrugs_shouldNotReturnRetired() {
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG_NAME_RETIRED");
		assertEquals(0, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String)
	 */
	@Test
	public void getDrugs_shouldReturnNonRetired() {
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG_NAME");
		assertEquals(1, drugList.size());
		
	}

	@Test
	public void getDrugs_shouldReturnDrugEvenIf_DrugNameHasSpecialCharacters() {
		List<Drug> drugList1 = dao.getDrugs("DRUG_NAME_WITH_SPECIAL_CHARACTERS (", null, true);
		assertEquals(1, drugList1.size());

	}

	/**
	 * @see HibernateConceptDAO#getConceptAttributeCount(ConceptAttributeType)
	 */
	@Test
	public void shouldGetConceptAttributeCountForAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptAttributeType conceptAttributeType = Context.getConceptService().getConceptAttributeType(1);
		assertEquals(1, dao.getConceptAttributeCount(conceptAttributeType));
		assertEquals(0, dao.getConceptAttributeCount(null));
	}

	@Test //TRUNK-4967
	public void isConceptNameDuplicate_shouldNotFailIfConceptDoesNotHaveADefaultNameForLocale() {
		//given
		ConceptClass diagnosis = dao.getConceptClasses("Diagnosis").get(0);
		ConceptDatatype na = dao.getConceptDatatypeByName("N/A");

		Concept tuberculosis = new Concept();
		tuberculosis.addName(new ConceptName("Tuberculosis", Locale.US));
		tuberculosis.setDatatype(na);
		tuberculosis.setConceptClass(diagnosis);
		dao.saveConcept(tuberculosis);

		ConceptName shortName = new ConceptName("TB", Locale.FRANCE);
		shortName.setConceptNameType(ConceptNameType.SHORT);
		tuberculosis.addName(shortName);

		//when
		boolean duplicate = dao.isConceptNameDuplicate(shortName);

		//then
		//no NPE exception thrown
		assertThat(duplicate, is(false));
	}

}
