package org.openmrs.api.db.hibernate;

import static java.util.Collections.emptyList;


import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;

public class HibernatePatientIdentifierMergeDAOTest extends BaseContextSensitiveTest {
	
	private HibernatePatientIdentifierMergeDAO hibernatePatientIdentifierMergeDao = new HibernatePatientIdentifierMergeDAO();
	private SessionFactory sessionFactory;
	
	@Before
	public void getPersonDAO() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		hibernatePatientIdentifierMergeDao.setSessionFactory(sessionFactory);
	}
	
	

	@Test
	public void mergePatientIdentifierTypes() {
		// Creating new patient identifier types
		PatientIdentifierType a = new PatientIdentifierType();
		Date dateobj = new Date();
		a.setName("SSN");
		a.setId(5);
		a.setDateCreated(dateobj);
		sessionFactory.getCurrentSession().update(a);
		PatientIdentifierType c = new PatientIdentifierType();
		c.setName("BadNumber");
		c.setId(6);
		c.setDateCreated(dateobj);
		sessionFactory.getCurrentSession().update(c);
		
		// Calling the merge function for merging duplicate patient identifier types
		hibernatePatientIdentifierMergeDao.mergePatientIdentifier(a, c);
		PatientIdentifierType cShouldBeNull = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, c.getId());
		PatientIdentifierType aShouldBeTheSame = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, a.getId());
		
		// cShouldBeNull is null because it is deleted after being merged
		assertEquals(null, cShouldBeNull);
		// a should be the same because it wasn't changed during the merge
		assertEquals(a, aShouldBeTheSame);
	}
	
	
}
