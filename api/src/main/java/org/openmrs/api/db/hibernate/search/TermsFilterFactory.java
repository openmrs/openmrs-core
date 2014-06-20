/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate.search;

import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

public class TermsFilterFactory {
	
	private Set<Set<Term>> includeTerms = new HashSet<Set<Term>>();
	
	private Set<Term> excludeTerms = new HashSet<Term>();
	
	public void setIncludeTerms(Set<Set<Term>> terms) {
		this.includeTerms = new HashSet<Set<Term>>(terms);
	}
	
	public void setExcludeTerms(Set<Term> terms) {
		this.excludeTerms = new HashSet<Term>(terms);
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
		
		for (Set<Term> terms : includeTerms) {
			BooleanQuery subquery = new BooleanQuery();
			for (Term term : terms) {
				subquery.add(new TermQuery(term), Occur.SHOULD);
			}
			query.add(subquery, Occur.MUST);
		}
		
		if (includeTerms.isEmpty()) {
			query.add(new MatchAllDocsQuery(), Occur.MUST);
		}
		
		for (Term term : excludeTerms) {
			query.add(new TermQuery(term), Occur.MUST_NOT);
		}
		
		return new CachingWrapperFilter(new QueryWrapperFilter(query));
	}
}
