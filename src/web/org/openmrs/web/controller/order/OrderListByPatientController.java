package org.openmrs.web.controller.order;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class OrderListByPatientController extends SimpleFormController {
	
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
        //binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
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
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String success = msa.getMessage("Order.list.saved");
			view = getSuccessView();
			if ( ServletRequestUtils.getIntParameter(request, "patientId") != null ) view += "?patientId=" + ServletRequestUtils.getIntParameter(request, "patientId");
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
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

		//default empty Object
		List<DrugOrder> orderList = new Vector<DrugOrder>();
		Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId");
		boolean showAll = ServletRequestUtils.getBooleanParameter(request, "showAll", false);
		System.err.println("pid is " + patientId);
		
		//only fill the Object is the user has authenticated properly
		if ( Context.isAuthenticated() ) {
			if ( patientId != null ) {
				// this is the default
				this.setFormView("/admin/orders/orderListByPatient");
				PatientService ps = Context.getPatientService();
				Patient p = ps.getPatient(patientId);
				
				if ( p != null ) {
					OrderService os = Context.getOrderService();
			    	orderList = os.getDrugOrdersByPatient(p);
				} else {
					log.error("Could not get a patient corresponding to patientId [" + patientId + "], thus could not get drug orders.");
					throw new ServletException();
				}
			} else {
				if ( showAll ) {
					this.setFormView("/admin/orders/orderDrugList");
					OrderService os = Context.getOrderService();
			    	orderList = os.getDrugOrders();
				} else {
					this.setFormView("/admin/orders/choosePatient");
				}
			}

		}
    	
        return orderList;
    }

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		
		Map<String,Object> refData = new HashMap<String,Object>();

		// Load international concept names so we can show the correct drug name
		Map<Integer,String> conceptNames = new HashMap<Integer,String>();
		
		List<Order> orderList = (List<Order>)obj;
		
		for ( Order order : orderList ) {
			Concept c = order.getConcept();
			String cName = c.getName(request.getLocale()).getName();
			conceptNames.put(c.getConceptId(), cName);
		}
		
		refData.put("conceptNames", conceptNames);

		// Load information about this patient that we might need
		Integer patientId = ServletRequestUtils.getIntParameter(request, "patientId");
		Patient p = null;
		
		if ( Context.isAuthenticated() ) {
			if ( patientId != null ) {
				PatientService ps = Context.getPatientService();
				p = ps.getPatient(patientId);
				
				Set<PersonName> PersonNames = p.getNames();
				Iterator i = PersonNames.iterator();
				PersonName pm = (PersonName)i.next();
				
				refData.put("patient", p);
				refData.put("PersonName", pm);
			}			
		}
				
		return refData;
	}
}