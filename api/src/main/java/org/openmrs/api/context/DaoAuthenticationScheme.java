/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import org.openmrs.api.db.ContextDAO;

/**
 * Base class for authentication schemes that intend to leverage OpenMRS' {@link ContextDAO}.
 *
 * @see {@link Context#authenticate(Credentials)}
 * 
 * @since 2.3.0
 */
public abstract class DaoAuthenticationScheme implements AuthenticationScheme {
	
	protected ContextDAO getContextDAO() {
		return Context.getContextDAO();
	}
}
