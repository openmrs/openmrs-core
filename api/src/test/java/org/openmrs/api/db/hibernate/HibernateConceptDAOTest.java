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

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateConceptDAOTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/db/hibernate/include/HibernateConceptTestDataSet.xml";
	
	@Autowired
	private HibernateConceptDAO dao;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(PROVIDERS_INITIAL_XML);
		
		updateSearchIndex();
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return a drug if either the drug name or concept name matches the phase not both
	 */
	@Test
	@Verifies(value = "return a drug if either the drug name or concept name matches the phase not both", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugIf_eitherDrugNameOrConceptNameMatchesThePhaseNotBoth() throws Exception {
		Concept concept = dao.getConcept(3);
		
		// concept has "COUGH SYRUP" as a concept_name and also Drug has
		// Drug_name as "COUGH" so return two distinct drugs that means search
		// either drug name or concept name match the phase
		List<Drug> drugList = dao.getDrugs("COUGH", concept, true, true, false, 0, 10);
		Assert.assertEquals(2, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return distinct drugs
	 */
	@Test
	@Verifies(value = "return distinct drugs", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDistinctDrugs() throws Exception {
		Concept concept1 = dao.getConcept(14);
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG", concept1, true, true, false, 0, 10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean)
	 * @verifies returns a drug regardless of case sensitivity
	 */
	@Test
	@Verifies(value = "return a drug if drug name is passed with upper or lower case", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugIf_EitherDrugNameIsUpperOrLowerCase() throws Exception {
		List<Drug> drugList1 = dao.getDrugs("Triomune-30", null, true);
		Assert.assertEquals(1, drugList1.size());
		
		List<Drug> drugList2 = dao.getDrugs("triomune-30", null, true);
		Assert.assertEquals(1, drugList2.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return a drug if phrase match drug_name No need to match both concept_name and
	 *           drug_name
	 */
	@Test
	@Verifies(value = "return a drug if phrase match drug_name No need to match both concept_name and drug_name", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugIfPhraseMatchDrugNameNoNeedToMatchBothConceptNameAndDrugName() throws Exception {
		// This concept does not contain concept_name with "Triomune"
		Concept concept2 = dao.getConcept(3);
		
		// In this test there is no any concept_name match with "Triomune" but
		// Drug name match with "Trimonue" so no need to match both drug_name
		// and the concept_name to find drug
		List<Drug> drugList = dao.getDrugs("Triomune", concept2, true, true, false, 0, 10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return a drug, if phrase match concept_name No need to match both concept_name and
	 *           drug_name
	 */
	@Test
	@Verifies(value = "return a drug, if phrase match concept_name No need to match both concept_name and drug_name", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugIfPhaseMatchConceptNameNoNeedToMatchBothConceptNameAndDrugName() throws Exception {
		Concept concept4 = dao.getConcept(7);
		
		//In this test, there is no any drug_name with "VOIDED" but concept_name
		//match with "VOIDED" so this prove no need to match both drug_name and the
		//concept_name
		List<Drug> drugList = dao.getDrugs("VOIDED", concept4, true, true, false, 0, 10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return drug when phrase match drug_name even searchDrugConceptNames is false
	 */
	@Test
	@Verifies(value = "return drug when phrase match drug_name even searchDrugConceptNames is false", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugWhenPhraseMatchDrugNameEvenSerchDrugConceeptNameIsfalse() throws Exception {
		
		List<Drug> drugList = dao.getDrugs("Triomune-30", null, true, false, false, 0, 10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String)
	 * @verifies return drug should not return retired
	 */
	@Test
	@Verifies(value = "return drug should not return retired", method = "getDrugs(String)")
	public void getDrugs_shouldNotReturnRetired() throws Exception {
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG_NAME_RETIRED");
		Assert.assertEquals(0, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String)
	 * @verifies return drug should return non-retired
	 */
	@Test
	@Verifies(value = "return drug should return non-retired", method = "getDrugs(String)")
	public void getDrugs_shouldReturnNonRetired() throws Exception {
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG_NAME");
		Assert.assertEquals(1, drugList.size());
		
	}
	
}
