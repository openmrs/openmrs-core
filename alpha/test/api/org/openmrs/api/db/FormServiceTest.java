package org.openmrs.api.db;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldAnswer;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class FormServiceTest extends TestCase {
	
	protected EncounterService es;
	protected PatientService ps;
	protected UserService us;
	protected ObsService obsService;
	protected FormService formService;
	protected ConceptService conceptService;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("USER-1", "test");
		
		es = context.getEncounterService();
		assertNotNull(es);
		ps = context.getPatientService();
		assertNotNull(ps);
		us = context.getUserService();
		assertNotNull(us);
		obsService = context.getObsService();
		assertNotNull(obsService);
		formService = context.getFormService();
		assertNotNull(formService);
		conceptService = context.getConceptService();
		assertNotNull(conceptService);
		
	}

	public void testFormCreateUpdateDelete() throws Exception {
		
		//testing Form creation
		
		Form form1 = new Form();
		
		String name1 = "form name1";
		String version1 = "version1";
		String descript1 = "descript1";
		String schema_namespace1 = "schema1";
		String def1 = "def1";
		
		form1.setName(name1);
		form1.setVersion(version1);
		form1.setDescription(descript1);
		form1.setSchemaNamespace(schema_namespace1);
		form1.setDefinition(def1);
		
		formService.createForm(form1);
		
		//testing get form 
		
		Form form2 = formService.getForm(form1.getFormId());
		
		String name2 = "form name2";
		String version2 = "version2";
		String descript2 = "descript2";
		String schema_namespace2 = "schema2";
		String def2 = "def2";
		
		form2.setName(name2);
		form2.setVersion(version2);
		form2.setDescription(descript2);
		form2.setSchemaNamespace(schema_namespace2);
		form2.setDefinition(def2);
		
		formService.updateForm(form2);
		
		//testing correct updation
		
		Form form3 = formService.getForm(form2.getFormId());
		
		assertTrue(form1.equals(form3));
		
		assertTrue(form3.getName().equals(name2));
		assertTrue(form3.getVersion().equals(version2));
		assertTrue(form3.getDescription().equals(descript2));
		assertTrue(form3.getSchemaNamespace().equals(schema_namespace2));
		assertTrue(form3.getDefinition().equals(def2));
		
		//testing (un)retiration
		
		formService.retireForm(form2, "reason");
		assertTrue(form2.isRetired());
		assertTrue(form2.getRetiredReason().equals("reason"));
		
		formService.unretireForm(form2);
		assertFalse(form2.isRetired());
		assertFalse(form2.getRetiredReason().equals("reason"));
		
		//testing deletion
		
		formService.deleteForm(form2);
		//formService.deleteForm(form1); //deleting a deleted form
	}
	
	public void xtestFormFieldCreateUpdateDelete() throws Exception {
		
		//testing creation
		
		List<Form> forms = formService.getForms();
		assertNotNull(forms);
		assertTrue(forms.size() > 2);
		
		List<Field> fields = formService.getFields();
		assertNotNull(fields);
		assertTrue(fields.size() > 2);
		
		FormField subFormField1 = null;
		Form form1 = forms.get(1);
		Field field1 = fields.get(1);
		Integer fieldNumber1 = new Integer(1);
		String fieldPart1 = "part1";
		Integer pageNumber1 = new Integer(1);
		Integer minOccurs1 = new Integer(1);
		Integer maxOccurs1 = new Integer(1);
		Boolean required1 = true;
		
		FormField formField1 = new FormField();
		
		formField1.setParent(subFormField1);
		formField1.setForm(form1);
		formField1.setField(field1);
		formField1.setFieldNumber(fieldNumber1);
		formField1.setFieldPart(fieldPart1);
		formField1.setPageNumber(pageNumber1);
		formField1.setMinOccurs(minOccurs1);
		formField1.setMaxOccurs(maxOccurs1);
		formField1.setRequired(required1);
		
		//formService.createFormField(formField1);
		
		//testing updation
		
		FormField formField2 = null; //formService.getFormField(formField1.getFormFieldId());
		
		FormField subFormField2 = null;
		Form form2 = forms.get(2);
		Field field2 = fields.get(2);
		Integer fieldNumber2 = new Integer(2);
		String fieldPart2 = "part2";
		Integer pageNumber2 = new Integer(2);
		Integer minOccurs2 = new Integer(2);
		Integer maxOccurs2 = new Integer(2);
		Boolean required2 = false;
		
		formField2.setParent(subFormField2);
		formField2.setForm(form2);
		formField2.setField(field2);
		formField2.setFieldNumber(fieldNumber2);
		formField2.setFieldPart(fieldPart2);
		formField2.setPageNumber(pageNumber2);
		formField2.setMinOccurs(minOccurs2);
		formField2.setMaxOccurs(maxOccurs2);
		formField2.setRequired(required2);
		
		//formService.updateFormField(formField2);
		
		//testing correct updation
		
		FormField formField3 = null; //formService.getFormField(formField2.getFormFieldId());
		
		assertTrue(formField3.equals(formField2));
		
		assertFalse(formField3.getParent().equals(formField1.getParent()));
		assertFalse(formField3.getForm().equals(formField1.getForm()));
		assertFalse(formField3.getField().equals(formField1.getField()));
		assertFalse(formField3.getFieldNumber().equals(formField1.getFieldNumber()));
		assertFalse(formField3.getFieldPart().equals(formField1.getFieldPart()));
		assertFalse(formField3.getPageNumber().equals(formField1.getPageNumber()));
		assertFalse(formField3.getMinOccurs().equals(formField1.getMinOccurs()));
		assertFalse(formField3.getMaxOccurs().equals(formField1.getMaxOccurs()));
		assertFalse(formField3.isRequired().equals(formField1.isRequired()));
		
		//testing deletion
		
		//formService.deleteFormField(formField3);
		//formService.deleteFormField(formField3);
		//assertNull(formService.getFormField(formField3.getFormFieldId()));
		
	}
	
	public void testFieldCreateModifyDelete() throws Exception {
		
		//testing creation
		
		Concept concept1  = conceptService.getConcept(1);
		String  name1     = "name1";
		String  descript1 = "descript1";
		FieldType fieldtype1 = formService.getFieldTypes().get(1);
		String table1 = "table1";
		String attr1 = "attr1";
		Boolean multi1 = true;
		
		Set<FieldAnswer> answers1 = new HashSet<FieldAnswer>();
		FieldAnswer answer1 = new FieldAnswer();
		answer1.setConcept(conceptService.getConcept(10));
		answers1.add(answer1);
		
		Field field1 = new Field();
		
		field1.setConcept(concept1);
		field1.setName(name1);
		field1.setDescription(descript1);
		field1.setFieldType(fieldtype1);
		field1.setTableName(table1);
		field1.setAttributeName(attr1);
		field1.setSelectMultiple(multi1);
		field1.addAnswer(answer1); //adding to an empty list test
		field1.setAnswers(answers1); //overwriting previous addition
		field1.removeAnswer(answer1); //removing only answer in list
		field1.addAnswer(answer1);   //readding the only answer
		
		formService.updateField(field1);
		
		//testing updation
		
		Field field2 = formService.getField(field1.getFieldId());
		
		Concept concept2  = conceptService.getConcept(2);
		String  name2     = "name2";
		String  descript2 = "descript2";
		FieldType fieldtype2 = formService.getFieldTypes().get(0);
		String table2 = "table2";
		String attr2 = "attr2";
		Boolean multi2 = false;
		
		field2.setConcept(concept2);
		field2.setName(name2);
		field2.setDescription(descript2);
		field2.setFieldType(fieldtype2);
		field2.setTableName(table2);
		field2.setAttributeName(attr2);
		field2.setSelectMultiple(multi2);
		
		assertTrue(field1.getAnswers().equals(field2.getAnswers()));
		
		FieldAnswer answer2 = new FieldAnswer();
		answer2.setConcept(conceptService.getConcept(22));
		field2.addAnswer(answer2);
		field2.removeAnswer(answer1);
		
		formService.updateField(field2);
		
		//testing differences
		
		Field field3 = formService.getField(field2.getFieldId());
		
		assertTrue(field3.equals(field1));
		
		assertTrue(field1.getConcept().equals(concept2));
		assertTrue(field1.getName().equals(name2));
		assertTrue(field1.getDescription().equals(descript2));
		assertTrue(field1.getFieldType().equals(fieldtype2));
		assertTrue(field1.getTableName().equals(table2));
		assertTrue(field1.getAttributeName().equals(attr2));
		assertTrue(field1.getSelectMultiple().equals(multi2));
		assertTrue(field1.getAnswers().equals(field3.getAnswers()));
		
		//testing deletion
		
		formService.deleteField(field3);
		//formService.deleteField(field1);
		
		assertNull(formService.getField(field3.getFieldId()));
	}
	
	public static Test suite() {
		return new TestSuite(FormServiceTest.class, "Basic Form Service functionality");
	}

}
