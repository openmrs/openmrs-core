package org.openmrs.api.db.hibernate;

import static java.util.Collections.emptyList;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItems;

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
	public void getPatientIdentifiers_shouldGetByIdentifierType() {
		PatientIdentifierType a = new PatientIdentifierType();
		a.setName("BadNumber");
		PatientIdentifierType c = new PatientIdentifierType();
		c.setName("SSN");
		List<PatientIdentifierType> b = new ArrayList<>();
		b.add(a);
		System.out.println(hibernatePatientIdentifierMergeDao);
		hibernatePatientIdentifierMergeDao.mergePatientIdentifier(c, b);
	}
	
	
}
