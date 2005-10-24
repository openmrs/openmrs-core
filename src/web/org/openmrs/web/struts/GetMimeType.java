package org.openmrs.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.openmrs.MimeType;
import org.openmrs.api.ObsService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;

public class GetMimeType extends DispatchAction {

	/**
	 * @see org.apache.struts.actions.DispatchAction#dispatchMethod(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	protected ActionForward dispatchMethod(ActionMapping mapping,
			ActionForm actionForm, HttpServletRequest request,
			HttpServletResponse response, String str) throws Exception {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your session has expired.");
			// response.sendRedirect(request.getContextPath() + "/logout");
			return mapping.findForward("logout");
		}
		
		ObsService os = context.getObsService();
		
		MimeType mt = os.getMimeType(Integer.valueOf(request.getParameter("mimeTypeId")));
		
		MimeTypeForm mimeTypeForm = new MimeTypeForm();
		
		mimeTypeForm.setMimeType(mt.getMimeType());
		mimeTypeForm.setDescription(mt.getDescription());
		mimeTypeForm.setMimeTypeId(mt.getMimeTypeId());

		request.setAttribute("mimeTypeForm", mimeTypeForm);
		
		return mapping.findForward("display");
	}

}