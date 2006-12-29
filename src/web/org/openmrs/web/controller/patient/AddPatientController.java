package org.openmrs.web.controller.patient;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.web.dwr.PatientListItem;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class AddPatientController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    @Override
    protected List<PatientListItem> formBackingObject(HttpServletRequest request) throws ServletException {
		
    	List<PatientListItem> patientList = new Vector<PatientListItem>();
    	
    	if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			
			Integer userId = Context.getAuthenticatedUser().getUserId();
			
			String name = ServletRequestUtils.getStringParameter(request, "name", "");
			String birthyear = ServletRequestUtils.getStringParameter(request, "birthyear", "");
			String age = ServletRequestUtils.getStringParameter(request, "age", "");
			String gender = ServletRequestUtils.getStringParameter(request, "gndr", "");
			
			if (!name.equals("") || !birthyear.equals("") || !age.equals("") || !gender.equals("")) {
					
				log.info(userId + "|" + name + "|" + birthyear + "|" + age + "|" + gender);
				
				Integer d = null;
				birthyear = birthyear.trim();
				age = age.trim();
				if (birthyear.length() > 3)
					d = Integer.valueOf(birthyear);
				else if (age.length() > 0) {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					d = c.get(Calendar.YEAR);
					d = d - Integer.parseInt(age);
				}
				
				if (gender.length() < 1)
					gender = null;
				
				List<Patient> patients = new Vector<Patient>();
				patients.addAll(ps.getSimilarPatients(name, d, gender));
				
				patientList = new Vector<PatientListItem>(patients.size());
				for (Patient p : patients) {
					patientList.add(new PatientListItem(p));
				}
			}
			
		}
		
		return patientList;
    }
    
    /**
     * Prepares the form view
     */
    public ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
    	
    	ModelAndView mav = super.showForm(request, response, errors);
    	
    	Object o = mav.getModel().get(this.getCommandName());
    	
    	List patientList = (List) o;
    	
    	if (patientList.size() < 1) {
    		String name = ServletRequestUtils.getStringParameter(request, "name", "");
			String birthyear = ServletRequestUtils.getStringParameter(request, "birthyear", "");
			String age = ServletRequestUtils.getStringParameter(request, "age", "");
			String gender = ServletRequestUtils.getStringParameter(request, "gndr", "");
			
			if (!name.equals("") || !birthyear.equals("") || !age.equals("") || !gender.equals("")) {
				mav.clear();
				mav.setView(new RedirectView("newPatient.form?name=" + name + "&birthyear=" + birthyear + "&age=" + age + "&gndr=" + gender));
			}
    	}
    	
    	return mav;
    	
    }
    
}