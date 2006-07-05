package org.openmrs.web.controller.widget;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.util.Helper;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Legal arguments:
 *	size=compact -> a one-line search box which takes you to another page  
 *	size=full	 -> a search box that pulls up results in-place via ajax.
 * @author djazayeri
 */
public class FindOnePatientWidget implements Controller {

	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Map<String, String> arguments = Helper.parseParameterList(request.getParameter("arguments"));
		String size = arguments.get("size");
		if (size == null) {
			size = "compact";
		}
		
		Map model = new HashMap();
		model.put("authenticated", context != null && context.getAuthenticatedUser() != null);
		model.put("size", size);
		
		return new ModelAndView("/widget/findOnePatientWidget", "model", model);
	}
}
