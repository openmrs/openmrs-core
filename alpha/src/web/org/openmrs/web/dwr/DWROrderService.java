package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWROrderService {

	protected final Log log = LogFactory.getLog(getClass());
		
	/**
	 */
	public void createDrugOrder(Integer patientId, Integer drugId, Double dose, String units, String frequency, String startDate, String instructions) {
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
			drugOrder.setInstructions(instructions);
			
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
			context.getOrderService().updateOrder(drugOrder, patient);
		}
	}
	
	public void voidOrder(Integer orderId, String voidReason) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Order o = context.getOrderService().getOrder(orderId);
			context.getOrderService().voidOrder(o, voidReason);
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
			context.getOrderService().discontinueOrder(o, discontinueReason, dDiscDate);
		}
	}

	public Vector<DrugOrderListItem> getDrugOrdersByPatientId(Integer patientId, int whatToShow) {
		Context context = getContext();
		if (context != null) {
			Patient p = context.getPatientService().getPatient(patientId);
			Vector<DrugOrderListItem> ret = new Vector<DrugOrderListItem>();
			List<DrugOrder> drugOrders = context.getOrderService().getDrugOrdersByPatient(p, whatToShow);
			if ( drugOrders != null ) {
				if ( drugOrders.size() > 0 ) {
					for (DrugOrder drugOrder : drugOrders) {
						ret.add(new DrugOrderListItem(drugOrder));
					}
				}
			}
			return ret;
		} else {
			return null;
		}
	}

	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, OrderService.SHOW_COMPLETE);
	}
	
	public String getUnitsByDrugId(Integer drugId) {
		String ret = "";
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Drug drug = context.getConceptService().getDrug(drugId);
			if ( drug != null ) {
				String drugUnits = drug.getUnits();
				if ( drugUnits != null ) ret = drugUnits;
			}
		}
		
		return ret; 
	}
		
	public Vector<DrugSetItem> getCurrentDrugSet(Integer patientId, String drugSetId) {
		return getDrugSet(patientId, drugSetId, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugSetItem> getCompletedDrugSet(Integer patientId, String drugSetId) {
		return getDrugSet(patientId, drugSetId, OrderService.SHOW_COMPLETE);
	}

	public Vector<DrugSetItem> getDrugSet(Integer patientId, String drugSetId, int whatToShow) {
		log.debug("In getDrugSet() method");
		
		Vector<DrugSetItem> dsiList = null;
		
		Map<String, List<DrugOrder>> orders = this.getOrdersByDrugSet(patientId, drugSetId, ",", whatToShow);
		DrugSetItem dsi = new DrugSetItem();
		Concept c = OpenmrsUtil.getConceptByIdOrName(drugSetId, getContext());
		dsi.setDrugSetId(c.getConceptId());
		dsi.setDrugSetLabel(drugSetId.replace(" ", "_"));
		dsi.setName(c.getName(getContext().getLocale()).getName());
		if ( orders != null ) {
			List<DrugOrder> currList = orders.get(drugSetId);
			if ( currList != null ) {
				dsi.setDrugCount(currList.size());
			} else { 
				dsi.setDrugCount(0);
			}
		} else dsi.setDrugCount(0);
		dsiList = new Vector<DrugSetItem>();
		dsiList.add(dsi);

		return dsiList;
	}

	public Vector<DrugSetItem> getCurrentOtherDrugSet(Integer patientId, String displayDrugSetIds) {
		return getOtherDrugSet(patientId, displayDrugSetIds, OrderService.SHOW_CURRENT);
	}
	
	public Vector<DrugSetItem> getCompletedOtherDrugSet(Integer patientId, String displayDrugSetIds) {
		return getOtherDrugSet(patientId, displayDrugSetIds, OrderService.SHOW_COMPLETE);
	}

	public Vector<DrugSetItem> getOtherDrugSet(Integer patientId, String displayDrugSetIds, int whatToShow) {
		DrugSetItem dsi = new DrugSetItem();

		dsi.setDrugSetLabel("__other__");
		dsi.setName("*");
		Vector<DrugOrderListItem> otherItems = getOtherDrugOrdersByPatientIdDrugSetId(patientId, displayDrugSetIds, whatToShow);
		if ( otherItems != null ) {
			dsi.setDrugCount(otherItems.size());
		} else dsi.setDrugCount(0);
		
		Vector<DrugSetItem> dsiList = new Vector<DrugSetItem>();
		dsiList.add(dsi);
		
		return dsiList;
	}

	public Vector<DrugOrderListItem> getCurrentOtherDrugOrdersByPatientIdDrugSetId(Integer patientId, String displayDrugSetIds) {
		log.debug("in getCurrentOtherDrugOrdersBy...() method");

		return this.getOtherDrugOrdersByPatientIdDrugSetId(patientId, displayDrugSetIds, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugOrderListItem> getCompletedOtherDrugOrdersByPatientIdDrugSetId(Integer patientId, String displayDrugSetIds) {
		log.debug("in getCompletedOtherDrugOrdersBy...() method");

		return this.getOtherDrugOrdersByPatientIdDrugSetId(patientId, displayDrugSetIds, OrderService.SHOW_COMPLETE);
	}
	
	public Vector<DrugOrderListItem> getOtherDrugOrdersByPatientIdDrugSetId(Integer patientId, String displayDrugSetIds, int whatToShow) {
		log.debug("in getOtherDrugOrdersBy...() method");
		
		Vector<DrugOrderListItem> ret = null;
		
		Map<String, List<DrugOrder>> ordersBySetId = this.getOrdersByDrugSet(patientId, displayDrugSetIds, ",", whatToShow);
		if ( ordersBySetId != null ) {
			List<DrugOrder> orders = ordersBySetId.get("*");
			if ( orders != null ) {
				for (DrugOrder order : orders) {
					if ( ret == null ) ret = new Vector<DrugOrderListItem>();
					DrugOrderListItem drugOrderItem = new DrugOrderListItem(order);
					drugOrderItem.setDrugSetLabel("__other__");
					ret.add(drugOrderItem);
				}
			}
		}
		
		return ret;
	}
	
	public Vector<DrugOrderListItem> getDrugOrdersByPatientIdDrugSetId(Integer patientId, String drugSetId, int whatToShow) {
		log.debug("Entering getCurrentDrugOrdersByPatientIdDrugSetId method with drugSetId: " + drugSetId);
		
		Vector<DrugOrderListItem> ret = null;
		
		Map<String, List<DrugOrder>> ordersBySetId = this.getOrdersByDrugSet(patientId, drugSetId, ",", whatToShow);
		if ( ordersBySetId != null ) {
			List<DrugOrder> orders = ordersBySetId.get(drugSetId);
			if ( orders != null ) {
				for (DrugOrder order : orders) {
					if ( ret == null ) ret = new Vector<DrugOrderListItem>();
					DrugOrderListItem drugOrderItem = new DrugOrderListItem(order);
					drugOrderItem.setDrugSetId(OpenmrsUtil.getConceptByIdOrName(drugSetId, this.getContext()).getConceptId());
					drugOrderItem.setDrugSetLabel(drugSetId.replace(" ", "_"));
					ret.add(drugOrderItem);
				}
			}
		}
		
		return ret;
	}

	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientIdDrugSetId(Integer patientId, String drugSetId) {
		return getDrugOrdersByPatientIdDrugSetId(patientId, drugSetId, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientIdDrugSetId(Integer patientId, String drugSetId) {
		return getDrugOrdersByPatientIdDrugSetId(patientId, drugSetId, OrderService.SHOW_COMPLETE);
	}

	
	public void voidCurrentDrugSet(Integer patientId, String drugSetId, String voidReason) {
		log.debug("in voidDrugSet() method");

		Context context = this.getContext();
		Patient p = context.getPatientService().getPatient(patientId);

		context.getOrderService().voidDrugSet(p, drugSetId, voidReason, OrderService.SHOW_CURRENT);
	}

	public void voidCompletedDrugSet(Integer patientId, String drugSetId, String voidReason) {
		log.debug("in voidDrugSet() method");

		Context context = this.getContext();
		Patient p = context.getPatientService().getPatient(patientId);

		context.getOrderService().voidDrugSet(p, drugSetId, voidReason, OrderService.SHOW_COMPLETE);
	}

	
	public void discontinueDrugSet(Integer patientId, String drugSetId, String discontinueReason, String discontinueDate) {
		log.debug("in discontinueDrugSet() method");

		Context context = getContext();
		Patient p = context.getPatientService().getPatient(patientId);

		Date discDate = null;
		if ( discontinueDate != null ) {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			try {
				discDate = sdf.parse(discontinueDate);
			} catch (ParseException e) {
				discDate = null;
			}
		}
	
		context.getOrderService().discontinueDrugSet(p, drugSetId, discontinueReason, discDate);
	}

	private Map<String, List<DrugOrder>> getOrdersByDrugSet(Integer patientId, String drugSetIds, String delimiter, int whatToShow) {
		Map<String, List<DrugOrder>> ret = null;
		
		if ( patientId != null && drugSetIds != null ) {
			Context context = getContext();
			if ( context != null ) {
				Patient p = context.getPatientService().getPatient(patientId);
				if ( p != null ) {
					List<DrugOrder> ordersToWorkWith = context.getOrderService().getDrugOrdersByPatient(p, whatToShow);
					ret = context.getOrderService().getDrugSetsByDrugSetIdList(ordersToWorkWith, drugSetIds, delimiter);
				}
			}
		}
		
		return ret;
	}

	private Context getContext() {
		return (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	}
}




