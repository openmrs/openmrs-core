/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.EnhancedUserType;

/**
 * A custom UserType for mapping Java enums as strings in Hibernate 7.x HBM XML mappings.
 * This replaces the removed {@code org.hibernate.type.EnumType} which was used in Hibernate 6.x.
 * <p>
 * Usage in HBM XML:
 * <pre>{@code
 * <property name="status" column="status" length="16" not-null="true">
 *     <type name="org.openmrs.api.db.hibernate.type.StringEnumType">
 *         <param name="enumClass">org.openmrs.Obs$Status</param>
 *     </type>
 * </property>
 * }</pre>
 *
 * NOTE: This class should be deleted once Obs, ConceptName, and OrderSet are migrated from
 * hibernate xml mapping files to annotations.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class StringEnumType implements EnhancedUserType<Enum>, DynamicParameterizedType {

	private Class<? extends Enum> enumClass;

	@Override
	public void setParameterValues(Properties parameters) {
		String enumClassName = parameters.getProperty("enumClass");
		if (enumClassName == null) {
			enumClassName = parameters.getProperty(ENTITY);
		}
		try {
			enumClass = (Class<? extends Enum>) Class.forName(enumClassName);
		}
		catch (ClassNotFoundException e) {
			throw new HibernateException("Enum class not found: " + enumClassName, e);
		}
	}

	@Override
	public int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<Enum> returnedClass() {
		return (Class<Enum>) enumClass;
	}

	@Override
	public boolean equals(Enum x, Enum y) throws HibernateException {
		return x == y;
	}

	@Override
	public int hashCode(Enum x) throws HibernateException {
		return x == null ? 0 : x.hashCode();
	}

	@Override
	public Enum nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
		String name = rs.getString(position);
		if (rs.wasNull() || name == null) {
			return null;
		}
		return Enum.valueOf(enumClass, name);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Enum value, int index, WrapperOptions options) throws SQLException {
		if (value == null) {
			st.setNull(index, Types.VARCHAR);
		} else {
			st.setString(index, value.name());
		}
	}

	@Override
	public Enum deepCopy(Enum value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Enum value) throws HibernateException {
		return value == null ? null : value.name();
	}

	@Override
	public Enum assemble(Serializable cached, Object owner) throws HibernateException {
		if (cached == null) {
			return null;
		}
		return Enum.valueOf(enumClass, (String) cached);
	}

	@Override
	public String toSqlLiteral(Enum value) {
		return value == null ? "null" : "'" + value.name() + "'";
	}

	@Override
	public String toString(Enum value) throws HibernateException {
		return value == null ? null : value.name();
	}

	@Override
	public Enum fromStringValue(CharSequence sequence) throws HibernateException {
		if (sequence == null) {
			return null;
		}
		return Enum.valueOf(enumClass, sequence.toString());
	}
}
