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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.QueryWrapperFilter;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.StandardFilterKey;
import org.hibernate.search.filter.FilterKey;

import java.util.List;
import java.util.Map;

public class FilterFactory {
	
	private Map<String, Object> fieldMap;
	
	public void setFieldMap(Map<String, Object> map) {
		this.fieldMap = map;
	}
	
	@Key
	public FilterKey getKey() {
		StandardFilterKey key = new StandardFilterKey();
		key.addParameter(fieldMap);
		return key;
	}
	
	@Factory
	public Filter getFilter() {
		BooleanQuery query = new BooleanQuery();
		for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
			String field = entry.getKey();
			Object value = entry.getValue();
			if (value.getClass().isArray()) {
				BooleanQuery arrayQuery = new BooleanQuery();
				BooleanClause.Occur clause = (field.startsWith("-")) ? BooleanClause.Occur.MUST_NOT
				        : BooleanClause.Occur.SHOULD;
				if (clause == BooleanClause.Occur.MUST_NOT) {
					arrayQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
					field = field.substring(1);
				}
				Object[] values = (Object[]) value;
				for (Object val : values) {
					arrayQuery.add(new TermQuery(new Term(field, val.toString())), clause);
				}
				query.add(arrayQuery, BooleanClause.Occur.MUST);
			} else {
				query.add(new TermQuery(new Term(field, value.toString())), BooleanClause.Occur.MUST);
			}
		}
		return new CachingWrapperFilter(new QueryWrapperFilter(query));
	}
}
