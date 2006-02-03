package org.openmrs.web.dwr;

import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptWord;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.form.FormUtil;
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
	
	public List<Object> findFields(String txt) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Locale locale = context.getLocale();
		
		// return list will contain ConceptListItems and FieldListItems.
		List<Object> objects = new Vector<Object>();
		
		if (context != null) {

			for(Field field : context.getFormService().findFields(txt))
				objects.add(new FieldListItem(field));
			
			List<ConceptWord> conceptWords = context.getConceptService().findConcepts(txt, locale, false);
			for (ConceptWord word : conceptWords) {
				objects.add(new ConceptListItem(word));
				for (Field field : context.getFormService().findFields(word.getConcept()))
					objects.add(new FieldListItem(field));
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
		s += "<a href='#" + ff.getFieldNumber() + "' onmouseover='hoverField(" + ff.getFormFieldId() + ", this)' onmouseout='unHoverField(this)' onclick='selectField(" + ff.getFormFieldId() + ", this)' class='edit'>";
		if (ff.getField().getFieldType().getFieldTypeId() == 1)
			s += "CONCEPT." + ff.getField().getName() + " " + ff.getField().getFieldId();
		else
			s += ff.getField().getName();
		s += "</a> ";
		s += "<a href='#delete' onclick='deleteField(" + ff.getFieldNumber() + ", this)' class='delete'> &nbsp; &nbsp; </a>";
		
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
    	
    	if (opt.length() > 50) {
    		s = "<option value='" + ff.getFormFieldId() + "' title='" + opt + "'>";
    		opt = opt.substring(0, 50) + "...";
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
