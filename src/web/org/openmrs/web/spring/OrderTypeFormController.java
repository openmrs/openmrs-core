package org.openmrs.web.spring;

import java.text.NumberFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.context.Context;
import org.openmrs.web.Constants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class OrderTypeFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    public OrderTypeFormController() {
    	this.setCommandName("orderType");
    	this.setSessionForm(true);
    	this.setFormView("editOrderType.jsp");
    	this.setSuccessView("orderTypes.jsp");
    	this.setValidator(new OrderTypeValidator());
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your session has expired.");
			// response.sendRedirect(request.getContextPath() + "/logout");
			return new ModelAndView("/logout");
		}
		
		OrderType orderType = (OrderType)obj;
		context.getAdministrationService().updateOrderType(orderType);
		
		httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "Order Type saved.");
		
		return new ModelAndView(new RedirectView(getSuccessView()));
	}

    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your session has expired.");
			// response.sendRedirect(request.getContextPath() + "/logout");
			return new ModelAndView(new RedirectView(getSuccessView()));
		}
    	
		OrderService os = context.getOrderService();
    	OrderType orderType = os.getOrderType(Integer.valueOf(request.getParameter("orderTypeId")));
    	
        return orderType;
    }
    
    
}