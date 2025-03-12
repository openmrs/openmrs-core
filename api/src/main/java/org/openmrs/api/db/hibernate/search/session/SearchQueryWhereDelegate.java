/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.session;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.dsl.SimpleBooleanPredicateClausesCollector;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.engine.search.query.dsl.SearchQueryWhereStep;

/**
 * Used by {@link SearchQuerySelectDelegate}.
 * For internal use only.
 * 
 * @since 2.8.0
 */
class SearchQueryWhereDelegate<
	N extends SearchQueryOptionsStep<?, H, LOS, ?, ?>,
	H,
	LOS,
	PDF extends SearchPredicateFactory> implements SearchQueryWhereStep<N, H, LOS, PDF> {

	private final SearchQueryWhereStep<N, H, LOS, PDF> delegate;

	private final Function<SearchPredicateFactory, SearchPredicate> finalPredicate;
	
	public SearchQueryWhereDelegate(SearchQueryWhereStep<N, H, LOS, PDF> delegate, 
									Function<SearchPredicateFactory, SearchPredicate> finalPredicate) {
		this.delegate = delegate;
		this.finalPredicate = finalPredicate;
	}

	@Override
	public N where(SearchPredicate predicate) {
		return delegate.where((f, root) -> {
			root.add(predicate);
			if (finalPredicate != null) {
				root.add(finalPredicate.apply(f));
			}
		});
	}

	@Override
	public N where(Function<? super PDF, ? extends PredicateFinalStep> predicateContributor) {
		return  delegate.where((f, root) -> {
			root.add(predicateContributor.apply(f));
			if (finalPredicate != null) {
				root.add(finalPredicate.apply(f));
			}
		});
	}

	@Override
	public N where(BiConsumer<? super PDF, ? super SimpleBooleanPredicateClausesCollector<?>> predicateContributor) {
		return delegate.where((f, root) -> {
			predicateContributor.accept(f, root);
			if (finalPredicate != null) {
				root.add(finalPredicate.apply(f));
			}
		});
	}
}
