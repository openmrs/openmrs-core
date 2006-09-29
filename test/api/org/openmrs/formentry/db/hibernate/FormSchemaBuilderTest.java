package org.openmrs.formentry.db.hibernate;

import junit.framework.TestCase;

import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.formentry.FormSchemaBuilder;

public class FormSchemaBuilderTest extends TestCase {

	
	public void testClass() throws Exception {
		
		HibernateUtil.startup();
		
		Context.authenticate("admin", "test");
		
		Form form = Context.getFormService().getForm(17);
		String payload = new FormSchemaBuilder(form).getSchema();
		
		System.out.println(payload);
		
		HibernateUtil.shutdown();
	}
	
}