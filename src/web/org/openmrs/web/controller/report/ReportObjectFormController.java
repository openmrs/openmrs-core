package org.openmrs.web.controller.report;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.EmptyReportObject;
import org.openmrs.reporting.ReportService;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.UserEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ReportObjectFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        //NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
        binder.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, true));
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor("t", "f", true));
        binder.registerCustomEditor(Character.class, new CharacterEditor(true));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));
        binder.registerCustomEditor(Concept.class, new ConceptEditor(context));
        binder.registerCustomEditor(User.class, new UserEditor(context));
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
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			AbstractReportObject reportObject = (AbstractReportObject)obj;

			context.getAdministrationService().updateReportObject(reportObject);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ReportObject.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}
			

	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind(javax.servlet.http.HttpServletRequest, java.lang.Object)
	 */
	@Override
	protected void onBind(HttpServletRequest request, Object obj) throws Exception {
		// TODO Auto-generated method stub
		//super.onBind(arg0, arg1);
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ReportService rs = (ReportService)context.getReportService();

		AbstractReportObject reportObject = (AbstractReportObject)obj;
		String setType = reportObject.getType();
		String setSubType = reportObject.getSubType();
		if ( setType != null && setSubType != null ) {
			if ( !rs.getReportObjectFactory().isSubTypeOfType(setType, setSubType) ) {
				((AbstractReportObject)obj).setSubType(null);
			}
		}
		
		
		String currentClassName = this.getCommand(request).getClass().getName();
		String correspondingValidatorName = rs.getReportObjectValidatorByClass(currentClassName);
		Validator v = null;
		if ( correspondingValidatorName.length() > 0 ) {
			try {
				Class cls = Class.forName(correspondingValidatorName);
				Constructor ct = cls.getConstructor();
				v = (Validator)ct.newInstance();
				this.setValidator(v);
			} catch ( Throwable t ) {
				//System.out.println("Could not instantiate validator \"" + correspondingValidatorName + "\" for ReportObject class " + currentClassName);
			}
		} else {
			try {
				Class cls = Class.forName(currentClassName + "Validator");
				Constructor ct = cls.getConstructor();
				v = (Validator)ct.newInstance();
				this.setValidator(v);
			} catch ( Throwable t ) {
				log.debug("Could not instantiate default validator \"" + currentClassName + "Validator\" for ReportObject class " + currentClassName + "; Using default validator: " + rs.getDefaultReportObjectValidator());
				Class cls = Class.forName(rs.getDefaultReportObjectValidator());
				Constructor ct = cls.getConstructor();
				v = (Validator)ct.newInstance();
				this.setValidator(v);
			}
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		Map addedData = new HashMap();
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		ReportService rs = context.getReportService();

		Set<String> availableTypes = rs.getReportObjectTypes();
		addedData.put("availableTypes", availableTypes.iterator());
		
		String selectedType = RequestUtils.getStringParameter(request, "type", "");

		//System.out.println("\n\n\nclass is " + this.getCommandClass());

		if ( selectedType.length() > 0 ) {
			Set<String> availableSubTypes = rs.getReportObjectSubTypes(selectedType);
			addedData.put("availableSubTypes", availableSubTypes.iterator());
		}
		
		Map extendedObjectInfo = new HashMap();
		for ( Field field : obj.getClass().getDeclaredFields() ) {
			String fieldName = field.getName();
			Method m = obj.getClass().getMethod("get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1), null);
			Object fieldObj = m.invoke(obj, null);
			extendedObjectInfo.put(fieldName, fieldObj);
		}
		addedData.put("extendedObjectInfo", extendedObjectInfo);
		
		return addedData;
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#isFormChangeRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected boolean isFormChangeRequest(HttpServletRequest request) {
		//System.out.println("in isFormChangeRequest() - submit is " + RequestUtils.getStringParameter(request, "submit", ""));
		String type = RequestUtils.getStringParameter(request, "type", "");
		String subType = RequestUtils.getStringParameter(request, "subType", "");
		String submitted = RequestUtils.getStringParameter(request, "submitted", "");
		return (type.length() == 0 || subType.length() == 0 || submitted.length() == 0 );
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	//System.out.println("in formBackingObject");
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		AbstractReportObject reportObject = null;
		ReportService rs = context.getReportService();

		//context.getClass().getDeclaredField("some").getGenericType().
		
		if (context != null && context.isAuthenticated()) {
			int reportObjectId = RequestUtils.getIntParameter(request, "reportObjectId", 0);
	    	if (reportObjectId > 0) 
	    		reportObject = rs.getReportObject(Integer.valueOf(reportObjectId));	
		}
		
		if (reportObject == null)
			reportObject = new EmptyReportObject();
		
		String presetType = RequestUtils.getStringParameter(request, "type", "");
		if ( presetType.length() > 0 ) reportObject.setType(presetType);
    	
		String presetSubType = RequestUtils.getStringParameter(request, "subType", "");
		if ( presetSubType.length() > 0 && rs.getReportObjectFactory().isSubTypeOfType(presetType, presetSubType) ) {
			reportObject.setSubType(presetSubType);
			String className = rs.getReportObjectClassBySubType(presetSubType);
			if ( className.length() > 0 ) {
				try {
					Class cls = Class.forName(className);
					Constructor ct = cls.getConstructor();
					reportObject = (AbstractReportObject)ct.newInstance();
					reportObject.setType(presetType);
					reportObject.setSubType(presetSubType);
				} catch (Throwable e) {
					//System.out.println("Unable to generate subtype class for ReportObject");
				}
			}
		}
		
		//System.out.println("type is " + presetType + ", and subtype is " + presetSubType + ", and class is " + reportObject.getClass().getName());

		return reportObject;
    }
    
}