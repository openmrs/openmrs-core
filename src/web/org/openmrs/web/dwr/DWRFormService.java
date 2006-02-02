package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRFormService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Field getField(Integer fieldId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		FormService fs = context.getFormService();
		Field f = fs.getField(fieldId);
		
		return f;
	}
	
	public FormField getFormField(Integer formFieldId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		FormService fs = context.getFormService();
		FormField f = fs.getFormField(formFieldId);
		
		return f;
	}
	

}
