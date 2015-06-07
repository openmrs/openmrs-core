/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.EnumType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.openmrs.api.context.Context;

/**
 * This is left for backwards compatibility with OpenMRS 1.11. and before. OpenMRS 1.12 and later
 * are shipped with Hibernate 4+, which has the built-in support for enums. Please use EnumType
 * instead.
 *
 * @since 1.7
 * @deprecated since 1.12. Use {@link EnumType}.
 */
@Deprecated
public class HibernateEnumType implements UserType, ParameterizedType {
	
	private static final int[] SQL_TYPES = { Types.VARCHAR };
	
	private Class clazz = null;
	
	public void setParameterValues(Properties params) {
		String enumClassName = params.getProperty("enumClassName");
		if (enumClassName == null) {
			throw new MappingException("enumClassName parameter not specified");
		}
		
		try {
			this.clazz = Context.loadClass(enumClassName);
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
	
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
	        throws HibernateException, SQLException {
		String name = rs.getString(names[0]);
		Object result = null;
		if (!rs.wasNull() && !StringUtils.isBlank(name)) {
			result = Enum.valueOf(clazz, name);
		}
		return result;
	}
	
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
	        throws HibernateException, SQLException {
		if (null == value) {
			st.setNull(index, Types.VARCHAR);
		} else {
			st.setString(index, ((Enum) value).name());
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
	
	@SuppressWarnings("squid:S1201")
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if (null == x || null == y) {
			return false;
		}
		return x.equals(y);
	}
	
}
