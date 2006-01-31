package org.openmrs.web.controller.form;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.form.FormSchemaBuilder;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class FormFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			Form form = (Form)obj;
			context.getFormService().updateForm(form);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Form.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Form form = null;
		
		if (context != null && context.isAuthenticated()) {
			FormService fs = context.getFormService();
			String formId = request.getParameter("formId");
	    	if (formId != null)
	    		form = fs.getForm(Integer.valueOf(formId));	
		}
		
		if (form == null)
			form = new Form();
    	
        return form;
    }

	protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Form form = (Form)obj;
		
		String tree = "";
		
		if (context != null && context.isAuthenticated()) {
			TreeMap<Integer, TreeSet<FormField>> formFields = FormSchemaBuilder.getFormStructure(context, form);
			tree = generateTree(formFields, 0);
		}
		
		map.put("tree", tree);
		
		return map;
	}
    
	private String generateTree(TreeMap<Integer, TreeSet<FormField>> formFields, Integer current) {
		
		String s = "";
		
		if (formFields.containsKey(current)) {
			TreeSet<FormField> set = formFields.get(current);
			for (FormField ff : set) {
				s += generateFormFieldHTML(ff);
				if (formFields.containsKey(ff.getFormFieldId())) {
					s += "<div class='indent'>";
					s += generateTree(formFields, ff.getFormFieldId());
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
		s += "<a href='#" + ff.getFieldNumber() + "' onclick='selectField(" + ff.getFieldNumber() + ", this)' class='edit'>";
		if (ff.getField().getFieldType().getFieldTypeId() == 1)
			s += "CONCEPT." + ff.getField().getName() + " " + ff.getField().getFieldId();
		else
			s += ff.getField().getName();
		s += "</a> ";
		s += "<a href='#delete' onclick='deleteField(" + ff.getFieldNumber() + ", this)' class='delete'>delete</a>";
		
		s += "</div>";
    	
    	return s;
    }
}