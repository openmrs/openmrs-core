package org.openmrs.api.db.hibernate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;

/**
 * Tests the methods in {@link HibernateUtil}
 */
public class HibernateUtilTest extends BaseContextSensitiveTest {
	
	private static final String INITIAL_DATA_XML_UNLOCALIZED_ONLY = "org/openmrs/api/db/hibernate/include/HibernateUtilTest-unlocalizedOnly.xml";
	
	private static final String INITIAL_DATA_XML_LOCALIZED_ONLY = "org/openmrs/api/db/hibernate/include/HibernateUtilTest-localizedOnly.xml";
	
	private static final String INITIAL_DATA_XML_BOTH = "org/openmrs/api/db/hibernate/include/HibernateUtilTest-both.xml";
	
	
	/**
	 * @see {@link HibernateUtil#getEqCriterionForLocalizedColumn(String,String)}
	 */
    @Test
    @SkipBaseSetup
    @Verifies(value = "should return correct criterion when has unlocalized value only", method = "getEqCriterionForLocalizedColumn(String,String)")
    public void getEqCriterionForLocalizedColumn_shouldReturnCorrectCriterionWhenHasUnlocalizedValueOnly() throws Exception {
    	// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		// add test data which exists unlocalized value only
		executeDataSet(INITIAL_DATA_XML_UNLOCALIZED_ONLY);
		
		// emulate EncounterService#getEncounterType(String) to run this method
    	EncounterType type = Context.getEncounterService().getEncounterType("Test Enc Type B");
    	assertNotNull(type);
    	assertTrue(type.getEncounterTypeId().equals(2));
    }

	/**
     * @see {@link HibernateUtil#getEqCriterionForLocalizedColumn(String,String)}
     * 
     */
    @Test
    @Verifies(value = "should return correct criterion when has localized values only", method = "getEqCriterionForLocalizedColumn(String,String)")
    public void getEqCriterionForLocalizedColumn_shouldReturnCorrectCriterionWhenHasLocalizedValuesOnly() throws Exception {
    	// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		// add test data which exists localized values only
		executeDataSet(INITIAL_DATA_XML_LOCALIZED_ONLY);
		
		// emulate EncounterService#getEncounterType(String) to run this method
		EncounterType type = Context.getEncounterService().getEncounterType("Test Enc Type A");
		assertNotNull(type);
		assertTrue(type.getEncounterTypeId().equals(3));
    }

	/**
     * @see {@link HibernateUtil#getEqCriterionForLocalizedColumn(String,String)}
     * 
     */
    @Test
    @Verifies(value = "should return correct criterion when has unlocalized and localized values", method = "getEqCriterionForLocalizedColumn(String,String)")
    public void getEqCriterionForLocalizedColumn_shouldReturnCorrectCriterionWhenHasUnlocalizedAndLocalizedValues()
                                                                                                                   throws Exception {
    	// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		// add test data which exists both unlocalized and localized values
		executeDataSet(INITIAL_DATA_XML_BOTH);
		
		// emulate EncounterService#getEncounterType(String) to run this method
		
		// this encounterType hasn't been localized
		EncounterType type = Context.getEncounterService().getEncounterType("Test Enc Type B");
		assertNotNull(type);
		assertTrue(type.getEncounterTypeId().equals(2));
		
		// this encounterType has been localized
		type = Context.getEncounterService().getEncounterType("Test Enc Type A");
		assertNotNull(type);
		assertTrue(type.getEncounterTypeId().equals(3));
    }
    
    /**
	 * @see {@link HibernateUtil#getLikeCriterionForLocalizedColumn(String,String,null,MatchMode)}
	 */
    @Test
    @Verifies(value = "should return correct criterion when has unlocalized value only", method = "getLikeCriterionForLocalizedColumn(String,String,null,MatchMode)")
	public void getLikeCriterionForLocalizedColumn_shouldReturnCorrectCriterionWhenHasUnlocalizedValueOnly()
	                                                                                                        throws Exception {
    	// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		// add test data which exists unlocalized value only
		executeDataSet(INITIAL_DATA_XML_UNLOCALIZED_ONLY);
		
		// emulate EncounterService#getEncounterType(String) to run this method
		List<EncounterType> types = Context.getEncounterService().findEncounterTypes("Test Enc Type");
		assertNotNull(types);
		assertTrue(types.size() == 2);
    }

	/**
     * @see {@link HibernateUtil#getLikeCriterionForLocalizedColumn(String,String,null,MatchMode)}
     * 
     */
    @Test
    @Verifies(value = "should return correct criterion when has localized values only", method = "getLikeCriterionForLocalizedColumn(String,String,null,MatchMode)")
	public void getLikeCriterionForLocalizedColumn_shouldReturnCorrectCriterionWhenHasLocalizedValuesOnly() throws Exception {
    	// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		// add test data which exists localized values only
		executeDataSet(INITIAL_DATA_XML_LOCALIZED_ONLY);
		
		// emulate EncounterService#getEncounterType(String) to run this method
		List<EncounterType> types = Context.getEncounterService().findEncounterTypes("Test Enc Type");
		assertNotNull(types);
		assertTrue(types.size() == 2);
    }

	/**
     * @see {@link HibernateUtil#getLikeCriterionForLocalizedColumn(String,String,null,MatchMode)}
     * 
     */
    @Test
    @Verifies(value = "should return correct criterion when has unlocalized and localized values", method = "getLikeCriterionForLocalizedColumn(String,String,null,MatchMode)")
	public void getLikeCriterionForLocalizedColumn_shouldReturnCorrectCriterionWhenHasUnlocalizedAndLocalizedValues()
	                                                                                                                 throws Exception {
    	// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		// add test data which exists both unlocalized and localized values
		executeDataSet(INITIAL_DATA_XML_BOTH);
		
		// emulate EncounterService#getEncounterType(String) to run this method
		List<EncounterType> types = Context.getEncounterService().findEncounterTypes("Test Enc Type");
		assertNotNull(types);
		assertTrue(types.size() == 2);
		
		types = Context.getEncounterService().findEncounterTypes("Some Retired Type");
		assertNotNull(types);
		assertTrue(types.size() == 1);
    }

}
