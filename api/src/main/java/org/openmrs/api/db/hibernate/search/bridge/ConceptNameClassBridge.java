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
package org.openmrs.api.db.hibernate.search.bridge;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.openmrs.ConceptName;

/**
 * Indexes not voided concept names in locale fields e.g. name_en_US and name_en.
 */
public class ConceptNameClassBridge implements FieldBridge {
	
	/**
	 * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object,
	 *      org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
	 */
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		ConceptName conceptName = (ConceptName) value;
		
		if (conceptName.isVoided()) {
			return;
		}
		
		String fieldValue = conceptName.getName();
		if (!StringUtils.isBlank(conceptName.getLocale().getCountry())) {
			String fieldName = name + "_" + conceptName.getLocale();
			addField(fieldName, fieldValue, document, luceneOptions);
		}
		
		String fieldName = name + "_" + conceptName.getLocale().getLanguage();
		addField(fieldName, fieldValue, document, luceneOptions);
	}
	
	protected void addField(String fieldName, String fieldValue, Document document, LuceneOptions luceneOptions) {
		Field field = new Field(fieldName, fieldValue, luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions
		        .getTermVector());
		
		field.setBoost(luceneOptions.getBoost());
		document.add(field);
	}
}
