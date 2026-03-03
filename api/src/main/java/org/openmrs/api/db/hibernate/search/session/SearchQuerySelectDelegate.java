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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.dsl.SimpleBooleanPredicateClausesCollector;
import org.hibernate.search.engine.search.projection.SearchProjection;
import org.hibernate.search.engine.search.projection.dsl.ProjectionFinalStep;
import org.hibernate.search.engine.search.projection.dsl.SearchProjectionFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.engine.search.query.dsl.SearchQuerySelectStep;
import org.hibernate.search.engine.search.query.dsl.SearchQueryWhereStep;
import org.hibernate.search.engine.search.query.dsl.spi.AbstractDelegatingSearchQuerySelectStep;
import org.openmrs.api.db.hibernate.search.SearchQueryContributor;

/**
 * Allows to set final predicate. See {@link SearchQueryContributor}.
 * For internal use only.
 * 
 * @param <R>
 * @param <E>
 * @param <LOS>
 *     
 * @since 2.8.0
 */
class SearchQuerySelectDelegate<R, E, LOS> extends AbstractDelegatingSearchQuerySelectStep<R, E, LOS> implements SearchQueryContributor {

	private Function<SearchPredicateFactory, SearchPredicate> finalPredicate;
	
	public SearchQuerySelectDelegate(SearchQuerySelectStep<?, R, E, LOS, ?, ?> delegate) {
		super(delegate);
	}
	
	@Override
	public void setFinalPredicate(Function<SearchPredicateFactory, SearchPredicate> finalPredicate) {
		this.finalPredicate = finalPredicate;
	}

	@Override
	public Function<SearchPredicateFactory, SearchPredicate> getFinalPredicate() {
		return finalPredicate;
	}

	@Override
	public SearchQueryOptionsStep<?, E, LOS, ?, ?> where(SearchPredicate predicate) {
		if (finalPredicate != null) {
			return super.where((f, root) -> {
				root.add(predicate);
				root.add(finalPredicate.apply(f));
			});
		} else {
			return super.where(predicate);
		}
	}

	@Override
	public SearchQueryOptionsStep<?, E, LOS, ?, ?> where(BiConsumer<? super SearchPredicateFactory, ? super SimpleBooleanPredicateClausesCollector<?>> predicateContributor) {
		if (finalPredicate != null) {
			return super.where((f, root) -> {
				predicateContributor.accept(f, root);
				root.add(finalPredicate.apply(f));
			});
		} else {
			return super.where(predicateContributor);
		}
	}

	@Override
	public SearchQueryOptionsStep<?, E, LOS, ?, ?> where(Function<? super SearchPredicateFactory, ? extends PredicateFinalStep> predicateContributor) {
		if (finalPredicate != null) {
			return super.where((f, root) -> {
				root.add(predicateContributor.apply(f));
				root.add(finalPredicate.apply(f));
			});
		} else {
			return super.where(predicateContributor);
		}
		
	}

	@Override
	public <P> SearchQueryWhereStep<?, P, LOS, ?> select(Class<P> objectClass) {
		return new SearchQueryWhereDelegate<>(super.select(objectClass), finalPredicate);
	}

	@Override
	public SearchQueryWhereStep<?, E, LOS, ?> selectEntity() {
		return new SearchQueryWhereDelegate<>(super.selectEntity(), finalPredicate);
	}

	@Override
	public SearchQueryWhereStep<?, List<?>, LOS, ?> select(SearchProjection<?>... projections) {
		return new SearchQueryWhereDelegate<>(super.select(projections), finalPredicate);
	}

	@Override
	public <P> SearchQueryWhereStep<?, P, LOS, ?> select(Function<? super SearchProjectionFactory<R, E>, ? extends ProjectionFinalStep<P>> projectionContributor) {
		return new SearchQueryWhereDelegate<>(super.select(projectionContributor), finalPredicate);
	}

	@Override
	public SearchQueryWhereStep<?, R, LOS, ?> selectEntityReference() {
		return new SearchQueryWhereDelegate<>(super.selectEntityReference(), finalPredicate);
	}

	@Override
	public <P> SearchQueryWhereStep<?, P, LOS, ?> select(SearchProjection<P> projection) {
		return new SearchQueryWhereDelegate<>(super.select(projection), finalPredicate);
	}
}
