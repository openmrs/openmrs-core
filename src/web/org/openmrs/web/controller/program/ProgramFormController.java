package org.openmrs.web.controller.program;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.WorkflowCollectionEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ProgramFormController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
	
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
    	super.initBinder(request, binder);
    	
    	Context context = (Context) request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
    	if (context != null) {
    		binder.registerCustomEditor(Concept.class, new ConceptEditor(context));
    		binder.registerCustomEditor(java.util.Collection.class, "workflows", new WorkflowCollectionEditor(context));
    	}
    }
    
	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	log.debug("called formBackingObject");
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Program program = null;
		
		if (context != null && context.isAuthenticated()) {
			ProgramWorkflowService ps = context.getProgramWorkflowService();
			String programId = request.getParameter("programId");
	    	if (programId != null)
	    		program = ps.getProgram(Integer.valueOf(programId));	
		}
		
		if (program == null)
			program = new Program();
    	
        return program;
    }
    
	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		log.debug("about to save " + obj);
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			Program p = (Program) obj;
			if (p.getProgramId() != null) {
				throw new IllegalArgumentException("You can't modify a Program after it's been created");
			}
			context.getProgramWorkflowService().createProgram(p);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Program.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}
    
}
