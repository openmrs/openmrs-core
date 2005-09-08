package org.openmrs.api.ibatis;

import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.User;

import com.ibatis.sqlmap.client.SqlMapClient;

public class IbatisTest extends TestCase {
	
	public void setUp() {
		SqlMapClient mapper = SqlMap.instance();
		assertNotNull("Ibatis SqlMap instance", mapper);		
	}
	
	public void testConnection() throws SQLException {
		User user = (User)SqlMap.instance().queryForObject("getUser", 1);
		assertNotNull("user #1", user);
	}
	
	public void tearDown() {
	}
	
	public static Test suite() {
		return new TestSuite(IbatisTest.class, "Basic Ibatis functionality");
	}

}
