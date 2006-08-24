package org.openmrs.web.controller.order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.DrugEditor;
import org.openmrs.web.propertyeditor.EncounterEditor;
import org.openmrs.web.propertyeditor.OrderTypeEditor;
import org.openmrs.web.propertyeditor.UserEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class OrderDrugFormController extends SimpleFormController {
	
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
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        binder.registerCustomEditor(OrderType.class, new OrderTypeEditor(context));
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor("t", "f", true));
        binder.registerCustomEditor(Concept.class, new ConceptEditor(context));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale()), true));
        binder.registerCustomEditor(User.class, new UserEditor(context));
        binder.registerCustomEditor(Encounter.class, new EncounterEditor(context));
        binder.registerCustomEditor(Drug.class, new DrugEditor(context));
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> refData = new HashMap<String, Object>();
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		// just the text that we need for an empty orderType list
		String emptyOrderTypeList = this.getMessageSourceAccessor().getMessage("OrderType.list.empty");
		refData.put("emptyOrderTypeList", emptyOrderTypeList);
		
		// put the default dateformat for the locale
		refData.put("datePattern", OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()));
		
		return refData;
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
			DrugOrder order = (DrugOrder)obj;
			
			// TODO: for now, orderType will have to be hard-coded?
			order.setOrderType(new OrderType(new Integer(2)));

			Patient thisPatient = null;
			if ( order.getEncounter() == null ) {
				Integer patientId = RequestUtils.getIntParameter(request, "patientId");
				if ( patientId != null ) {
					thisPatient = context.getPatientService().getPatient(patientId);
				}
			}
			if ( order.getDateCreated() == null ) order.setDateCreated(new Date());
			if ( order.getVoided() == null ) order.setVoided(new Boolean(false));
			context.getOrderService().updateOrder(order, thisPatient);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Order.drug.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		OrderService os = context.getOrderService();
		
		DrugOrder order = null;
		
		if (context != null && context.isAuthenticated()) {
			Integer orderId = RequestUtils.getIntParameter(request, "orderId");
	    	if (orderId != null) order = (DrugOrder)os.getOrder(orderId);	
		}
		
		// if this is a new order, let's see if the user has picked a type yet
		if (order == null) {
			order = new DrugOrder();
			Integer orderTypeId = RequestUtils.getIntParameter(request, "orderTypeId");
			if ( orderTypeId != null ) {
				OrderType ot = os.getOrderType(orderTypeId);
				order.setOrderType(ot);
			}
		}
    	
        return order;
    }   
}