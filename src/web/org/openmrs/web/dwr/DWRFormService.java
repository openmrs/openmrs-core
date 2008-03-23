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
import org.openmrs.ConceptWord;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.FormUtil;
import org.openmrs.web.WebUtil;


public class DWRFormService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public List<FormListItem> findForms(String text, boolean includeUnpublished) {
		List<FormListItem> forms = new Vector<FormListItem>();
		
		for(Form form : Context.getFormService().findForms(text, includeUnpublished, false)) {
			forms.add(new FormListItem(form));
		}
		
		return forms;
	}
	
	public List<FormListItem> getForms(boolean includeUnpublished) {
		List<FormListItem> forms = new Vector<FormListItem>();
		
		for(Form form : Context.getFormService().getForms(!includeUnpublished)) {
			forms.add(new FormListItem(form));
		}
		
		return forms;
	}
	
	public Field getField(Integer fieldId) {
		Field f = new Field();
		FormService fs = Context.getFormService();
		f = fs.getField(fieldId);
		return f;
	}
	
	public FormFieldListItem getFormField(Integer formFieldId) {
		FormField f = new FormField();
		FormService fs = Context.getFormService();
		f = fs.getFormField(formFieldId);
		return new FormFieldListItem(f, Context.getLocale());
	}
	
	public List<FormFieldListItem> getFormFields(Integer formId) {
		List<FormFieldListItem> formFields = new Vector<FormFieldListItem>();
		Form form = Context.getFormService().getForm(formId);
		for (FormField ff : form.getFormFields())
			formFields.add(new FormFieldListItem(ff, Context.getLocale()));
		return formFields;
	}

	public List<FieldListItem> findFields(String txt) {
		List<FieldListItem> fields = new Vector<FieldListItem>();
		
		for(Field field : Context.getFormService().findFields(txt))
			fields.add(new FieldListItem(field, Context.getLocale()));
		
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
		catch (NumberFormatException e) {}
		
		Map<Integer, Boolean> fieldForConceptAdded = new HashMap<Integer, Boolean>();
		
		if (concept != null) {
			for (Field field : Context.getFormService().findFields(concept)) {
				FieldListItem fli = new FieldListItem(field, locale); 
				if (!objects.contains(fli))
					objects.add(fli);
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			if (!fieldForConceptAdded.containsKey((concept.getConceptId()))) {
				objects.add(new ConceptListItem(concept, locale));
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			
		}
		
		for(Field field : Context.getFormService().findFields(txt)) {
			FieldListItem fi = new FieldListItem(field, locale);
			if (!objects.contains(fi)) {
				objects.add(fi);
				concept = field.getConcept();
				if (concept != null)
					fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			
		}
		
		List<ConceptWord> conceptWords = Context.getConceptService().findConcepts(txt, locale, false);
		for (ConceptWord word : conceptWords) {
			concept = word.getConcept();
			for (Field field : Context.getFormService().findFields(concept)) {
				FieldListItem fli = new FieldListItem(field, locale);
				if (!objects.contains(fli))
					objects.add(fli);
				fieldForConceptAdded.put(concept.getConceptId(), true);
			}
			if (!fieldForConceptAdded.containsKey((concept.getConceptId()))) {
				objects.add(new ConceptListItem(word));
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
	
	public Integer[] saveFormField(Integer fieldId, String name, String fieldDesc, Integer fieldTypeId, Integer conceptId, String table, String attr, 
			String defaultValue, boolean multiple, Integer formFieldId, Integer formId, Integer parent, Integer number, String part, Integer page, Integer min, Integer max, boolean required, float sortWeight) {
		
		FormField ff = null;
		Field field = null;
		
		FormService fs = Context.getFormService();
		ConceptService cs = Context.getConceptService();
		
		
		if (formFieldId != null && formFieldId != 0)
			ff = fs.getFormField(formFieldId);
		else
			ff = new FormField(formFieldId);
		
		ff.setForm(fs.getForm(formId));
		if (parent == null)
			ff.setParent(null);
		else if (!parent.equals(ff.getFormFieldId()))
			ff.setParent(fs.getFormField(parent));
		ff.setFieldNumber(number);
		ff.setFieldPart(part);
		ff.setPageNumber(page);
		ff.setMinOccurs(min);
		ff.setMaxOccurs(max);
		ff.setRequired(required);
		ff.setSortWeight(sortWeight);
		
		log.debug("fieldId: " + fieldId);
		log.debug("formFieldId: " + formFieldId);
		log.debug("parentId: "+ parent);
		log.debug("parent: " + ff.getParent());
		
		if (fieldId != null && fieldId != 0)
			field = fs.getField(fieldId);
		else
			field = new Field(fieldId);
		
		if (field == null) {
			log.error("Field is null. Field Id: " + fieldId);
		}
		
		field.setName(name);
		field.setDescription(fieldDesc);
		field.setFieldType(fs.getFieldType(fieldTypeId));
		if (conceptId != null && conceptId != 0)
			field.setConcept(cs.getConcept(conceptId));
		else
			field.setConcept(null);
		field.setTableName(table);
		field.setAttributeName(attr);
		field.setDefaultValue(defaultValue);
		field.setSelectMultiple(multiple);
	
		ff.setField(field);
		fs.updateFormField(ff);
		
		fieldId = ff.getField().getFieldId();
		formFieldId = ff.getFormFieldId();
		
		Integer[] arr = {fieldId, formFieldId};
		
		return arr;
	}
	
	public void deleteFormField(Integer id) {
		if (Context.isAuthenticated()) {
			Context.getFormService().deleteFormField(Context.getFormService().getFormField(id));
			//Context.closeSession();
		}
	}
    
    private String generateJSTree(TreeMap<Integer, TreeSet<FormField>> formFields, Integer current, Locale locale) {
		String s = "";
		
		if (formFields.containsKey(current)) {
			TreeSet<FormField> set = formFields.get(current);
			for (FormField ff : set) {
				s += generateFormFieldJavascript(ff, locale);
				if (formFields.containsKey(ff.getFormFieldId())) {
					s += generateJSTree(formFields, ff.getFormFieldId(), locale);
				}
			}
		}
		
		return s;
	}
    
    private String generateFormFieldJavascript(FormField ff, Locale locale) {
    	
    	String parent = "''";
		if (ff.getParent() != null)
			parent = ff.getParent().getFormFieldId().toString();
		
		Field field = ff.getField();
		Concept concept = new Concept();
		ConceptName conceptName = new ConceptName();
		Boolean isSet = false;
		if (field.getConcept() != null) {
			concept = field.getConcept();
			conceptName = concept.getName(locale);
			isSet = concept.isSet();
		}
		
		if (log.isDebugEnabled())
			log.debug("ff.getFormFieldId: " + ff.getFormFieldId());
		
    	return "addNode(tree, {formFieldId: " + ff.getFormFieldId() + ", " + 
    					"parent: " + parent + ", " + 
    					"fieldId: " + field.getFieldId() + ", " + 
    					"fieldName: \"" + WebUtil.escapeQuotesAndNewlines(field.getName()) + "\", " + 
    					"description: \"" + WebUtil.escapeQuotesAndNewlines(field.getDescription()) + "\", " +
    					"fieldType: " + field.getFieldType().getFieldTypeId() + ", " + 
    					"conceptId: " + concept.getConceptId() + ", " + 
						"conceptName: \"" + WebUtil.escapeQuotesAndNewlines(conceptName.getName()) + "\", " + 
    					"tableName: \"" + field.getTableName() + "\", " + 
    					"attributeName: \"" + field.getAttributeName() + "\", " + 
    					"defaultValue: \"" + WebUtil.escapeQuotesAndNewlines(field.getDefaultValue()) + "\", " + 
    					"selectMultiple: " + field.getSelectMultiple() + ", " + 
    					"numForms: " + field.getForms().size() + ", " + 
    					"isSet: " + isSet + ", " +
    						
    					"fieldNumber: " + ff.getFieldNumber() + ", " + 
    					"fieldPart: \"" + (ff.getFieldPart() == null ? "" : WebUtil.escapeQuotesAndNewlines(ff.getFieldPart())) + "\", " + 
    					"pageNumber: " + ff.getPageNumber() + ", " + 
    					"minOccurs: " + ff.getMinOccurs() + ", " + 
    					"maxOccurs: " + ff.getMaxOccurs() + ", " + 
    					"isRequired: " + ff.isRequired() + ", " + 
    					"sortWeight: " + ff.getSortWeight() + "});";
    }
    
    /**
     * Sorts loosely on:
     *   FieldListItems first, then concepts
     *   FieldListItems with higher number of forms first, then lower
     *   Concepts with shorter names before longer names
     * @author bwolfe
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
				FieldListItem f1 = (FieldListItem)o1;
				FieldListItem f2 = (FieldListItem)o2;
				Integer numForms1 = f1.getNumForms();
				Integer numForms2 = f2.getNumForms();
				return numForms2.compareTo(numForms1);
			}
			else if (o1 instanceof FieldListItem && o2 instanceof ConceptListItem) {
				return -1;
			}
			else if (o1 instanceof ConceptListItem && o2 instanceof FieldListItem) {
				return 1;
			}
			else if (o1 instanceof ConceptListItem && o2 instanceof ConceptListItem) {
				ConceptListItem c1 = (ConceptListItem)o1;
				ConceptListItem c2 = (ConceptListItem)o2;
				int length1 = c1.getName().length();
				int length2 = c2.getName().length();
				return new Integer(length1).compareTo(new Integer(length2));
			}
			else
				return 0;
		}
    }
}
