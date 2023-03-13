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

import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.standard.ClassicFilterFactory;
import org.apache.lucene.analysis.phonetic.PhoneticFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.cfg.SearchMapping;

/**
 * Provides a Lucene SearchMapping for any objects in openmrs-core.
 * 
 * Objects such as PersonName can use the analyzers provided by this mapping to make their fields searchable.
 * This class defines some default analyzers:
 * 	phraseAnalyzer, which allows searching for an entire phrase, including whitespace
 * 	startAnalyzer, which allows searching for tokens that match at the beginning
 * 	exactAnalyzer, which allows searching for tokens that are identical
 * 	anywhereAnalyzer, which allows searching for text within tokens
 *
 * @since 2.4.0
 */
public class LuceneAnalyzerFactory {
	@Factory
	public SearchMapping getSearchMapping() {
		SearchMapping mapping = new SearchMapping();
		mapping
			.analyzerDef(LuceneAnalyzers.PHRASE_ANALYZER, KeywordTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class)
			.filter(ASCIIFoldingFilterFactory.class);
		mapping.analyzerDef(LuceneAnalyzers.EXACT_ANALYZER, WhitespaceTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class)
			.filter(ASCIIFoldingFilterFactory.class);
		mapping.analyzerDef(LuceneAnalyzers.START_ANALYZER, WhitespaceTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class)
			.filter(ASCIIFoldingFilterFactory.class)
			.filter(EdgeNGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");
		mapping.analyzerDef(LuceneAnalyzers.ANYWHERE_ANALYZER, WhitespaceTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class)
			.filter(ASCIIFoldingFilterFactory.class)
			.filter(NGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");
		mapping.analyzerDef(LuceneAnalyzers.SOUNDEX_ANALYZER, StandardTokenizerFactory.class)
			.filter(ClassicFilterFactory.class) 
			.filter(LowerCaseFilterFactory.class)
			.filter(PhoneticFilterFactory.class)
			.param("encoder", "Soundex");
		
		return mapping;
	}
}

