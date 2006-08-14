package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWROrderService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public static final int SHOW_CURRENT = 1;
	public static final int SHOW_ALL = 2;
	public static final int SHOW_COMPLETE = 3;
	public static final int SHOW_NOTVOIDED = 4;
	
	
	/**
	 */
	public void createDrugOrder(Integer patientId, Integer drugId, Double dose, String units, String frequency, String startDate) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			DrugOrder drugOrder = new DrugOrder();
			Patient patient = context.getPatientService().getPatient(patientId);
			Drug drug = context.getConceptService().getDrug(drugId);
			drugOrder.setDrug(drug);
			Concept concept = drug.getConcept();
			drugOrder.setConcept(concept);
			drugOrder.setOrderType(new OrderType(new Integer(OpenmrsConstants.ORDERTYPE_DRUG)));

			drugOrder.setDose(dose);
			drugOrder.setUnits(units);
			drugOrder.setFrequency(frequency);
			
			Date dStartDate = null;
			if ( startDate != null ) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				try {
					dStartDate = sdf.parse(startDate);
				} catch (ParseException e) {
					dStartDate = null;
				}
			}
			drugOrder.setStartDate(dStartDate);
			
			drugOrder.setDateCreated(new Date());
			drugOrder.setVoided(new Boolean(false));
			context.getAdministrationService().updateOrder(drugOrder, patient);
		}
	}
	
	public void voidOrder(Integer orderId, String voidReason) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Order o = context.getOrderService().getOrder(orderId);
			context.getAdministrationService().voidOrder(o, voidReason);
		}
	}
	
	public void discontinueOrder(Integer orderId, String discontinueReason, String discontinueDate) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Date dDiscDate = null;
			if ( discontinueDate != null ) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				try {
					dDiscDate = sdf.parse(discontinueDate);
				} catch (ParseException e) {
					dDiscDate = null;
				}
			}
			
			Order o = context.getOrderService().getOrder(orderId);
			context.getAdministrationService().discontinueOrder(o, discontinueReason, dDiscDate);
		}
	}

	public Vector<DrugOrderListItem> getDrugOrdersByPatientId(Integer patientId, int whatToShow) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Patient p = context.getPatientService().getPatient(patientId);
			Vector<DrugOrderListItem> ret = new Vector<DrugOrderListItem>();
			List<DrugOrder> drugOrders = context.getOrderService().getDrugOrdersByPatient(p);
			for (DrugOrder drugOrder : drugOrders) {
				boolean shouldAdd = false;

				if ( whatToShow == DWROrderService.SHOW_COMPLETE && drugOrder.getDiscontinued() ) shouldAdd = true;
				if ( whatToShow == DWROrderService.SHOW_CURRENT && !drugOrder.getDiscontinued() && !drugOrder.getVoided() ) shouldAdd = true;
				if ( whatToShow == DWROrderService.SHOW_NOTVOIDED && !drugOrder.getVoided() ) shouldAdd = true;
				if ( whatToShow == DWROrderService.SHOW_ALL ) shouldAdd = true;

				if ( shouldAdd ) ret.add(new DrugOrderListItem(drugOrder));
			}
			return ret;
		} else {
			return null;
		}
	}

	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, DWROrderService.SHOW_CURRENT);
	}

	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, DWROrderService.SHOW_COMPLETE);
	}
}
