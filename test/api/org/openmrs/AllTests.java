package org.openmrs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.api.ibatis.IbatisTest;
import org.openmrs.context.ContextTest;

public class AllTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTest(IbatisTest.suite());
		suite.addTest(ContextTest.suite());
//		suite.addTest(HibernateTest.suite());
		
		return suite;
	}
}
