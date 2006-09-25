package org.openmrs.formentry;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

/**
 * Servlet created to allow FormEntryQueueProcessor to be triggered from the web
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormEntryQueueProcessorServlet extends HttpServlet {

  private static FormEntryQueueProcessor processor;

	private static final long serialVersionUID = -5502982924363644402L;

	// private Log log = LogFactory.getLog(this.getClass());

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();

		Context context = getContext(httpSession);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}

		ServletOutputStream out = response.getOutputStream();

		try {
			getFormEntryQueueProcessor(context).processFormEntryQueue();
			out.print("FormEntry queue processor has started");
		} catch (APIException e) {
			out
					.print("FormEntry queue processor failed to start.  Perhaps it is already running?");
		}

	}


  /**
   *  Get the form entry queue processor.
   *
   *  @return   an instance of the form entry queue processor
   */
  private FormEntryQueueProcessor getFormEntryQueueProcessor(Context context) { 
    synchronized (processor) { 
      if ( processor == null ) { 
        processor = new FormEntryQueueProcessor(context);
      }
    }
    return processor;
  }

  /**
   *  Get the user's context.
   *
   *  @return   an instance of the form entry queue processor
   */
	private Context getContext(HttpSession httpSession) {
		return (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	}

}
