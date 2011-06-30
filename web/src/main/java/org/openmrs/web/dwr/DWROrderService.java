/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

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
			throw new APIException("There is no drug with the name or drugId of: " + drugId);
		
		drugOrder.setDrug(drug);
		Concept concept = drug.getConcept();
		drugOrder.setConcept(concept);
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
				throw new APIException(e.getMessage());
			}
		}
		drugOrder.setStartDate(dStartDate);
		
		drugOrder.setDateCreated(new Date());
		drugOrder.setVoided(new Boolean(false));
		
		try {
			Context.getOrderService().saveOrder(drugOrder);
		}
		catch (APIException e) {
			throw new APIException(e.getMessage());
		}
		
		log.debug("Finished creating new drug order");
		return ret;
	}
	
	public void voidOrder(Integer orderId, String voidReason) {
		Order o = Context.getOrderService().getOrder(orderId);
		Context.getOrderService().voidOrder(o, voidReason);
	}
	
	public void discontinueOrder(Integer orderId, String discontinueReason, String discontinueDate) throws APIException {
		Date dDiscDate = null;
		if (discontinueDate != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				dDiscDate = sdf.parse(discontinueDate);
			}
			catch (ParseException e) {
				throw new APIException(e.getMessage());
			}
		}
		
		Order o = Context.getOrderService().getOrder(orderId);
		try {
			Context.getOrderService().discontinueOrder(o,
			    Context.getConceptService().getConcept(discontinueReason).getName().getName(), null, dDiscDate);
		}
		catch (APIException e) {
			throw new APIException(e.getMessage());
		}
	}
	
	//TODO Find all calls to this methods in the views
	public Vector<DrugOrderListItem> getDrugOrdersByPatientId(Integer patientId, int whatToShow) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Vector<DrugOrderListItem> ret = new Vector<DrugOrderListItem>();
		List<DrugOrder> drugOrders = Context.getOrderService().getDrugOrdersByPatient(p);
		if (drugOrders != null) {
			if (drugOrders.size() > 0) {
				for (DrugOrder drugOrder : drugOrders) {
					ret.add(new DrugOrderListItem(drugOrder));
				}
			}
		}
		return ret;
	}
	
	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientId(Integer patientId) {
		//TODO fix me
		return null;
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
	
	/*
	 * This method would normally have a return type of void, but DWR requires a callback 
	 */
	public boolean voidCurrentDrugOrders(Integer patientId, String voidReason) {
		log.debug("beginning method");
		
		boolean ret = true;
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		List<DrugOrder> currentOrders = Context.getOrderService().getDrugOrdersByPatient(p);
		
		for (DrugOrder o : currentOrders) {
			Context.getOrderService().voidOrder(o, voidReason);
		}
		
		return ret;
	}
	
	/*
	 * This method would normally have a return type of void, but DWR requires a callback 
	 */
	public boolean discontinueCurrentDrugOrders(Integer patientId, String discontinueReason, String discontinueDate)
	        throws APIException {
		log.debug("beginning method");
		
		boolean ret = true;
		
		Date discDate = null;
		if (discontinueDate != null) {
			SimpleDateFormat sdf = Context.getDateFormat();
			try {
				discDate = sdf.parse(discontinueDate);
			}
			catch (ParseException e) {
				throw new APIException(e.getMessage());
			}
		}
		
		Patient p = Context.getPatientService().getPatient(patientId);
		
		List<DrugOrder> currentOrders = Context.getOrderService().getDrugOrdersByPatient(p);
		
		for (DrugOrder o : currentOrders) {
			try {
				Context.getOrderService().discontinueOrder(o,
				    Context.getConceptService().getConcept(discontinueReason).getName().getName(), null, discDate);
			}
			catch (APIException e) {
				throw new APIException(e.getMessage());
			}
		}
		
		return ret;
	}
}
