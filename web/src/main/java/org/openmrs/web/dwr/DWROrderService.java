/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class DWROrderService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * This method would normally be return type void, but DWR requires a callback, so we know when
	 * to refresh view
	 */
	public boolean createDrugOrder(Integer patientId, String drugId, Double dose, String units, String frequency,
	        String startDate, String instructions) throws Exception {
		log.debug("PatientId is " + patientId + " and drugId is " + drugId + " and dose is " + dose + " and units are "
		        + units + " and frequency is " + frequency + " and startDate is " + startDate + " and instructions are "
		        + instructions);
		
		boolean ret = true;
		
		DrugOrder drugOrder = new DrugOrder();
		Patient patient = Context.getPatientService().getPatient(patientId);
		Drug drug = Context.getConceptService().getDrugByNameOrId(drugId);
		if (drug == null)
			throw new DWRException("There is no drug with the name or drugId of: " + drugId);
		
		OrderType orderType = Context.getOrderService().getOrderType(OpenmrsConstants.ORDERTYPE_DRUG);
		if (orderType == null)
			throw new DWRException(
			        "There is no 'Drug' Order Type in the system.  This must be an Order Type with orderTypeId = "
			                + OpenmrsConstants.ORDERTYPE_DRUG);
		
		drugOrder.setDrug(drug);
		Concept concept = drug.getConcept();
		drugOrder.setConcept(concept);
		drugOrder.setOrderType(orderType);
		drugOrder.setPatient(patient);
		drugOrder.setDose(dose);
		drugOrder.setUnits(units);
		drugOrder.setFrequency(frequency);
		drugOrder.setInstructions(instructions);
		
		Date dStartDate = null;
		if (startDate != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				dStartDate = sdf.parse(startDate);
			}
			catch (ParseException e) {
				throw new DWRException(e.getMessage());
			}
		}
		drugOrder.setStartDate(dStartDate);
		
		drugOrder.setDateCreated(new Date());
		drugOrder.setVoided(new Boolean(false));
		
		try {
			Context.getOrderService().saveOrder(drugOrder);
		}
		catch (APIException e) {
			throw new DWRException(e.getMessage());
		}
		
		log.debug("Finished creating new drug order");
		return ret;
	}
	
	public void voidOrder(Integer orderId, String voidReason) {
		Order o = Context.getOrderService().getOrder(orderId);
		Context.getOrderService().voidOrder(o, voidReason);
	}
	
	public void discontinueOrder(Integer orderId, String discontinueReason, String discontinueDate) throws DWRException {
		Date dDiscDate = null;
		if (discontinueDate != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				dDiscDate = sdf.parse(discontinueDate);
			}
			catch (ParseException e) {
				throw new DWRException(e.getMessage());
			}
		}
		
		Order o = Context.getOrderService().getOrder(orderId);
		try {
			Context.getOrderService().discontinueOrder(o, Context.getConceptService().getConcept(discontinueReason),
			    dDiscDate);
		}
		catch (APIException e) {
			throw new DWRException(e.getMessage());
		}
	}
	
	public Vector<DrugOrderListItem> getDrugOrdersByPatientId(Integer patientId, int whatToShow) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Vector<DrugOrderListItem> ret = new Vector<DrugOrderListItem>();
		List<DrugOrder> drugOrders = Context.getOrderService().getDrugOrdersByPatient(p, whatToShow);
		if (drugOrders != null) {
			if (drugOrders.size() > 0) {
				for (DrugOrder drugOrder : drugOrders) {
					ret.add(new DrugOrderListItem(drugOrder));
				}
			}
		}
		return ret;
	}
	
	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, OrderService.SHOW_CURRENT_AND_FUTURE);
	}
	
	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, OrderService.SHOW_COMPLETE);
	}
	
	public String getUnitsByDrugId(Integer drugId) {
		String ret = "";
		
		Drug drug = Context.getConceptService().getDrug(drugId);
		if (drug != null) {
			String drugUnits = drug.getUnits();
			if (drugUnits != null)
				ret = drugUnits;
		}
		
		return ret;
	}
	
	public Vector<DrugSetItem> getCurrentDrugSet(Integer patientId, String drugSetId) {
		return getDrugSet(patientId, drugSetId, OrderService.SHOW_CURRENT_AND_FUTURE);
	}
	
	public Vector<DrugSetItem> getCompletedDrugSet(Integer patientId, String drugSetId) {
		return getDrugSet(patientId, drugSetId, OrderService.SHOW_COMPLETE);
	}
	
	public Vector<DrugSetItem> getDrugSet(Integer patientId, String drugSetId, int whatToShow) {
		log.debug("In getDrugSet() method");
		
		Vector<DrugSetItem> dsiList = null;
		
		Map<String, List<DrugOrder>> orders = this.getOrdersByDrugSet(patientId, drugSetId, ",", whatToShow);
		DrugSetItem dsi = new DrugSetItem();
		Concept c = OpenmrsUtil.getConceptByIdOrName(drugSetId);
		dsi.setDrugSetId(c.getConceptId());
		dsi.setDrugSetLabel(drugSetId.replace(" ", "_"));
		dsi.setName(c.getName(Context.getLocale()).getName());
		if (orders != null) {
			List<DrugOrder> currList = orders.get(drugSetId);
			if (currList != null) {
				dsi.setDrugCount(currList.size());
			} else {
				dsi.setDrugCount(0);
			}
		} else
			dsi.setDrugCount(0);
		dsiList = new Vector<DrugSetItem>();
		dsiList.add(dsi);
		
		return dsiList;
	}
	
	public Vector<DrugSetItem> getCurrentOtherDrugSet(Integer patientId, String displayDrugSetIds) {
		return getOtherDrugSet(patientId, displayDrugSetIds, OrderService.SHOW_CURRENT_AND_FUTURE);
	}
	
	public Vector<DrugSetItem> getCompletedOtherDrugSet(Integer patientId, String displayDrugSetIds) {
		return getOtherDrugSet(patientId, displayDrugSetIds, OrderService.SHOW_COMPLETE);
	}
	
	public Vector<DrugSetItem> getOtherDrugSet(Integer patientId, String displayDrugSetIds, int whatToShow) {
		DrugSetItem dsi = new DrugSetItem();
		
		dsi.setDrugSetLabel("__other__");
		dsi.setName("*");
		Vector<DrugOrderListItem> otherItems = getOtherDrugOrdersByPatientIdDrugSetId(patientId, displayDrugSetIds,
		    whatToShow);
		if (otherItems != null) {
			dsi.setDrugCount(otherItems.size());
		} else
			dsi.setDrugCount(0);
		
		Vector<DrugSetItem> dsiList = new Vector<DrugSetItem>();
		dsiList.add(dsi);
		
		return dsiList;
	}
	
	public Vector<DrugOrderListItem> getCurrentOtherDrugOrdersByPatientIdDrugSetId(Integer patientId,
	        String displayDrugSetIds) {
		log.debug("in getCurrentOtherDrugOrdersBy...() method");
		
		return this.getOtherDrugOrdersByPatientIdDrugSetId(patientId, displayDrugSetIds,
		    OrderService.SHOW_CURRENT_AND_FUTURE);
	}
	
	public Vector<DrugOrderListItem> getCompletedOtherDrugOrdersByPatientIdDrugSetId(Integer patientId,
	        String displayDrugSetIds) {
		log.debug("in getCompletedOtherDrugOrdersBy...() method");
		
		return this.getOtherDrugOrdersByPatientIdDrugSetId(patientId, displayDrugSetIds, OrderService.SHOW_COMPLETE);
	}
	
	public Vector<DrugOrderListItem> getOtherDrugOrdersByPatientIdDrugSetId(Integer patientId, String displayDrugSetIds,
	        int whatToShow) {
		log.debug("in getOtherDrugOrdersBy...() method");
		
		Vector<DrugOrderListItem> ret = null;
		
		Map<String, List<DrugOrder>> ordersBySetId = this.getOrdersByDrugSet(patientId, displayDrugSetIds, ",", whatToShow);
		if (ordersBySetId != null) {
			List<DrugOrder> orders = ordersBySetId.get("*");
			if (orders != null) {
				for (DrugOrder order : orders) {
					if (ret == null)
						ret = new Vector<DrugOrderListItem>();
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
		if (ordersBySetId != null) {
			List<DrugOrder> orders = ordersBySetId.get(drugSetId);
			if (orders != null) {
				for (DrugOrder order : orders) {
					if (ret == null)
						ret = new Vector<DrugOrderListItem>();
					DrugOrderListItem drugOrderItem = new DrugOrderListItem(order);
					drugOrderItem.setDrugSetId(OpenmrsUtil.getConceptByIdOrName(drugSetId).getConceptId());
					drugOrderItem.setDrugSetLabel(drugSetId.replace(" ", "_"));
					ret.add(drugOrderItem);
				}
			}
		}
		
		return ret;
	}
	
	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientIdDrugSetId(Integer patientId, String drugSetId) {
		return getDrugOrdersByPatientIdDrugSetId(patientId, drugSetId, OrderService.SHOW_CURRENT_AND_FUTURE);
	}
	
	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientIdDrugSetId(Integer patientId, String drugSetId) {
		return getDrugOrdersByPatientIdDrugSetId(patientId, drugSetId, OrderService.SHOW_COMPLETE);
	}
	
	public void voidCurrentDrugSet(Integer patientId, String drugSetId, String voidReason) {
		log.debug("in voidDrugSet() method");
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		Context.getOrderService().voidDrugSet(p, drugSetId, voidReason, OrderService.SHOW_CURRENT_AND_FUTURE);
	}
	
	public void voidCompletedDrugSet(Integer patientId, String drugSetId, String voidReason) {
		log.debug("in voidDrugSet() method");
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		Context.getOrderService().voidDrugSet(p, drugSetId, voidReason, OrderService.SHOW_COMPLETE);
	}
	
	public void discontinueDrugSet(Integer patientId, String drugSetId, String discontinueReason, String discontinueDate)
	        throws DWRException {
		log.debug("in discontinueDrugSet() method");
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		Date discDate = null;
		if (discontinueDate != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				discDate = sdf.parse(discontinueDate);
			}
			catch (ParseException e) {
				throw new DWRException(e.getMessage());
			}
		}
		
		try {
			Context.getOrderService().discontinueDrugSet(p, drugSetId,
			    Context.getConceptService().getConceptByIdOrName(discontinueReason), discDate);
		}
		catch (APIException e) {
			throw new DWRException(e.getMessage());
		}
		
	}
	
	/*
	 * This method would normally have a return type of void, but DWR requires a callback 
	 */
	public boolean voidCurrentDrugOrders(Integer patientId, String voidReason) {
		log.debug("beginning method");
		
		boolean ret = true;
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		List<DrugOrder> currentOrders = Context.getOrderService().getDrugOrdersByPatient(p,
		    OrderService.SHOW_CURRENT_AND_FUTURE);
		
		for (DrugOrder o : currentOrders) {
			Context.getOrderService().voidOrder(o, voidReason);
		}
		
		return ret;
	}
	
	/*
	 * This method would normally have a return type of void, but DWR requires a callback 
	 */
	public boolean discontinueCurrentDrugOrders(Integer patientId, String discontinueReason, String discontinueDate)
	        throws DWRException {
		log.debug("beginning method");
		
		boolean ret = true;
		
		Date discDate = null;
		if (discontinueDate != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				discDate = sdf.parse(discontinueDate);
			}
			catch (ParseException e) {
				throw new DWRException(e.getMessage());
			}
		}
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		List<DrugOrder> currentOrders = Context.getOrderService().getDrugOrdersByPatient(p,
		    OrderService.SHOW_CURRENT_AND_FUTURE);
		
		for (DrugOrder o : currentOrders) {
			try {
				Context.getOrderService().discontinueOrder(o, Context.getConceptService().getConcept(discontinueReason),
				    discDate);
			}
			catch (APIException e) {
				throw new DWRException(e.getMessage());
			}
		}
		
		return ret;
	}
	
	private Map<String, List<DrugOrder>> getOrdersByDrugSet(Integer patientId, String drugSetIds, String delimiter,
	        int whatToShow) {
		Map<String, List<DrugOrder>> ret = null;
		
		if (patientId != null && drugSetIds != null) {
			Patient p = Context.getPatientService().getPatient(patientId);
			if (p != null) {
				List<DrugOrder> ordersToWorkWith = Context.getOrderService().getDrugOrdersByPatient(p, whatToShow);
				ret = Context.getOrderService().getDrugSetsByDrugSetIdList(ordersToWorkWith, drugSetIds, delimiter);
			}
		}
		
		return ret;
	}
	
}
