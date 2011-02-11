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
package org.openmrs.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderService.ORDER_STATUS;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Contains convenience methods for working with Orders.
 */
public class OrderUtil {
	
	private static final Log log = LogFactory.getLog(OrderUtil.class);
	
	/**
	 * Discontinues all current orders for the given <code>patient</code>
	 * 
	 * @param patient
	 * @param discontinueReason
	 * @param discontinueDate
	 * @see OrderService#discontinueOrder(org.openmrs.Order, Concept, Date)
	 * @should discontinue all orders for the given patient if none are yet discontinued
	 * @should not affect orders that were already discontinued on the specified date
	 * @should not affect orders that end before the specified date
	 * @should not affect orders that start after the specified date
	 */
	public static void discontinueAllOrders(Patient patient, Concept discontinueReason, Date discontinueDate) {
		if (log.isDebugEnabled())
			log.debug("In discontinueAll with patient " + patient + " and concept " + discontinueReason + " and date "
			        + discontinueDate);
		
		OrderService orderService = Context.getOrderService();
		
		List<DrugOrder> drugOrders = orderService.getDrugOrdersByPatient(patient, ORDER_STATUS.CURRENT);
		
		// loop over all of this patient's drug orders to discontinue each
		if (drugOrders != null) {
			for (DrugOrder drugOrder : drugOrders) {
				if (log.isDebugEnabled())
					log.debug("discontinuing order: " + drugOrder);
				// do the stuff to the database
				orderService.discontinueOrder(drugOrder, discontinueReason, discontinueDate);
			}
		}
	}
	
	/**
	 * Void all DrugOrders for drugs whose concepts are in the given set, and that have the given
	 * status. An end-user would think of this method as "delete all drug orders of the given type".
	 * 
	 * @param patient
	 * @param drugSetId
	 * @param voidReason
	 * @param status
	 * @should void all drug orders of the given type when status is null
	 * @should void drug orders of the given type for status of CURRENT
	 * @should void drug orders of the given type for status of COMPLETE
	 * @should not affect drug orders that are already voided
	 */
	public static void voidDrugSet(Patient patient, String drugSetId, String voidReason, ORDER_STATUS status) {
		if (log.isDebugEnabled())
			log.debug("Voiding drug sets for patient: " + patient + " drugSetId: " + drugSetId + " reason: " + voidReason
			        + " status: " + status);
		
		// do some null pointer checks
		
		if (Context.isAuthenticated() == false)
			throw new ContextAuthenticationException("Unable to void drugs when no one is logged in");
		
		if (patient == null)
			throw new APIException("Unable to void drugs without a patient being given");
		
		if (drugSetId == null)
			throw new APIException("Unable to void drugs without a drugSetId being given");
		
		OrderService orderService = Context.getOrderService();
		
		List<DrugOrder> currentOrders = orderService.getDrugOrdersByPatient(patient, status);
		
		Map<String, List<DrugOrder>> ordersBySetId = getDrugSetsByDrugSetIdList(currentOrders, drugSetId, ",");
		
		// loop over the orders and mark each as void in the database
		if (ordersBySetId != null) {
			List<DrugOrder> ordersToVoid = ordersBySetId.get(drugSetId);
			if (ordersToVoid != null) {
				for (DrugOrder order : ordersToVoid) {
					orderService.voidOrder(order, voidReason);
				}
			}
		}
	}
	
