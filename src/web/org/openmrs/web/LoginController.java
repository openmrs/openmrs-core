package org.openmrs.web;

import javax.servlet.ServletException;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class LoginController extends SimpleFormController {

	public ModelAndView onSubmit(Object command) throws ServletException {
		return new ModelAndView(new RedirectView(getSuccessView(), true));
	}

}
