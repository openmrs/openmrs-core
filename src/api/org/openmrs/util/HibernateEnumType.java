/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * This is intended to be a temporary utility class until enums are handled properly in Hibernate:
 * http://community.jboss.org/wiki/Java5EnumUserType
 * http://docs.jboss.org/hibernate/stable/core/reference/en/html_single/#inheritance
 */
public class HibernateEnumType implements UserType, ParameterizedType {
	
	private static final int[] SQL_TYPES = { Types.VARCHAR };

	private Class clazz = null;
	
	public void setParameterValues(Properties params) {
    	String enumClassName = params.getProperty("enumClassName");
    	if (enumClassName == null) {
    		throw new MappingException("enumClassName parameter not specified");
		}
  
	  	try {
			this.clazz = Class.forName(enumClassName);
		}
		catch (ClassNotFoundException e) {
			throw new MappingException("enumClass " + enumClassName + " not found", e);
		}
	}
	
	public int[] sqlTypes() {
		return SQL_TYPES;
	}
	
	public Class<?> returnedClass() {
		return clazz;
	}
	
	@SuppressWarnings("unchecked")
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
		String name = resultSet.getString(names[0]);
		Object result = null;
		if (!resultSet.wasNull() && !StringUtils.isBlank(name)) {
			result = Enum.valueOf(clazz, name);
		}
		return result;
	}
	
	public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index) throws HibernateException,
	                                                                                     SQLException {
		if (null == value) {
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			preparedStatement.setString(index, ((Enum) value).name());
		}
	}
	
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}
	
	public boolean isMutable() {
		return false;
	}
	
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}
	
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}
	
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}
	
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}
	
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y)
			return true;
		if (null == x || null == y)
			return false;
		return x.equals(y);
	}
}
