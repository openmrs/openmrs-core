/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.UserDAO;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HibernateContextDAOTest extends BaseContextSensitiveTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	private SearchSession searchSession;

	private MassIndexer massIndexer;

	private HibernateContextDAO dao;

	@BeforeEach
	public void setUp() {
		searchSession = mock(SearchSession.class);
		massIndexer = mock(MassIndexer.class);
		SearchSessionFactory searchSessionFactory = () -> searchSession;

		dao = new HibernateContextDAO(sessionFactory, searchSessionFactory, userDao);
	}

	private void setStoredIndexVersion(String version) {
		GlobalProperty indexVersion = Context.getAdministrationService()
		        .getGlobalPropertyObject(OpenmrsConstants.GP_SEARCH_INDEX_VERSION);
		if (indexVersion == null) {
			indexVersion = new GlobalProperty(OpenmrsConstants.GP_SEARCH_INDEX_VERSION);
		}
		indexVersion.setPropertyValue(version);
		Context.getAdministrationService().saveGlobalProperty(indexVersion);
	}

	/**
	 * An index left behind by an older version of Lucene cannot be opened by the Lucene we run now, so
	 * a rebuild triggered by a version change has to throw the old index away instead of indexing into
	 * it. Otherwise startup fails on the unreadable index and never reaches the version change.
	 *
	 * @see HibernateContextDAO#setupSearchIndex()
	 */
	@Test
	public void setupSearchIndex_shouldDropTheExistingIndexWhenTheStoredIndexVersionIsStale() throws Exception {
		when(searchSession.massIndexer()).thenReturn(massIndexer);
		when(massIndexer.dropAndCreateSchemaOnStart(true)).thenReturn(massIndexer);
		setStoredIndexVersion("1");

		dao.setupSearchIndex();

		InOrder inOrder = inOrder(massIndexer);
		inOrder.verify(massIndexer).dropAndCreateSchemaOnStart(true);
		inOrder.verify(massIndexer).startAndWait();
		assertEquals(OpenmrsConstants.SEARCH_INDEX_VERSION.toString(),
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_SEARCH_INDEX_VERSION));
	}

	/**
	 * @see HibernateContextDAO#setupSearchIndex()
	 */
	@Test
	public void setupSearchIndex_shouldNotRebuildTheIndexWhenTheStoredIndexVersionMatches() throws Exception {
		setStoredIndexVersion(OpenmrsConstants.SEARCH_INDEX_VERSION.toString());

		dao.setupSearchIndex();

		verify(massIndexer, never()).dropAndCreateSchemaOnStart(anyBoolean());
		verify(massIndexer, never()).startAndWait();
	}
}
