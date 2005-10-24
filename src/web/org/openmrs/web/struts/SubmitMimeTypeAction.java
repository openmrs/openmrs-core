package org.openmrs.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.openmrs.MimeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

public class SubmitMimeTypeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		MimeTypeForm mimeTypeForm = (MimeTypeForm) form;
		HttpSession httpSession = request.getSession();

		Context context = (Context) httpSession
				.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			// httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your
			// session has expired.");
			// response.sendRedirect(request.getContextPath() + "/logout");
			return mapping.findForward("failure");
		}

		AdministrationService as = context.getAdministrationService();
		
		MimeType mimeType = new MimeType();
		mimeType.setDescription(mimeTypeForm.getDescription());
		mimeType.setMimeType(mimeTypeForm.getMimeType());
		mimeType.setMimeTypeId(mimeTypeForm.getMimeTypeId());
		
		as.updateMimeType(mimeType);
		
		return mapping.findForward("success");
	}


}