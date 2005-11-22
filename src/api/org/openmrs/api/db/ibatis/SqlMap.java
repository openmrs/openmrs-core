package org.openmrs.api.ibatis;

import java.io.Reader;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;


public class SqlMap {

	private static Reader reader;
	private static SqlMapClient sqlMap;

	static {
		try {
			reader = Resources.getResourceAsReader("SqlMapConfig.xml");
			sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static SqlMapClient instance() {
		return sqlMap;
	}
	
}
