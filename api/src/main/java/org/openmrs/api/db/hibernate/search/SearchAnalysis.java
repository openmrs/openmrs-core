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

import org.openmrs.api.db.hibernate.search.lucene.LuceneConfig;

/**
 * Defines analyzers and normalizers to be used in Hibernate Search mappings.
 * <p>
 * Objects such as PersonName can use the analyzers provided by this mapping to make their fields searchable.
 * This class defines some default analyzers:
 * 	phraseAnalyzer, which allows searching for an entire phrase, including whitespace
 * 	startAnalyzer, which allows searching for tokens that match at the beginning
 * 	exactAnalyzer, which allows searching for tokens that are identical
 * 	anywhereAnalyzer, which allows searching for text within tokens
 *
 * @see LuceneConfig
 * @since 2.8.0
 */
public class SearchAnalysis {
	
	private SearchAnalysis() {
	}
	
	public static final String START_ANALYZER = "startAnalyzer";
	
	public static final String ANYWHERE_ANALYZER = "anywhereAnalyzer";
	
	public static final String EXACT_ANALYZER = "exactAnalyzer";
	
	public static final String PHRASE_ANALYZER = "phraseAnalyzer";
	
	public static final String SOUNDEX_ANALYZER = "soundexAnalyzer";
	
	public static final String NAME_ANALYZER = "nameAnalyzer";
}
