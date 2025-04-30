/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.lucene;

import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.phonetic.PhoneticFilterFactory;
import org.apache.lucene.analysis.standard.ClassicFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.openmrs.api.db.hibernate.search.SearchAnalysis;

/**
 * Provides Lucene analyzers.
 *
 * @see SearchAnalysis
 * @since 2.8.0
 */
public class LuceneAnalysisConfigurer implements org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer {
	
	public LuceneAnalysisConfigurer() {
	}

	@Override
	public void configure(LuceneAnalysisConfigurationContext context) {
		context.analyzer(SearchAnalysis.NAME_ANALYZER).custom()
			.tokenizer(StandardTokenizerFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class);
		
		context.analyzer(SearchAnalysis.PHRASE_ANALYZER).custom()
			.tokenizer(KeywordTokenizerFactory.class)
			.tokenFilter(ClassicFilterFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class);

		context.analyzer(SearchAnalysis.EXACT_ANALYZER).custom()
			.tokenizer(WhitespaceTokenizerFactory.class)
			.tokenFilter(ClassicFilterFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class);

		context.analyzer(SearchAnalysis.START_ANALYZER).custom()
			.tokenizer(WhitespaceTokenizerFactory.class)
			.tokenFilter(ClassicFilterFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class)
			.tokenFilter(EdgeNGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");

		context.analyzer(SearchAnalysis.ANYWHERE_ANALYZER).custom()
			.tokenizer(WhitespaceTokenizerFactory.class)
			.tokenFilter(ClassicFilterFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class)
			.tokenFilter(NGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");

		context.analyzer(SearchAnalysis.SOUNDEX_ANALYZER).custom()
			.tokenizer(StandardTokenizerFactory.class)
			.tokenFilter(ClassicFilterFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(PhoneticFilterFactory.class)
			.param("encoder", "Soundex");
	}
}
