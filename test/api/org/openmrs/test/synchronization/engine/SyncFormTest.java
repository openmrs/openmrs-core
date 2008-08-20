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
package org.openmrs.test.synchronization.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.springframework.test.annotation.NotTransactional;

/**
 *
 */
public class SyncFormTest extends SyncBaseTest {

	/**
	 * @see org.openmrs.synchronization.engine.SyncBaseTest#getInitialDataset()
	 */
	@Override
    public String getInitialDataset() {
	    return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
    }

	@Test
    @NotTransactional
	public void shouldEditFormMetadata() throws Exception {
		runSyncTest(new SyncTestHelper() {
			String newDescription = "Awesome new description";
			public void runOnChild() {
				Form form = Context.getFormService().getForm(1);
				form.setDescription(newDescription);
				Context.getFormService().updateForm(form);
			}
			public void runOnParent() {
				Form form = Context.getFormService().getForm(1);
				assertEquals("Description did not change", form.getDescription(), newDescription);
			}
		});
	}
	
	@Test
    @NotTransactional
	public void shouldDuplicateForm() throws Exception {
		runSyncTest(new SyncTestHelper() {
			String newName = "A new form";
			String newDescription = "Awesome new description";
			int numFields;
			int numFormsBefore;
			public void runOnChild() {
				numFormsBefore = Context.getFormService().getForms().size();
				Form form = Context.getFormService().getForm(1);
				numFields = form.getFormFields().size();
				assertNotSame("Form should have some fields", numFields, 0);
				Form dup = Context.getFormService().duplicateForm(form);
				dup.setName(newName);
				dup.setDescription(newDescription);
				Context.getFormService().updateForm(dup);
			}
			public void runOnParent() {
				assertEquals("Should now have N+1 forms", Context.getFormService().getForms().size(), numFormsBefore + 1);
				Form form = null;
				for (Form f : Context.getFormService().getForms())
					if (f.getName().equals(newName))
						form = f;
				assertNotNull("Couldn't find form", form);
				assertEquals("Name is wrong", form.getName(), newName);
				assertEquals("Description is wrong", form.getDescription(), newDescription);
				assertEquals("Wrong fields", form.getFormFields().size(), numFields);
			}
		});
	}

	@Test
    @NotTransactional
	public void shouldAddFieldToForm() throws Exception {
		runSyncTest(new SyncTestHelper() {
			FormService fs = Context.getFormService();
			int numFieldsBefore;
			Concept weight = Context.getConceptService().getConceptByName("WEIGHT");
			String name = "LookAtMe";
			public void runOnChild() {
				Field field = new Field();
				field.setConcept(weight);
				field.setFieldType(fs.getFieldType(1));
				field.setName(name);
				fs.createField(field);
				
				Form form = Context.getFormService().getForm(1);
				numFieldsBefore = form.getFormFields().size();
				FormField ff = new FormField();
				ff.setField(field);
				ff.setFieldNumber(99);
				ff.setPageNumber(55);
				form.addFormField(ff);
				fs.updateForm(form);
			}
			public void runOnParent() {
				Form form = Context.getFormService().getForm(1);
				assertEquals("Added new field", form.getFormFields().size(), numFieldsBefore + 1);
				int numTheSame = 0;
				for (FormField ff : form.getFormFields()) {
					Field f = ff.getField();
					
					if ( (f.getConcept() != null && f.getConcept().equals(weight)) &&
							(ff.getFieldNumber() != null && ff.getFieldNumber() == 99) &&
							(ff.getPageNumber() != null && ff.getPageNumber() == 55) &&
							name.equals(f.getName()) ) {
						++numTheSame;
					}
				}
				assertEquals(numTheSame, 1);
			}
		});
	}

}
