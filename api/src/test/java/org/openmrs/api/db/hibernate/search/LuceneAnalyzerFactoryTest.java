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

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.cfg.SearchMapping;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneAnalyzerFactoryTest extends BaseContextSensitiveTest {

	@Indexed
	public class TestObject extends BaseOpenmrsObject {

		public static final long serialVersionUID = 1L;

		@DocumentId
		private Integer objId;

		@Fields({
			@Field(name = "namePhrase", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER), boost = @Boost(10f)),
			@Field(name = "nameExact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(8f)),
			@Field(name = "nameStart", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(4f)),
			@Field(name = "nameAnywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER), boost = @Boost(2f))
		})
		private String name;

		@Override
		public Integer getId() {
			return objId;
		}

		@Override
		public void setId(Integer id) {
			objId = id;
		}
		
		public TestObject(String name) {
			super();
			this.name = name;
		}

	}

	private LuceneAnalyzerFactory laf;

	private GlobalPropertiesTestHelper globalPropertiesTestHelper;

	@Before
	public void setUp() {
		laf = new LuceneAnalyzerFactory();
	}

	/**
	 * @see LuceneAnalyzerFactory#getSearchMapping() 
	 */
	@Test
	public void getSearchMapping_defaults_exactAnalyzer_shouldMatchExactTokens() {
		TestObject[] testObjects = {
			new TestObject("Bouillabaisse Cuttlefish")
		};
	}

}
