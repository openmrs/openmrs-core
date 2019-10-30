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
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.standard.ClassicFilterFactory;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.cfg.SearchMapping;

public class LuceneAnalyzerFactory {
	@Factory
	public SearchMapping getSearchMapping() {
		SearchMapping mapping = new SearchMapping();
		mapping
			.analyzerDef(LuceneAnalyzers.PHRASE_ANALYZER, KeywordTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class);
		mapping.analyzerDef(LuceneAnalyzers.EXACT_ANALYZER, WhitespaceTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class);
		mapping.analyzerDef(LuceneAnalyzers.START_ANALYZER, WhitespaceTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class)
			.filter(EdgeNGramFilterFactory.class)
                .param("minGramSize", "2")
                .param("maxGramSize", "20");
		mapping.analyzerDef(LuceneAnalyzers.ANYWHERE_ANALYZER, WhitespaceTokenizerFactory.class)
			.filter(ClassicFilterFactory.class)
			.filter(LowerCaseFilterFactory.class)
			.filter(NGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");
		return mapping;
	}
}

