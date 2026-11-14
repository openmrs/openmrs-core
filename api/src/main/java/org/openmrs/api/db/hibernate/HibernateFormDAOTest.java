package org.openmrs.api.db.hibernate;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.api.db.FormDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {

	@Autowired
	private FormDAO formDAO;

	@Test
	public void getAllForms_shouldExcludeRetiredFormsWhenIncludeRetiredIsFalse() {
		// when
		List<Form> forms = formDAO.getAllForms(false);

		// then
		assertNotNull(forms);
		assertFalse(forms.isEmpty());

		for (Form form : forms) {
			assertFalse(form.getRetired());
		}
	}
}

