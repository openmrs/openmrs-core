package org.openmrs.dynamicformentry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.UserEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class PatientFormEntryController extends SimpleFormController {

	/** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    DateFormat dateFormat;
    
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		dateFormat = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()), Context.getLocale());
		
	    binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	    binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, true));
	    binder.registerCustomEditor(Location.class, new LocationEditor());
	    binder.registerCustomEditor(User.class, new UserEditor());
	    binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(dateFormat, true));
	}
    
    protected PatientFormEntryModel formBackingObject(HttpServletRequest request) throws Exception {
    	log.debug("session: " + request.getSession() + " (" + isSessionForm() + ")");
    	PatientFormEntryModel patientForm = new PatientFormEntryModel();
    	patientForm.setPatient(Context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId"))));
    	if (request.getParameter("formId") != null) {
    		Form form = Context.getFormService().getForm(Integer.valueOf(request.getParameter("formId")));
    		patientForm.setForm(form);
    		patientForm.getFieldsFromForm();
    	}
    	if (request.getParameter("locationId") != null) {
    		Integer locationId = Integer.valueOf(request.getParameter("locationId"));
    		Location l = Context.getPatientService().getLocation(locationId);
    		patientForm.setLocation(l);
    	}
    	request.getParameter("providerId");
    	if (request.getParameter("providerId") != null) {
    		Integer providerId = Integer.valueOf(request.getParameter("providerId"));
    		User u = Context.getUserService().getUser(providerId);
    		patientForm.setProvider(u);
    	}
		return patientForm;
    }
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
    		Object comm, BindException errors) throws Exception {

    	PatientFormEntryModel command = (PatientFormEntryModel) comm;
    	log.debug("command is " + command);
    	String view = getFormView();
    
    	view = getSuccessView();
		log.debug("redirecting to " + view);
    	return new ModelAndView(new RedirectView(view));
    }
	
}
