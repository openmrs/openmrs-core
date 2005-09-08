package org.openmrs.api.hibernate;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateTest extends TestCase {

	public void testHibernate() {
		Session session = HibernateUtil.currentSession();
		Assert.assertNotNull("obtain session object", session);
		Transaction tx = session.beginTransaction();
		Assert.assertNotNull("begin transaction", tx);
		tx.commit();
		HibernateUtil.closeSession();
	}
	
	public static Test suite() {
		return new TestSuite(HibernateTest.class, "Basic Hibernate functionality");
	}
	
}
