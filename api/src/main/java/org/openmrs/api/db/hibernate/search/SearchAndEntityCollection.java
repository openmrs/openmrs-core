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


import java.util.Collection;
import java.util.function.Function;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

/**
 * Wrapper class around a {@link SearchQueryContributor} object and the Type of the entities to be searched
 * by the query. An instance of this class is set as the source of a
 * {@link SearchCreatedEvent} object.
 * 
 * @since 2.8.0
 */
public class SearchAndEntityCollection<T> implements SearchQueryContributor {
	
	private final SearchQueryContributor queryContributor;
	
	private final Collection<? extends Class<? extends T>> entityCollection;
	
	public SearchAndEntityCollection(SearchQueryContributor query, 
									 Collection<? extends Class<? extends T>> entityCollection) {
		this.queryContributor = query;
		this.entityCollection = entityCollection;
	}
	
	@Override
	public void setFinalPredicate(Function<SearchPredicateFactory, SearchPredicate> finalPredicate) {
		queryContributor.setFinalPredicate(finalPredicate);
	}

	@Override
	public Function<SearchPredicateFactory, SearchPredicate> getFinalPredicate() {
		return queryContributor.getFinalPredicate();
	}

	/**
	 * Gets the entityCollection
	 *
	 * @return the entityCollection
	 */
	public Collection<? extends Class<? extends T>> getEntityCollection() {
		return entityCollection;
	}
	
}
