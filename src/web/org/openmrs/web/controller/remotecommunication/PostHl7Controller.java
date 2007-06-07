package org.openmrs.web.controller.remotecommunication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.HL7Source;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class PostHl7Controller implements Controller {

	protected final Log log = LogFactory.getLog(getClass());
	
	private String formView;
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Boolean success = false;
		if (!Context.isAuthenticated()) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
				Context.authenticate(username, password);
			} else {
				model.put("error", "RemoteCommunication.missingAuthentication");
			}
		}
		if (Context.isAuthenticated()) {
			String message = request.getParameter("hl7Message");
			String hl7Source = request.getParameter("source");
			if (StringUtils.hasText(message) && StringUtils.hasText(hl7Source)) {
				HL7Service service = Context.getHL7Service();
				HL7Source source = service.getHL7Source(hl7Source);
				
				HL7InQueue hl7InQueue = new HL7InQueue();
				hl7InQueue.setHL7Data(message);
				hl7InQueue.setHL7Source(source);
				log.debug("source: " + hl7Source + " , message: " + message);
				Context.getHL7Service().createHL7InQueue(hl7InQueue);
				success = true;
			} else {
				model.put("error", "RemoteCommunication.sourceAndMessageRequired");
			}
		}
		model.put("success", success);
		return new ModelAndView(formView, "model", model);
	}

	public String getFormView() {
		return formView;
	}

	public void setFormView(String formView) {
		this.formView = formView;
	}

}
