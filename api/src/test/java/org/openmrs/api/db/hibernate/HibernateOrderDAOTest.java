package org.openmrs.api.db.hibernate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernateOrderDAOTest extends BaseContextSensitiveTest {
	
	private HibernateOrderDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 *
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null) {
			dao = (HibernateOrderDAO) applicationContext.getBean("orderDAO");
		}
	}
	
	/**
	 * @see HibernateOrderDAO#getOrderTypeByConceptClass(ConceptClass)
	 * @verifies return order type mapped to given concept class
	 */
	@Test
	public void getAllPatientIdentifierTypes_shouldReturnOrderTypeMappedToGivenConceptClass() throws Exception {
		OrderType orderType = dao.getOrderTypeByConceptClass(Context.getConceptService().getConceptClass(1));
		
		Assert.assertNotNull(orderType);
		Assert.assertEquals(2, orderType.getOrderTypeId().intValue());
	}
}
