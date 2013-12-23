package org.openmrs.api.db.hibernate;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateConceptDAOTest extends BaseContextSensitiveTest{

	private static final String PROVIDERS_INITIAL_XML = "org/openmrs/api/db/hibernate/include/HibernateConceptTestDataSet.xml";
	
	
	private HibernateConceptDAO dao=null;
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
	 * @verifies  drug returns if either the drug name or concept name matches the query string
	 * 
	 */
	@Test
	public void getDrugsReturnsDrugs_eitherDrugNameOrConceptNameMatch()
	{
		Session session =sessionFactory.getCurrentSession();
		session.beginTransaction();
		//This Concept has concept name "COUGH SYRUP"
		Concept concept = (Concept)session.get(Concept.class, 3);
		
		
		//concept has COUGH as a concept name and also Drug has Drug_name as "COUGH" so return two distinct drugs that means search either drug name or concept name
		List<Drug> drugList=dao.getDrugs("COUGH",concept,true,true,false,0,10);
		Assert.assertEquals(2, drugList.size());
		
	}
	
	/**
	 * @verifies  do not return same drug multiple times if a drug has multiple names that match the search parameter
	 * 
	 */
	@Test
	public void getDrugsReturnsOnlyDistinctDrugsEitherDrugNameHasMultipleNamesThatMatchParameterOrNot()
	{
		Session session =sessionFactory.getCurrentSession();
		session.beginTransaction();
		//this method return concept which contain concept_name "FAVORITE FOOD, NON-CODED FOOD"
		Concept concept = (Concept)session.get(Concept.class, 4);
		
		
		//In this test concept_name match two times in concept name but return one distinct drug only
		List<Drug> drugList=dao.getDrugs("FOOD",concept,true,true,false,0,10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @verifies  drug can find only from drug name do not need to match both drug_name and concept_name
	 * 
	 */
	@Test
	public void getDrugsSearchFromOnlyDrugName()
	{
		Session session =sessionFactory.getCurrentSession();
		session.beginTransaction();
		//this method return concept which contain concept_name "COUGH SYRUP"
		Concept concept = (Concept)session.get(Concept.class, 3);
		
		
		//In this test there is no any concept_name match with "Triomune" but Drug name match with "Trimonue" so no need to match both drug_name and the concept_name to find drug
		List<Drug> drugList=dao.getDrugs("Triomune",concept,true,true,false,0,10);
		Assert.assertEquals(1, drugList.size());
		
	}
	
	/**
	 * @verifies  drug can find only from drug name do not need to match both drug_name and concept_name
	 * 
	 */
	@Test
	public void getDrugsSearchFromOnlyConceptName()
	{
		Session session =sessionFactory.getCurrentSession();
		session.beginTransaction();
		//this method return concept which contain concept_names "VOIDED","Y","YES"
		Concept concept = (Concept)session.get(Concept.class, 7);
		
		
		//In this test, there is no any drug name with "VOIDED" but concept_name with VOIDED so this prove no need to match both drug_name and the concept_name 
		List<Drug> drugList=dao.getDrugs("VOIDED",concept,true,true,false,0,10);
		Assert.assertEquals(1, drugList.size());
		
	}
	/**
	 * @verifies  drug can find only form drug name when concept is null and searchDrugConceptNames is false
	 * 
	 */
	@Test
	public void getDrugsSearchDrugWhenConceptNameIsNUll()
	{
			
		List<Drug> drugList=dao.getDrugs("Triomune-30",null,true,false,false,0,10);
		Assert.assertEquals(1, drugList.size());
		
	}

}