	/**
	 * Discontinue orders for the given patient with the given drug sets ...
	 * 
	 * @param patient
	 * @param drugSetId
	 * @param discontinueReason
	 * @param discontinueDate
	 * @should discontinue all orders of the given type for the given patient if none are yet
	 *         discontinued
	 * @should not affect orders that were already discontinued on the specified date
	 * @should not affect orders that end before the specified date
	 * @should not affect orders that start after the specified date
	 */
	public static void discontinueDrugSet(Patient patient, String drugSetId, Concept discontinueReason, Date discontinueDate) {
		if (log.isDebugEnabled()) {
			log.debug("Discontinuing drug sets. patient: " + patient + " drugSetId: " + drugSetId + " reason: "
			        + discontinueReason + " date: " + discontinueDate);
		}
		
		// do some null pointer checks
		
		if (Context.isAuthenticated() == false)
			throw new ContextAuthenticationException("Unable to discontinue drugs when no one is logged in");
		
		if (patient == null)
			throw new APIException("Unable to discontinue drugs without a patient being given");
		
		if (drugSetId == null)
			throw new APIException("Unable to discontinue drugs without a drugSetId being given");
		
		OrderService orderService = Context.getOrderService();
		
		List<DrugOrder> currentOrders = orderService.getDrugOrdersByPatient(patient, ORDER_STATUS.CURRENT);
		Map<String, List<DrugOrder>> ordersBySetId = getDrugSetsByDrugSetIdList(currentOrders, drugSetId, ",");
		
		// loop over all of the orders and discontinue each of them
		if (ordersBySetId != null) {
			List<DrugOrder> ordersToDiscontinue = ordersBySetId.get(drugSetId);
			if (ordersToDiscontinue != null) {
				for (DrugOrder order : ordersToDiscontinue) {
					orderService.discontinueOrder(order, discontinueReason, discontinueDate);
				}
			} else {
				log.debug("no orders to discontinue");
			}
		} else {
			log.debug("no ordersBySetId returned for " + drugSetId);
		}
		
	}
	
	/**
	 * Associates the concept id of a drug set to a name of the drug set in the current locale
	 * 
	 * @param drugSetIds a comma separated list with the concept id of the drug sets
	 * @return <code>Map<String, String></code> of the drug headers for the given drugSetIds
	 * @should get map from concept id as string to concept name
	 */
	public static Map<String, String> getDrugSetHeadersByDrugSetIdList(String drugSetIds) {
		Map<String, String> ret = null;
		
		if (drugSetIds == null)
			throw new APIException("Unable to get drug headers without drugSetIds being given");
		
		Map<String, Concept> concepts = OpenmrsUtil.delimitedStringToConceptMap(drugSetIds, ",");
		if (concepts != null) {
			for (Map.Entry<String, Concept> e : concepts.entrySet()) {
				String id = e.getKey();
				Concept concept = e.getValue();
				if (ret == null)
					ret = new HashMap<String, String>();
				ret.put(id, concept.getName(Context.getLocale()).getName());
			}
		}
		
		return ret;
	}
	
	/**
	 * Gets a map of DrugOrders that belong to a DrugSet concept ID
	 * 
	 * @param orderList the Drug Order list
	 * @param drugSetIdList a 'delimiter' separated list of drug sets
	 * @param delimiter the delimiter of drug sets (defaults to a comma if set to null)
	 * @return Map<String, List<DrugOrder>> of DrugOrders that belong to a DrugSet concept_id
	 * @should get a map from concept id as string to drug orders that belong to that drug set
	 * @should treat an asterisk as all other drugs
	 */
	public static Map<String, List<DrugOrder>> getDrugSetsByDrugSetIdList(List<DrugOrder> orderList, String drugSetIdList,
	                                                                      String delimiter) {
		if (log.isDebugEnabled()) {
			log.debug("in getdrugsetsbydrugsetidlist. orderlist: " + orderList + " drugsetidlist: " + drugSetIdList
			        + " delimiter: " + delimiter);
		}
		
		if (delimiter == null)
			delimiter = ",";
		
		Map<String, List<DrugOrder>> ret = null;
		
		if (drugSetIdList != null && orderList != null) {
			List<Concept> drugSetConcepts = new ArrayList<Concept>();
			boolean addOthers = false;
			Map<Concept, String> idToConceptMappings = new HashMap<Concept, String>();
			
			String[] drugSetIds = drugSetIdList.split(delimiter);
			log.debug("starting with " + drugSetIds.length + " items in comma-delimited list, and " + orderList.size()
			        + " orders that are " + orderList);
			for (String drugSetId : drugSetIds) {
				// go through and get all concepts for these drugSetIds - then we can call another method to get Map
				
				if ("*".equals(drugSetId)) {
					// add "other"
					addOthers = true;
				} else {
					Concept drugSetConcept = OpenmrsUtil.getConceptByIdOrName(drugSetId);
					
					if (drugSetConcept != null) {
						drugSetConcepts.add(drugSetConcept);
						idToConceptMappings.put(drugSetConcept, drugSetId);
						log.debug("added concept " + drugSetConcept.getName(Context.getLocale()) + ", and mapping to id "
						        + drugSetId);
					}
				}
			}
			
			// now we know what drugSet concepts to separate the orderList into
			
			// first, let's create a list of "others", starting with a full list that we remove from
			List<DrugOrder> otherOrders = null;
			if (addOthers)
				otherOrders = orderList;
			
			Map<Concept, List<DrugOrder>> ordersByConcepts = getDrugSetsByConcepts(orderList, drugSetConcepts);
			if (ordersByConcepts != null) {
				log.debug("obc is size " + ordersByConcepts.size());
				for (Map.Entry<Concept, List<DrugOrder>> e : ordersByConcepts.entrySet()) {
					Concept c = e.getKey();
					List<DrugOrder> orders = e.getValue();
					log.debug("found concept " + c.getName(Context.getLocale()) + ", and list is size " + orders.size()
					        + " and list is " + orders);
					if (addOthers && otherOrders != null) {
						otherOrders.removeAll(orders);
					}
					if (ret == null)
						ret = new HashMap<String, List<DrugOrder>>();
					log.debug("putting list of size " + orders.size() + " in string " + idToConceptMappings.get(c));
					ret.put(idToConceptMappings.get(c), orders);
				}
			}
			
			// add the "others" list to the Map
			if (addOthers && otherOrders != null) {
				if (ret == null)
					ret = new HashMap<String, List<DrugOrder>>();
				ret.put("*", otherOrders);
			}
		}
		
		return ret;
	}
	
