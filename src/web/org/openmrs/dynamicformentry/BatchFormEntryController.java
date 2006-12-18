package org.openmrs.dynamicformentry;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.UserEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class BatchFormEntryController extends SimpleFormController {

	/** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
	    binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	    binder.registerCustomEditor(java.lang.Double.class,
                new CustomNumberEditor(java.lang.Double.class, true));
	    binder.registerCustomEditor(Location.class, new LocationEditor());
	    binder.registerCustomEditor(User.class, new UserEditor());	    
	}
    
    protected BatchFormEntryModel formBackingObject(HttpServletRequest request) throws Exception {
    	BatchFormEntryModel batchForm = new BatchFormEntryModel();

    	PatientSet ps = Context.getPatientSetService().getMyPatientSet();
    	if (ps == null || ps.size() == 0)
    		throw new RuntimeException("You need a patient set first");
 
    	batchForm.setPatientSet(ps);
    	Form form = Context.getFormService().getForm(Integer.valueOf(request.getParameter("formId")));
    	batchForm.setForm(form);
    	batchForm.getFieldsFromForm();
    	
    	if (request.getParameter("locationId") != null) {
    		Integer locationId = Integer.valueOf(request.getParameter("locationId"));
    		Location l = Context.getEncounterService().getLocation(locationId);
    		batchForm.setLocation(l);
    	} else {
    		// TODO: check all patients for their assigned location / last encounter location, and default to the most common one, or else none
    		Patient p = Context.getPatientService().getPatient(ps.getPatientIds().iterator().next());
    		batchForm.setLocation(p.getHealthCenter());
    	}
    	
    	request.getParameter("providerId");
    	if (request.getParameter("providerId") != null) {
    		Integer providerId = Integer.valueOf(request.getParameter("providerId"));
    		User u = Context.getUserService().getUser(providerId);
    		batchForm.setProvider(u);
    	} else {
    		User u = Context.getFormEntryService().getUserByUsername("Unknown");
    		batchForm.setProvider(u);
    	}
    	
		return batchForm;
    }
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
    		Object comm, BindException errors) throws Exception {

    	throw new RuntimeException("Not Implemented");
    }

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map extraData = new HashMap();
		
		String datePattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase());

		extraData.put("datePattern", datePattern);
		
		return extraData;
	}
    
}
