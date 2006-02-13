package org.openmrs.web.controller.patient;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Tribe;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.api.context.Context;
import org.openmrs.util.Helper;
import org.openmrs.web.WebConstants;
import org.openmrs.web.dwr.PatientListItem;
import org.openmrs.web.propertyeditor.TribeEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class NewPatientFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		Context context = (Context) request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
		//binder.registerCustomEditor(java.lang.Integer.class, 
		//		new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(DateFormat.getDateInstance(DateFormat.SHORT), true));
        binder.registerCustomEditor(Tribe.class, new TribeEditor(context));
	}

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		PatientListItem pli = (PatientListItem)obj;

		MessageSourceAccessor msa = getMessageSourceAccessor();
		if (request.getParameter("action") == null || request.getParameter("action").equals(msa.getMessage("general.save"))) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "familyName", "error.name");
			if (pli.getPatientId() == null) {
				// if this is a new patient, they must input an identifier
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "identifier", "error.null");
			}
			else {
				Integer type = Integer.valueOf(request.getParameter("identifierType"));
				PatientIdentifierType pit = context.getFormEntryService().getPatientIdentifierType(type);
				if (pit.hasCheckDigit() && !Helper.isValidCheckDigit(pli.getIdentifier())) {
					errors.rejectValue("identifier", "error.checkdigits");
				}
			}
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "error.null");
		}
		return super.processFormSubmission(request, response, pli, errors);
	}

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
		
		if (context != null && context.isAuthenticated()) {
			FormEntryService ps = context.getFormEntryService();
			PatientListItem p = (PatientListItem)obj;
			String view = getSuccessView();
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			if (request.getParameter("action") != null && request.getParameter("action").equals(msa.getMessage("general.cancel"))) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.canceled");
				return new ModelAndView(new RedirectView(view + "?patientId=" + p.getPatientId()));
			}
			
			Patient patient = new Patient();
			if (p.getPatientId() != null)
				patient = ps.getPatient(p.getPatientId());
			boolean duplicate = false;
			for (PatientName pn : patient.getNames()) {
				if (pn.getGivenName().equals(p.getGivenName()) &&
					pn.getMiddleName().equals(p.getMiddleName()) &&
					pn.getFamilyName().equals(p.getFamilyName()))
					duplicate = true;
			}
			
			if (!duplicate)
				patient.addName(new PatientName(p.getGivenName(), p.getMiddleName(), p.getFamilyName()));
			
			if (p.getAddress1() != "" && p.getAddress2() != "") {
				duplicate = false;
				for (PatientAddress pa : patient.getAddresses()) {
					if (pa.getAddress1() == p.getAddress1() && pa.getAddress2() == p.getAddress2())
						duplicate = true;
				}
				if (!duplicate) {
					PatientAddress pa = new PatientAddress();
					pa.setAddress1(p.getAddress1());
					pa.setAddress2(p.getAddress2());
					patient.addAddress(pa);
						}			}
			
			if (p.getIdentifier().length() > 0) {
				PatientIdentifierType type = ps.getPatientIdentifierType(Integer.valueOf(request.getParameter("identifierType")));
				Location loc = ps.getLocation(Integer.valueOf(request.getParameter("location")));
				patient.addIdentifier(new PatientIdentifier(p.getIdentifier(), type, loc));
			}
			
			String identifier = "";
			if (patient.getIdentifiers().size() > 0) {
				PatientIdentifier pi = (PatientIdentifier)patient.getIdentifiers().toArray()[0];
				identifier = pi.getIdentifier();
			}
			
			patient.setBirthdate(p.getBirthdate());
			patient.setBirthdateEstimated(p.getBirthdateEstimated());
			patient.setGender(p.getGender());
			patient.setMothersName(p.getMothersName());
			Tribe t = ps.getTribe(Integer.valueOf(p.getTribe()));
			patient.setTribe(t);
			
			ps.updatePatient(patient);
						
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
			return new ModelAndView(new RedirectView(view + "?patientId=" + patient.getPatientId()));
		}
		
		return new ModelAndView(new RedirectView(getFormView()));
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
		
		Patient p = null;
		
		if (context != null && context.isAuthenticated()) {
			FormEntryService ps = context.getFormEntryService();
			String patientId = request.getParameter("pId");
	    	if (patientId != null && !patientId.equals("")) {
	    		p = ps.getPatient(Integer.valueOf(patientId));
	    	}
		}
		
		PatientListItem patient = new PatientListItem(p);
		
		if (p == null) {
			patient.setGender(request.getParameter("gender"));
			patient.setGivenName(request.getParameter("name"));
		}
		
        return patient;
    }
    
	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Map<String, Object> map = new HashMap<String, Object>();

		List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
		
		Patient patient = null;
		
		if (context != null && context.isAuthenticated()) {
			FormEntryService ps = context.getFormEntryService();
			String patientId = request.getParameter("pId");
	    	if (patientId != null && !patientId.equals("")) {
	    		patient = ps.getPatient(Integer.valueOf(patientId));
	    		identifiers.addAll(patient.getIdentifiers());
	    	}
		}
		
		map.put("identifiers", identifiers);
		
		return map;
	}   
	
}