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
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.cfg.AnalyzerDefMapping;
import org.hibernate.search.cfg.SearchMapping;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptMapTypeEditor;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
 * 	The filters used can be configured with the global property "search.filters".
 *
 * @since 2.4.0
 */
public class LuceneAnalyzerFactory {
	
	private static final Logger log = LoggerFactory.getLogger(ConceptMapTypeEditor.class);
	
	private static final String gp_name = OpenmrsConstants.GP_LUCENE_SEARCH_FILTERS;

	@Factory
	public SearchMapping getSearchMapping() {
		
		String filters = Context.getAdministrationService().getGlobalProperty(gp_name);

		Set<Class<? extends TokenFilterFactory>> filterFactories = Arrays.stream(filters.split(","))
			.map(this::getFilterFactory)
			.collect(Collectors.toSet());

		SearchMapping mapping = new SearchMapping();
		
		AnalyzerDefMapping phrase = mapping.analyzerDef(LuceneAnalyzers.PHRASE_ANALYZER, KeywordTokenizerFactory.class);
		addFilters(phrase, filterFactories);

		AnalyzerDefMapping exact = mapping.analyzerDef(LuceneAnalyzers.EXACT_ANALYZER, WhitespaceTokenizerFactory.class);
		addFilters(exact, filterFactories);
		
		AnalyzerDefMapping start = mapping.analyzerDef(LuceneAnalyzers.START_ANALYZER, WhitespaceTokenizerFactory.class);
		start.filter(EdgeNGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");
		addFilters(start, filterFactories);
		
		AnalyzerDefMapping anywhere = mapping.analyzerDef(LuceneAnalyzers.ANYWHERE_ANALYZER, WhitespaceTokenizerFactory.class);
		anywhere.filter(NGramFilterFactory.class)
			.param("minGramSize", "2")
			.param("maxGramSize", "20");
		addFilters(anywhere, filterFactories);
		
		return mapping;
	}
	
	private Class<? extends TokenFilterFactory> getFilterFactory(String name) {
		Class<?> clz;
		try {
			clz = Class.forName(name);
		} catch (Exception e) {
			log.error("Unable to get Lucene filter factory named '" + name + "'. Please check the value of the setting " + gp_name);
			throw new RuntimeException(e);
		}
		Class<? extends TokenFilterFactory> tff;
		try {
			tff = clz.asSubclass(TokenFilterFactory.class);
		} catch (ClassCastException e) {
			log.error("The class '" + name + "' provided by the setting does not appear to be a TokenFilterFactory. " +
				"Filters listed in the setting " + gp_name + " must extend TokenFilterFactory.");
			throw e;
		}
		return tff;
	}
	
	private void addFilters(AnalyzerDefMapping def, Set<Class<? extends TokenFilterFactory>> filterFactories) {
		for (Class<? extends TokenFilterFactory> ff : filterFactories) {
			def.filter(ff);
		}
	}
}

