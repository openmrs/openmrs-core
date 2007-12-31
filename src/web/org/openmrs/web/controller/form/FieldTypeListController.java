package org.openmrs.web.controller.form;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.FieldType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class FieldTypeListController extends SimpleFormController {
	
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
		

		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] fieldTypeList = request.getParameterValues("fieldTypeId");
			AdministrationService as = Context.getAdministrationService();
			FormService rs = Context.getFormService();
			//FieldTypeService rs = new TestFieldTypeService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textFieldType = msa.getMessage("FieldType.fieldType");
			String noneDeleted = msa.getMessage("FieldType.nonedeleted");
			if ( fieldTypeList != null ) {
				for (String p : fieldTypeList) {
					//TODO convenience method deleteFieldType(Integer) ??
					try {
						as.deleteFieldType(rs.getFieldType(Integer.valueOf(p)));
						if (!success.equals("")) success += "<br/>";
						success += textFieldType + " " + p + " " + deleted;
					}
					catch (APIException e) {
						log.warn("Error deleting field type", e);
						if (!error.equals("")) error += "<br/>";
						error += textFieldType + " " + p + " " + notDeleted;
					}
				}
			} else {
				success += noneDeleted;
			}
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
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

		//default empty Object
		List<FieldType> fieldTypeList = new Vector<FieldType>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			FormService fs = Context.getFormService();
			//FieldTypeService rs = new TestFieldTypeService();
	    	fieldTypeList = fs.getFieldTypes();
		}
    	
        return fieldTypeList;
    }
    
}