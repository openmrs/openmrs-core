package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {
	private HibernateFormDAO dao;

	@Before
	public void setUp() throws Exception {
		if (dao == null) {
			dao = (HibernateFormDAO) applicationContext.getBean("formDAO");
		}
	}

	@Test
	public void getFormCriteria_test() {
		String partialName = null;
		Boolean published = null;
		Boolean retired = null;
		Collection<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Collection<FormField> containingAnyFormField = new ArrayList<FormField>();
		Collection<FormField> containingAllFormFields = new ArrayList<FormField>();
		Collection<Field> fields = new ArrayList<Field>();

		try {
			List<Form> f = dao.getForms(partialName, published, encounterTypes,
					retired, containingAnyFormField, containingAllFormFields,
					fields);
			ArrayList<Integer> it = new ArrayList<Integer>();

			if (containingAnyFormField.isEmpty()) {
				Assert.assertEquals(1, f.size());
			} else {
				for (FormField ff : containingAnyFormField) {
					it.add(ff.getForm().getFormId());
				}
				for (Form fo : f) {
					Assert.assertEquals(true, it.contains(fo.getFormId()));
				}
			}
			
		} catch (DAOException ex) {
			System.out.println(ex.getMessage());
		}

	}
}
