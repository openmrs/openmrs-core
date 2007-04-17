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
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
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
		
        binder.registerCustomEditor(OrderType.class, new OrderTypeEditor());
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor("t", "f", true));
        binder.registerCustomEditor(Concept.class, new ConceptEditor());
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()), Context.getLocale()), true));
        binder.registerCustomEditor(User.class, new UserEditor());
        binder.registerCustomEditor(Encounter.class, new EncounterEditor());
        binder.registerCustomEditor(Drug.class, new DrugEditor());
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> refData = new HashMap<String, Object>();
		
		HttpSession httpSession = request.getSession();
		
		
		// just the text that we need for an empty orderType list
		String emptyOrderTypeList = this.getMessageSourceAccessor().getMessage("OrderType.list.empty");
		refData.put("emptyOrderTypeList", emptyOrderTypeList);
		
		// put the default dateformat for the locale
		refData.put("datePattern", OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()));
		
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
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			DrugOrder order = (DrugOrder)obj;
			
			// TODO: for now, orderType will have to be hard-coded?
			order.setOrderType(new OrderType(new Integer(2)));

			Patient thisPatient = null;
			if ( order.getEncounter() == null ) {
				Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId");
				if ( patientId != null ) {
					thisPatient = Context.getPatientService().getPatient(patientId);
				}
			}

			order.setPatient(thisPatient);

			if ( order.getDateCreated() == null ) order.setDateCreated(new Date());
			if ( order.getVoided() == null ) order.setVoided(new Boolean(false));
			Context.getOrderService().updateOrder(order);
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
		
		OrderService os = Context.getOrderService();
		
		DrugOrder order = null;
		
		if (Context.isAuthenticated()) {
			Integer orderId = ServletRequestUtils.getIntParameter(request, "orderId");
	    	if (orderId != null) order = (DrugOrder)os.getOrder(orderId);	
		}
		
		// if this is a new order, let's see if the user has picked a type yet
		if (order == null) {
			order = new DrugOrder();
			Integer orderTypeId = ServletRequestUtils.getIntParameter(request, "orderTypeId");
			if ( orderTypeId != null ) {
				OrderType ot = os.getOrderType(orderTypeId);
				order.setOrderType(ot);
			}
		}
    	
        return order;
    }   
}