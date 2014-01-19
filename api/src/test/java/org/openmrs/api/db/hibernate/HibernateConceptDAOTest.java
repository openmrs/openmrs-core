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
	 * @verifies should not return distinct drugs
	 */
	@Test
	@Verifies(value = "should not return distinct drugs ", method = "getDrugs(String,Concept,boolean,boolean,boolean,Integer,Integer)")
	public void getDrugs_shouldNotRetunDistinctDrugs() throws Exception {
		Session session2 = sessionFactory.getCurrentSession();
		session2.beginTransaction();
		Concept concept1 = (Concept) session2.get(Concept.class, 14);
		
		List<Drug> drugList = dao.getDrugs("TEST_DRUG", concept1, true, true, false, 0, 10);
		Assert.assertEquals(2, drugList.size());
		
	}
	
}
