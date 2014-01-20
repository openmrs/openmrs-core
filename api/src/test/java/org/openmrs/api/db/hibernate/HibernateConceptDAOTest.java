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

public class HibernateConceptDAOTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/db/hibernate/include/HibernateConceptTestDataSet.xml";
	
	private HibernateConceptDAO dao = null;
	
	private SessionFactory sessionFactory = null;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(PROVIDERS_INITIAL_XML);
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (HibernateConceptDAO) applicationContext.getBean("conceptDAO");
		
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return a drug if either the drug name or concept name matches the phase not both
	 */
	@Test
	@Verifies(value = "return a drug if either the drug name or concept name matches the phase not both", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugIf_eitherDrugNameOrConceptNameMatchesThePhaseNotBoth() throws Exception {
		Session session1 = sessionFactory.getCurrentSession();
		session1.beginTransaction();
		// This Concept has concept name "COUGH SYRUP"
		Concept concept = (Concept) session1.get(Concept.class, 3);
		
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
		Session session2 = sessionFactory.getCurrentSession();
		session2.beginTransaction();
		Concept concept1 = (Concept) session2.get(Concept.class, 14);
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG", concept1, true, true, false, 0, 10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @see HibernateConceptDAO#getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)
	 * @verifies return a drug if phrase match drug_name No need to match both concept_name and
	 *           drug_name
	 */
	@Test
	@Verifies(value = "return a drug if phrase match drug_name No need to match both concept_name and drug_name", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldReturnDrugIfPhraseMatchDrugNameNoNeedToMatchBothConceptNameAndDrugName() throws Exception {
		Session session3 = sessionFactory.getCurrentSession();
		session3.beginTransaction();
		// This concept does not contain concept_name with "Triomune"
		Concept concept2 = (Concept) session3.get(Concept.class, 3);
		
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
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		//This concept which contain concept_names "VOIDED","Y","YES"
		Concept concept4 = (Concept) session.get(Concept.class, 7);
		
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
	
}
