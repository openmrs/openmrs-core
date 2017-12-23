/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search;

/**
 * All analyzers are defined on BaseOpenmrsObject.
 *
 * @see org.openmrs.BaseOpenmrsObject
 */
public class LuceneAnalyzers {
	
	private LuceneAnalyzers() {
	}
	
	public static final String START_ANALYZER = "startAnalyzer";
	
	public static final String ANYWHERE_ANALYZER = "anywhereAnalyzer";
	
	public static final String EXACT_ANALYZER = "exactAnalyzer";
	
	public static final String PHRASE_ANALYZER = "phraseAnalyzer";
}
