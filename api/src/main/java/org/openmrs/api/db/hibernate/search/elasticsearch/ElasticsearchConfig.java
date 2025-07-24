/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.elasticsearch;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;
import org.openmrs.api.db.hibernate.search.SearchAnalysis;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Provides Lucene analyzers.
 *
 * @see SearchAnalysis
 * @since 2.8.0
 */
@Component("elasticsearchConfig")
public class ElasticsearchConfig implements ElasticsearchAnalysisConfigurer {
	
	public ElasticsearchConfig() {
	}

	@Override
	public void configure(ElasticsearchAnalysisConfigurationContext context) {
		context.analyzer(SearchAnalysis.NAME_ANALYZER).custom()
			.tokenizer("standard")
			.tokenFilters("lowercase", "asciifolding");

		context.analyzer(SearchAnalysis.PHRASE_ANALYZER).custom()
			.tokenizer("whitespace")
			.tokenFilters("lowercase", "asciifolding");

		context.analyzer(SearchAnalysis.EXACT_ANALYZER).custom()
			.tokenizer("whitespace")
			.tokenFilters("lowercase", "asciifolding");

		context.analyzer(SearchAnalysis.START_ANALYZER).custom()
			.tokenizer("whitespace")
			.tokenFilters("lowercase", "asciifolding", "edge_ngram_2_20");

		context.analyzer(SearchAnalysis.ANYWHERE_ANALYZER).custom()
			.tokenizer("whitespace")
			.tokenFilters("lowercase", "asciifolding", "ngram_2_20");

		context.analyzer(SearchAnalysis.SOUNDEX_ANALYZER).custom()
			.tokenizer("standard")
			.tokenFilters("lowercase", "asciifolding", "phonetic_soundex");
		
		context.tokenFilter("edge_ngram_2_20").type("edge_ngram")
			.param("min_gram", "2").param("max_gram", "20");

		context.tokenFilter("ngram_2_20").type("ngram")
			.param("min_gram", "2").param("max_gram", "20");
		
		context.tokenFilter("phonetic_soundex").type("phonetic").param("encoder", "soundex");
	}
}
