/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.FormUtil;
import org.openmrs.web.WebUtil;

/**
 * A collection of methods used by DWR for access forms, fields, and FormFields. These methods are
 * similar to the {@link FormService} methods and have been chosen to be exposed via dwr to allow
 * for access via javascript.
 */
public class DWRFormService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Finds forms based on search text.
	 * 
	 * @param text the string to search on
	 * @param includeUnpublished true/false whether to include unpublished forms
	 * @return list of {@link FormListItem}s
	 */
	public List<FormListItem> findForms(String text, boolean includeUnpublished) {
		List<FormListItem> forms = new Vector<FormListItem>();
		
		for (Form form : Context.getFormService().getForms(text, includeUnpublished, null, null, null, null, null)) {
			forms.add(new FormListItem(form));
		}
		
		return forms;
	}
	
	/**
	 * Gets a list of FormListItems that correspond to forms. If includueUnpublished is true, all
	 * forms are returned. If false, only published forms are returned.
	 * 
	 * @param includeUnpublished true/false to include unpublished forms
	 * @return list of {@link FormListItem}s
	 */
	public List<FormListItem> getForms(boolean includeUnpublished) {
		List<FormListItem> formListItems = new Vector<FormListItem>();
		
		List<Form> forms = includeUnpublished ? Context.getFormService().getAllForms(false) : Context.getFormService()
		        .getPublishedForms();
		
		for (Form form : forms) {
			formListItems.add(new FormListItem(form));
		}
		
		return formListItems;
	}
	
	public Field getField(Integer fieldId) {
		Field f;
		FormService fs = Context.getFormService();
		f = fs.getField(fieldId);
		return f;
	}
	
	public FormFieldListItem getFormField(Integer formFieldId) {
		FormField f;
		FormService fs = Context.getFormService();
		f = fs.getFormField(formFieldId);
		return new FormFieldListItem(f, Context.getLocale());
	}
	
	public List<FormFieldListItem> getFormFields(Integer formId) {
		List<FormFieldListItem> formFields = new Vector<FormFieldListItem>();
		Form form = Context.getFormService().getForm(formId);
		for (FormField ff : form.getFormFields()) {
			formFields.add(new FormFieldListItem(ff, Context.getLocale()));
		}
		return formFields;
	}
	
	public List<FieldListItem> findFields(String txt) {
		List<FieldListItem> fields = new Vector<FieldListItem>();
		
		for (Field field : Context.getFormService().getFields(txt)) {
			fields.add(new FieldListItem(field, Context.getLocale()));
		}
		
		return fields;
	}
	
	public List<Object> findFieldsAndConcepts(String txt) {
		Locale locale = Context.getLocale();
		
		// return list will contain ConceptListItems and FieldListItems.
		List<Object> objects = new Vector<Object>();
		
		Concept concept = null;
		try {
			Integer i = Integer.valueOf(txt);
			concept = Context.getConceptService().getConcept(i);
		}
		catch (NumberFormatException e) {
			log.error("Error during getting concept", e);
		}
		
		Map<Integer, Boolean> fieldForConceptAdded = new HashMap<Integer, Boolean>();
		
		if (concept != null) {
			for (Field field : Context.getFormService().getFieldsByConcept(concept)) {
				FieldListItem fli = new FieldListItem(field, locale);
				if (!objects.contains(fli)) {
					objects.add(fli);
				}
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			if (!fieldForConceptAdded.containsKey((concept.getConceptId()))) {
				ConceptName cn = concept.getName(locale);
				objects.add(new ConceptListItem(concept, cn, locale));
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			
		}
		
		for (Field field : Context.getFormService().getFields(txt)) {
			FieldListItem fi = new FieldListItem(field, locale);
			if (!objects.contains(fi)) {
				objects.add(fi);
				concept = field.getConcept();
				if (concept != null) {
					fieldForConceptAdded.put(concept.getConceptId(), true);
				}
			}
			
		}
		
		List<ConceptSearchResult> searchResults = Context.getConceptService().getConcepts(txt, locale, false);
		for (ConceptSearchResult searchResult : searchResults) {
			concept = searchResult.getConcept();
			for (Field field : Context.getFormService().getFieldsByConcept(concept)) {
				FieldListItem fli = new FieldListItem(field, locale);
				if (!objects.contains(fli)) {
					objects.add(fli);
				}
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			if (!fieldForConceptAdded.containsKey((concept.getConceptId()))) {
				objects.add(new ConceptListItem(searchResult));
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
		}
		
		Collections.sort(objects, new FieldConceptSort<Object>(locale));
		
		return objects;
	}
	
	public String getJSTree(Integer formId) {
		Form form = Context.getFormService().getForm(formId);
		TreeMap<Integer, TreeSet<FormField>> formFields = FormUtil.getFormStructure(form);
		return generateJSTree(formFields, 0, Context.getLocale());
	}
	
	public Integer[] saveFormField(Integer fieldId, String name, String fieldDesc, Integer fieldTypeId, Integer conceptId,
	        String table, String attr, String defaultValue, boolean multiple, Integer formFieldId, Integer formId,
	        Integer parent, Integer number, String part, Integer page, Integer min, Integer max, boolean required,
	        float sortWeight) {
		
		FormField ff = null;
		Field field = null;
		
		FormService fs = Context.getFormService();
		ConceptService cs = Context.getConceptService();
		
		if (formFieldId != null && formFieldId != 0) {
			ff = fs.getFormField(formFieldId);
		} else {
			ff = new FormField(formFieldId);
		}
		
		ff.setForm(fs.getForm(formId));
		if (parent == null) {
			ff.setParent(null);
		} else if (!parent.equals(ff.getFormFieldId())) {
			ff.setParent(fs.getFormField(parent));
		}
		ff.setFieldNumber(number);
		ff.setFieldPart(part);
		ff.setPageNumber(page);
		ff.setMinOccurs(min);
		ff.setMaxOccurs(max);
		ff.setRequired(required);
		ff.setSortWeight(sortWeight);
		
		log.debug("fieldId: " + fieldId);
		log.debug("formFieldId: " + formFieldId);
		log.debug("parentId: " + parent);
		log.debug("parent: " + ff.getParent());
		
		if (fieldId != null && fieldId != 0) {
			field = fs.getField(fieldId);
		} else {
			field = new Field(fieldId);
		}
		
		if (field == null) {
			log.error("Field is null. Field Id: " + fieldId);
		} else {
			field.setName(name);
			field.setDescription(fieldDesc);
			field.setFieldType(fs.getFieldType(fieldTypeId));
			if (conceptId != null && conceptId != 0) {
				field.setConcept(cs.getConcept(conceptId));
			} else {
				field.setConcept(null);
			}
			field.setTableName(table);
			field.setAttributeName(attr);
			field.setDefaultValue(defaultValue);
			field.setSelectMultiple(multiple);
		}
		
		ff.setField(field);
		fs.saveFormField(ff);
		
		fieldId = ff.getField().getFieldId();
		formFieldId = ff.getFormFieldId();
		
		Integer[] arr = { fieldId, formFieldId };
		
		return arr;
	}
	
	public void deleteFormField(Integer id) {
		if (Context.isAuthenticated()) {
			Context.getFormService().purgeFormField(Context.getFormService().getFormField(id));
			//Context.closeSession();
		}
	}
	
	private String generateJSTree(TreeMap<Integer, TreeSet<FormField>> formFields, Integer current, Locale locale) {
		StringBuilder s = new StringBuilder("");
		
		if (formFields.containsKey(current)) {
			TreeSet<FormField> set = formFields.get(current);
			for (FormField ff : set) {
				s.append(generateFormFieldJavascript(ff, locale));
				if (formFields.containsKey(ff.getFormFieldId())) {
					s.append(generateJSTree(formFields, ff.getFormFieldId(), locale));
				}
			}
		}
		
		return s.toString();
	}
	
	private String generateFormFieldJavascript(FormField ff, Locale locale) {
		
		String parent = "''";
		if (ff.getParent() != null) {
			parent = ff.getParent().getFormFieldId().toString();
		}
		
		Field field = ff.getField();
		Concept concept = new Concept();
		ConceptName conceptName = new ConceptName();
		Boolean isSet = false;
		Boolean isCodedDatatype = false;
		if (field.getConcept() != null) {
			concept = field.getConcept();
			conceptName = concept.getName(locale);
			isSet = concept.isSet();
			isCodedDatatype = concept.getDatatype().isCoded();
		}
		
		if (log.isDebugEnabled()) {
			log.debug("ff.getFormFieldId: " + ff.getFormFieldId());
		}
		
		List<Field> fields = new Vector<Field>();
		fields.add(field);
		
		int size = Context.getFormService().getFormCount(null, null, null, null, null, null, fields);
		
		return "addNode(tree, {formFieldId: " + ff.getFormFieldId() + ", " + "parent: " + parent + ", " + "fieldId: "
		        + field.getFieldId() + ", " + "fieldName: \"" + WebUtil.escapeQuotesAndNewlines(field.getName()) + "\", "
		        + "description: \"" + WebUtil.escapeQuotesAndNewlines(field.getDescription()) + "\", " + "fieldType: "
		        + field.getFieldType().getFieldTypeId() + ", " + "conceptId: " + concept.getConceptId() + ", "
		        + "conceptName: \"" + WebUtil.escapeQuotesAndNewlines(conceptName.getName()) + "\", " + "tableName: \""
		        + field.getTableName() + "\", " + "attributeName: \"" + field.getAttributeName() + "\", "
		        + "defaultValue: \"" + WebUtil.escapeQuotesAndNewlines(field.getDefaultValue()) + "\", "
		        + "selectMultiple: " + field.getSelectMultiple() + ", " + "isCodedDatatype: " + isCodedDatatype + ", "
		        + "numForms: " + size + ", " + "isSet: " + isSet + ", " +

		        "fieldNumber: " + ff.getFieldNumber() + ", " + "fieldPart: \""
		        + (ff.getFieldPart() == null ? "" : WebUtil.escapeQuotesAndNewlines(ff.getFieldPart())) + "\", "
		        + "pageNumber: " + ff.getPageNumber() + ", " + "minOccurs: " + ff.getMinOccurs() + ", " + "maxOccurs: "
		        + ff.getMaxOccurs() + ", " + "isRequired: " + ff.isRequired() + ", " + "sortWeight: " + ff.getSortWeight()
		        + "});";
	}
	
	/**
	 * Sorts loosely on: FieldListItems first, then concepts FieldListItems with higher number of
	 * forms first, then lower Concepts with shorter names before longer names
	 * 
	 * @param <Obj>
	 */
	
	private class FieldConceptSort<Obj extends Object> implements Comparator<Object> {
		
		Locale locale;
		
		FieldConceptSort(Locale locale) {
			this.locale = locale;
		}
		
		public int compare(Object o1, Object o2) {
			if (o1 instanceof FieldListItem && o2 instanceof FieldListItem) {
				FieldListItem f1 = (FieldListItem) o1;
				FieldListItem f2 = (FieldListItem) o2;
				Integer numForms1 = f1.getNumForms();
				Integer numForms2 = f2.getNumForms();
				return numForms2.compareTo(numForms1);
			} else if (o1 instanceof FieldListItem && o2 instanceof ConceptListItem) {
				return -1;
			} else if (o1 instanceof ConceptListItem && o2 instanceof FieldListItem) {
				return 1;
			} else if (o1 instanceof ConceptListItem && o2 instanceof ConceptListItem) {
				ConceptListItem c1 = (ConceptListItem) o1;
				ConceptListItem c2 = (ConceptListItem) o2;
				int length1 = c1.getName().length();
				int length2 = c2.getName().length();
				return Integer.valueOf(length1).compareTo(Integer.valueOf(length2));
			} else {
				return 0;
			}
		}
	}
}
