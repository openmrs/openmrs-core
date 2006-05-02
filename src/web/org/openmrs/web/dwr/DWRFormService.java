package org.openmrs.web.dwr;

import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptWord;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormUtil;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRFormService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Field getField(Integer fieldId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Field f = new Field();
		if (context != null) {
			FormService fs = context.getFormService();
			f = fs.getField(fieldId);
		}
		return f;
	}
	
	public FormFieldListItem getFormField(Integer formFieldId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		FormField f = new FormField();
		if (context != null) {
			FormService fs = context.getFormService();
			f = fs.getFormField(formFieldId);
		}
		return new FormFieldListItem(f);
	}

	public List<FieldListItem> findFields(String txt) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		List<FieldListItem> fields = new Vector<FieldListItem>();
		
		if (context != null) {
			for(Field field : context.getFormService().findFields(txt))
				fields.add(new FieldListItem(field));
		}
		
		return fields;
	}
	
	public List<Object> findFieldsAndConcepts(String txt) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Locale locale = context.getLocale();
		
		// return list will contain ConceptListItems and FieldListItems.
		List<Object> objects = new Vector<Object>();
		
		if (context != null) {

			Concept concept = null;
			try {
				Integer i = Integer.valueOf(txt);
				concept = context.getConceptService().getConcept(i);
			}
			catch (NumberFormatException e) {}
			
			if (concept != null) {
				objects.add(new ConceptListItem(concept, locale));
				for (Field field : context.getFormService().findFields(concept))
					objects.add(new FieldListItem(field));
			}
			
			List<ConceptWord> conceptWords = context.getConceptService().findConcepts(txt, locale, false);
			for (ConceptWord word : conceptWords) {
				objects.add(new ConceptListItem(word));
				for (Field field : context.getFormService().findFields(word.getConcept()))
					objects.add(new FieldListItem(field));
			}

			for(Field field : context.getFormService().findFields(txt)) {
				FieldListItem fi = new FieldListItem(field);
				if (!objects.contains(fi))
					objects.add(fi);
			}
			
		}
		
		return objects;
	}
	
	public String getHTMLTree(Integer formId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Form form = context.getFormService().getForm(formId);
			TreeMap<Integer, TreeSet<FormField>> formFields = FormUtil.getFormStructure(context, form);
			return generateHTMLTree(formFields, 0);
		}
		return "";
	}
	
	public String getOptionTree(Integer formId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String str = "";
		if (context != null) {
			Form form = context.getFormService().getForm(formId);
			TreeMap<Integer, TreeSet<FormField>> formFields = FormUtil.getFormStructure(context, form);
			str = generateOptionTree(formFields, 0, 0);
		}
		return "<option value=''><option>" + str;	
	}
	
	public void saveFormField(Integer fieldId, String name, String fieldDesc, Integer fieldTypeId, Integer conceptId, String table, String attr, boolean multiple, Integer formFieldId, Integer formId, Integer parent, Integer number, String part, Integer page, Integer min, Integer max, boolean required) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null && context.isAuthenticated()) {
			FormService fs = context.getFormService();
			ConceptService cs = context.getConceptService();
			
			FormField ff;
			if (formFieldId != null && formFieldId != 0)
				ff = fs.getFormField(formFieldId);
			else
				ff = new FormField(formFieldId);
			
			ff.setForm(fs.getForm(formId));
			if (!ff.getFormFieldId().equals(parent))
				ff.setParent(fs.getFormField(parent));
			ff.setFieldNumber(number);
			ff.setFieldPart(part);
			ff.setPageNumber(page);
			ff.setMinOccurs(min);
			ff.setMaxOccurs(max);
			ff.setRequired(required);
			
			log.debug("fieldId: " + fieldId);
			log.debug("formFieldId: " + formFieldId);
			log.debug("parentId: "+ parent);
			log.debug("parent: " + ff.getParent());
			
			Field field;
			if (fieldId != null && fieldId != 0)
				field = fs.getField(fieldId);
			else
				field = new Field(fieldId);
			
			field.setName(name);
			field.setDescription(fieldDesc);
			field.setFieldType(fs.getFieldType(fieldTypeId));
			if (conceptId != null && conceptId != 0)
				field.setConcept(cs.getConcept(conceptId));
			field.setTableName(table);
			field.setAttributeName(attr);
			field.setSelectMultiple(multiple);
		
			ff.setField(field);
			
			fs.updateFormField(ff);
			context.endTransaction();
		}
		
		return;
	}
	
	public void deleteFormField(Integer id) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null && context.isAuthenticated()) {
			context.getFormService().deleteFormField(context.getFormService().getFormField(id));
			context.endTransaction();
		}
	}

	private String generateOptionTree(TreeMap<Integer, TreeSet<FormField>> formFields, Integer current, Integer level) {
		
		String s = "";
		
		if (formFields.containsKey(current)) {
			TreeSet<FormField> set = formFields.get(current);
			for (FormField ff : set) {
				s += generateFormFieldOption(ff, level);
				if (formFields.containsKey(ff.getFormFieldId())) {			
					s += generateOptionTree(formFields, ff.getFormFieldId(), level+1);
				}
			}
		}
		
		return s;
	}

	
	private String generateHTMLTree(TreeMap<Integer, TreeSet<FormField>> formFields, Integer current) {
		
		String s = "";
		
		if (formFields.containsKey(current)) {
			TreeSet<FormField> set = formFields.get(current);
			for (FormField ff : set) {
				s += generateFormFieldHTML(ff);
				if (formFields.containsKey(ff.getFormFieldId())) {
					s += "<div class='indent'>";
					s += generateHTMLTree(formFields, ff.getFormFieldId());
					s += "</div>";
				}
			}
		}
		
		return s;
	}
    
    private String generateFormFieldHTML(FormField ff) {
    	String s = "<div class='formField'>";
    	
    	if (ff.getFieldNumber() != null)
    		s += ff.getFieldNumber() + ". ";
    	if (ff.getFieldPart() != null)
    		s += ff.getFieldPart() + ". ";
    	if ((ff.getMinOccurs() != null && ff.getMinOccurs() > 0) || (ff.getMaxOccurs() != null && ff.getMaxOccurs() != 1)){
    		s += " (";
    		if (ff.getMinOccurs() == null)
    			s += "0";
    		else
    			s += ff.getMinOccurs().toString();
    		s += "..";
    		if (ff.getMaxOccurs() == -1)
    			s += "n";
    		else {
    			if (ff.getMaxOccurs() == null)
    				s += "0";
    			else
    				s += ff.getMaxOccurs();
    		}
    		s += ") ";
    	}
		if (ff.isRequired())
			s += "<span class='required'> * </span>";
		s += "<a href='#" + ff.getFormFieldId() + "' onmouseover='hoverField(" + ff.getFormFieldId() + ", this)' onmouseout='unHoverField(this)' onclick='return selectField(" + ff.getFormFieldId() + ", this)' class='edit'>";
		if (ff.getField().getConcept() != null)
			s += ff.getField().getName() + " (" + ff.getField().getConcept().getConceptId() + ")";
		else
			s += ff.getField().getName();
		s += "</a> ";
		s += "<a href='#delete' onclick='return deleteField(" + ff.getFormFieldId() + ", this)' class='delete'> &nbsp; &nbsp; </a>";
		
		s += "</div>";
    	
    	return s;
    }

    private String generateFormFieldOption(FormField ff, Integer level) {
    	
    	String indent = "";
		for (int i=0; i<level; i++) 
			indent += "&nbsp; ";
		
    	String opt = indent;
    	if (ff.getFieldNumber() != null)
    		opt += ff.getFieldNumber() + ". ";
    	if (ff.getFieldPart() != null)
    		opt += ff.getFieldPart() + ". ";
    	opt += ff.getField().getName();
    	
    	String s = "";
    	
    	if (opt.length() > 42) {
    		s = "<option value='" + ff.getFormFieldId() + "' title='" + opt + "'>";
    		opt = opt.substring(0, 42) + "...";
    		s += opt;
    		s += "</option>";
    	}
    	else {
    		s = "<option value='" + ff.getFormFieldId() + "'>";
    		s += opt;
    		s += "</option>";
    	}
    	
    	return s;
    }
    
}