	/**
	 * Splits the drug orders into sublists based on which drug set the order's drug belongs to
	 * 
	 * @param drugOrders List of drugOrders
	 * @param drugSets List of drugSets concept
	 * @return Map<Concept, List<DrugOrder>> of a sublist of drug orders mapped by the drug set
	 *         concept that they belong
	 * @throws APIException
	 * @should get a map from concept to drugs orders in that drug set
	 */
	public static Map<Concept, List<DrugOrder>> getDrugSetsByConcepts(List<DrugOrder> drugOrders, List<Concept> drugSets)
	                                                                                                                     throws APIException {
		if (log.isDebugEnabled()) {
			log.debug("in getdrugsetsbyconcepts. drugOrders: " + drugOrders + " drugSets: " + drugSets);
		}
		
		Map<Concept, List<DrugOrder>> hmRet = null;
		
		if (drugSets != null && drugOrders != null) {
			log.debug("drugSets is size " + drugSets.size());
			for (Concept c : drugSets) {
				List<DrugOrder> ordersForConcept = new ArrayList<DrugOrder>();
				
				Collection<ConceptSet> relatedConcepts = c.getConceptSets();
				log.debug("Concept is " + c.getName(Context.getLocale()) + " and has " + relatedConcepts.size()
				        + " related concepts");
				
				// now we have as a list, let's iterate
				if (relatedConcepts != null) {
					for (ConceptSet cs : relatedConcepts) {
						Concept csConcept = cs.getConcept();
						for (DrugOrder currOrder : drugOrders) {
							Drug currDrug = currOrder.getDrug();
							if (currDrug != null) {
								Concept currConcept = currDrug.getConcept(); // must not be null - ordained by data model
								if (currConcept.equals(csConcept)) {
									ordersForConcept.add(currOrder);
									log.debug("just added an order for " + currDrug.getName() + " to list of "
									        + c.getName(Context.getLocale()));
								}
							}
						}
					}
				}
				
				if (ordersForConcept.size() > 0) {
					if (hmRet == null)
						hmRet = new HashMap<Concept, List<DrugOrder>>();
					hmRet.put(c, ordersForConcept);
					log.debug("Concept " + c.getName(Context.getLocale()) + " was put to the map with a list of size "
					        + ordersForConcept.size());
				}
			}
		} else
			log.debug("drugSets is null");
		
		return hmRet;
	}
	
}
