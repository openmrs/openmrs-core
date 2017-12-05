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

import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;
import org.hibernate.search.filter.impl.CachingWrapperFilter;

public class TermsFilterFactory {
	
	private Set<Set<Term>> includeTerms = new HashSet<>();
	
	private Set<Term> excludeTerms = new HashSet<>();
	
	public void setIncludeTerms(Set<Set<Term>> terms) {
		this.includeTerms = new HashSet<>(terms);
	}
	
	public void setExcludeTerms(Set<Term> terms) {
		this.excludeTerms = new HashSet<>(terms);
	}

	@Key
	public FilterKey getKey() {
		StandardFilterKey key = new StandardFilterKey();
		key.addParameter(includeTerms);
		key.addParameter(excludeTerms);
		return key;
	}
	
	@Factory
	public Filter getFilter() {
		BooleanQuery query = new BooleanQuery();

		if (includeTerms.isEmpty()) {
			query.add(new MatchAllDocsQuery(), Occur.MUST);
		} else {
			for (Set<Term> terms : includeTerms) {

				if (terms.size() == 1) {
					query.add(new TermQuery(terms.iterator().next()), Occur.MUST);
				} else if (terms.size() > 1) {
					BooleanQuery subquery = new BooleanQuery();
					for (Term term : terms) {
						subquery.add(new TermQuery(term), Occur.SHOULD);
					}
					query.add(subquery, Occur.MUST);
				}
			}
		}

		for (Term term : excludeTerms) {
			query.add(new TermQuery(term), Occur.MUST_NOT);
		}
		
		return new CachingWrapperFilter(new QueryWrapperFilter(query));
	}
}
