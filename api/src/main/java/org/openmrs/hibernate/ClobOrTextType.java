/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

/**
 * @since 2.8.0
 */
public class ClobOrTextType extends AbstractSingleColumnStandardBasicType<String> {

    public ClobOrTextType(Dialect dialect) {
        super(resolveSqlTypeDescriptor(dialect), StringTypeDescriptor.INSTANCE);
    }

	private static SqlTypeDescriptor resolveSqlTypeDescriptor(Dialect dialect) {
		if (dialect instanceof MySQLDialect) {
			return ClobTypeDescriptor.DEFAULT;
		}
		return VarcharTypeDescriptor.INSTANCE;
	}

    @Override
    public String getName() {
        return "large_text";
    }
} 
