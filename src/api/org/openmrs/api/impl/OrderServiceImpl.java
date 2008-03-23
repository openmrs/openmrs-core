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
package org.openmrs.api.impl;

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
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.DrugOrderSupport;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Order-related services
 * @version 1.0
 */
public class OrderServiceImpl implements OrderService {

	protected final Log log = LogFactory.getLog(getClass());

	private OrderDAO dao;
	
	public OrderServiceImpl() {}
	
	private OrderDAO getOrderDAO() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ORDERS);
		
		return dao;
	}
	
	public void setOrderDAO(OrderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Create a new Order
	 * @param Order to create
	 * @throws APIException
	 */
	public void createOrder(Order order) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().createOrder(order);
	}

	/**
	 * Update Order
	 * @param Order to update
	 * @throws APIException
	 */
	public void updateOrder(Order order) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		// If this order has no encounter, check if the patient exi
		
		getOrderDAO().updateOrder(order);
	}

	/**
	 * Delete Order
	 * @param Order to delete
	 * @throws APIException
	 */
	public void deleteOrder(Order order) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().deleteOrder(order);
	}

	/**
	 * Void Order
	 * @param voidReason 
	 * @param Order to void
	 * @throws APIException
	 */
	public void voidOrder(Order order, String voidReason) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().voidOrder(order, voidReason);
	}
	
	/**
	 * Void Order
	 * @param voidReason 
	 * @param Order to void
	 * @throws APIException
	 */
	public void discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().discontinueOrder(order, discontinueReason, discontinueDate);
	}

	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws APIException
	 */
	public void createOrderType(OrderType orderType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getOrderDAO().createOrderType(orderType);
	}

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getOrderDAO().updateOrderType(orderType);
	}

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getOrderDAO().deleteOrderType(orderType);
	}

	/**
	 * Creates a collection of orders and an encounter to hold them. orders[i].encounter will be set to the new encounter.
	 * If there's an EncounterType with name "Regimen Change", then the newly-created encounter will have that type
	 * @throws APIException if there is no User with username Unknown or no Location with name Unknown or Unknown Location, 
	 * or if there's no encounter type with name 'Regimen Change' 
	 */
	public void createOrdersAndEncounter(Patient p, Collection<Order> orders) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_ADD_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_ORDERS);
		User unknownUser = Context.getUserService().getUserByUsername("Unknown");
		Location unknownLocation = Context.getEncounterService().getLocationByName("Unknown Location");
		if (unknownLocation == null)
			unknownLocation = Context.getEncounterService().getLocationByName("Unknown");
		
		// TODO: fix this hack
		if (unknownUser == null) {
			unknownUser = Context.getAuthenticatedUser();
		}
		if (unknownUser == null || unknownLocation == null) {
			throw new APIException("Couldn't find a Location and a User named 'Unknown'.");
		}
		
		EncounterType encounterType = Context.getEncounterService().getEncounterType("Regimen Change"); 
 	 	if (encounterType == null) 
 	 		throw new APIException("Couldn't find an encounter type 'Regimen Change'"); 
		
		Encounter e = new Encounter();
		e.setPatient(p);
		e.setProvider(unknownUser);
		e.setLocation(unknownLocation);
		e.setEncounterDatetime(new Date());
		// TODO: Remove hardcoded encounter type
		e.setEncounterType(encounterType);
		for (Order order : orders) {
			if (order.getCreator() == null) {
				order.setCreator(Context.getAuthenticatedUser());
			}
			if (order.getDateCreated() == null) {
				order.setDateCreated(new Date());
			}
			e.addOrder(order);
			order.setEncounter(e);
		}
		Context.getEncounterService().createEncounter(e);
	}

	/**
	 * Get order by internal identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 */
	public Order getOrder(Integer orderId) throws APIException {
		return getOrderDAO().getOrder(orderId);
	}

	public DrugOrder getDrugOrder(Integer drugOrderId) throws APIException {
		return getOrderDAO().getDrugOrder(drugOrderId);
	}

	/**
	 * Get all orders
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<Order> getOrders() throws APIException {
		return getOrderDAO().getOrders();
	}

	/**
	 * Get all drug orders
	 * 
	 * @return drug orders list
	 * @throws APIException
	 */
	public List<DrugOrder> getDrugOrders() throws APIException {
		return getOrderDAO().getDrugOrders();
	}

	/**
	 * Get all orders by User
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<Order> getOrdersByUser(User user) throws APIException {
		//return getOrderDAO().getOrdersByUser(user);
		return null;
	}

	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<Order> getOrdersByPatient(Patient patient) throws APIException {
		return getOrderDAO().getOrdersByPatient(patient);
	}

	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow) {
		return getDrugOrdersByPatient(patient, whatToShow, false);
	}
		
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow, boolean includeVoided) {
		List<DrugOrder> ret = new ArrayList<DrugOrder>();
		
		if (patient != null) {
			List<DrugOrder> drugOrders = getOrderDAO().getDrugOrdersByPatient(patient, includeVoided);
			if ( drugOrders != null ) {
				for (DrugOrder drugOrder : drugOrders) {
					if ( whatToShow == OrderService.SHOW_COMPLETE && drugOrder.isDiscontinued() ) ret.add(drugOrder);
					if ( whatToShow == OrderService.SHOW_CURRENT && drugOrder.isCurrent() ) ret.add(drugOrder);
					if ( whatToShow == OrderService.SHOW_NOTVOIDED && !drugOrder.getVoided() ) ret.add(drugOrder);
					if ( whatToShow == OrderService.SHOW_ALL ) ret.add(drugOrder);
				}
			}
		}
		
		return ret;
	}
		
	/**
	 * Undiscontinue order record
	 * 
	 * @param order order to be undiscontinued
	 */
	public void undiscontinueOrder(Order order) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ORDERS);
		getOrderDAO().undiscontinueOrder(order);
	}

	
	/**
	 * Unvoid order record
	 * 
	 * @param order order to be unvoided
	 */
	public void unvoidOrder(Order order) throws APIException {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ORDERS);
		getOrderDAO().unvoidOrder(order);
	}

	/**
	 * Get all order types
	 * 
	 * @return order types list
	 * @throws APIException
	 */
	public List<OrderType> getOrderTypes() throws APIException {
		return getOrderDAO().getOrderTypes();
	}

	/**
	 * Get orderType by internal identifier
	 * 
	 * @param orderType id
	 * @return orderType with given internal identifier
	 * @throws APIException
	 */
	public OrderType getOrderType(Integer orderTypeId) throws APIException {
		return getOrderDAO().getOrderType(orderTypeId);
	}

	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException {
		return getOrderDAO().getDrugOrdersByPatient(patient);
	}

	public Map<Concept,List<DrugOrder>> getDrugSetsByConcepts(List<DrugOrder> drugOrders, List<Concept> drugSets) throws APIException {
		log.debug("In getDrugSetsByConcepts method");

		Map<Concept, List<DrugOrder>> hmRet = null;
		
		if ( drugSets != null && drugOrders != null ) {
			log.debug("drugSets is size " + drugSets.size());
			for ( Concept c : drugSets ) {
				List<DrugOrder> ordersForConcept = new ArrayList<DrugOrder>();
				
				Collection<ConceptSet> relatedConcepts = c.getConceptSets();
				log.debug("Concept is " + c.getName(Context.getLocale()) + " and has " + relatedConcepts.size() + " related concepts");

				// now we have as a list, let's iterate
				if ( relatedConcepts != null ) {
					for ( ConceptSet cs : relatedConcepts ) {
						Concept csConcept = cs.getConcept();
						for ( DrugOrder currOrder : drugOrders ) {
							Drug currDrug = currOrder.getDrug();
							if ( currDrug != null ) {
								Concept currConcept = currDrug.getConcept();  // must not be null - ordained by data model
								if ( currConcept.equals(csConcept)) {
									ordersForConcept.add(currOrder);
									log.debug("just added an order for " + currDrug.getName() + " to list of " + c.getName(Context.getLocale()));
								}
							}
						}
					}
				}
				
				if ( ordersForConcept.size() > 0 ) {
					if ( hmRet == null ) hmRet = new HashMap<Concept, List<DrugOrder>>();
					hmRet.put(c, ordersForConcept);
					log.debug("Concept " + c.getName(Context.getLocale()) + " was put to the map with a list of size " + ordersForConcept.size());
				}
			}
		} else log.debug("drugSets is null");
		
		return hmRet;
	}

	public List<RegimenSuggestion> getStandardRegimens () {
		DrugOrderSupport dos = null;
		List<RegimenSuggestion> standardRegimens = null;
		
		try {
			dos = DrugOrderSupport.getInstance();
		} catch ( Exception e ) {
			log.error("Error getting instance of DrugOrderSupport object");
		}
		
		if ( dos != null ) {
			standardRegimens = dos.getStandardRegimens();
		} else {
			log.error("DrugOrderSupport object is null after new instance");
		}
		
		return standardRegimens;
	}

	public Map<String, List<DrugOrder>> getDrugSetsByDrugSetIdList(List<DrugOrder> orderList, String drugSetIdList, String delimiter) {
		if ( delimiter == null ) delimiter = ",";
		
		Map<String, List<DrugOrder>> ret = null;
		
		if ( drugSetIdList != null && orderList != null ) {
			List<Concept> drugSetConcepts = new ArrayList<Concept>();
			boolean addOthers = false;
			Map<Concept, String> idToConceptMappings = new HashMap<Concept, String>();
			
			String[] drugSetIds = drugSetIdList.split(delimiter);
			log.debug("starting with " + drugSetIds.length + " items in comma-delimited list, and " + orderList.size() + " orders that are " + orderList);
			for ( String drugSetId : drugSetIds ) {
				// go through and get all concepts for these drugSetIds - then we can call another method to get Map
				
				if ( "*".equals(drugSetId)) {
					// add "other"
					addOthers = true;
				} else {
					Concept drugSetConcept = OpenmrsUtil.getConceptByIdOrName(drugSetId); 
						
					if ( drugSetConcept != null ) {
						drugSetConcepts.add(drugSetConcept);
						idToConceptMappings.put(drugSetConcept, drugSetId);
						log.debug("added concept " + drugSetConcept.getName(Context.getLocale()) + ", and mapping to id " + drugSetId);
					}
				}
			}

			// now we know what drugSet concepts to separate the orderList into
			
			// first, let's create a list of "others", starting with a full list that we remove from
			List<DrugOrder> otherOrders = null;
			if ( addOthers ) otherOrders = orderList;
			
			
			Map<Concept, List<DrugOrder>> ordersByConcepts = getDrugSetsByConcepts(orderList, drugSetConcepts);
			if ( ordersByConcepts != null ) {
				log.debug("obc is size " + ordersByConcepts.size());
				for ( Map.Entry<Concept, List<DrugOrder>> e : ordersByConcepts.entrySet() ) {
					Concept c = e.getKey();
					List<DrugOrder> orders = e.getValue();
					log.debug("found concept " + c.getName(Context.getLocale()) + ", and list is size " + orders.size() + " and list is " + orders);
					if ( addOthers && otherOrders != null ) {
						otherOrders.removeAll(orders);
					}
					if ( ret == null ) ret = new HashMap<String, List<DrugOrder>>();
					log.debug("putting list of size " + orders.size() + " in string " + idToConceptMappings.get(c));
					ret.put(idToConceptMappings.get(c), orders);
				}
			}
			
			// add the "others" list to the Map
			if ( addOthers && otherOrders != null ) {
				if ( ret == null ) ret = new HashMap<String, List<DrugOrder>>();
				ret.put("*", otherOrders);
			}
		}
		
		return ret;
	}

	public Map<String, String> getDrugSetHeadersByDrugSetIdList(String drugSetIds) {
		Map<String, String> ret = null;
		
		if ( drugSetIds != null ) {
			Map<String, Concept> concepts = OpenmrsUtil.delimitedStringToConceptMap(drugSetIds, ",");
			if ( concepts != null ) {
				for ( Map.Entry<String, Concept> e : concepts.entrySet() ) {
					String id = e.getKey();
					Concept concept = e.getValue();
					if ( ret == null ) ret = new HashMap<String, String>();
					ret.put(id, concept.getName(Context.getLocale()).getName());
				}
			}
		}
		
		return ret;
	}

	public void discontinueDrugSet(Patient patient, String drugSetId, Concept discontinueReason, Date discontinueDate) {
		log.debug("in discontinueDrugSet() method with " + drugSetId);
		if (Context.isAuthenticated() && patient != null && drugSetId != null && discontinueDate != null ) {
			List<DrugOrder> currentOrders = this.getDrugOrdersByPatient(patient, OrderService.SHOW_CURRENT);
			Map<String, List<DrugOrder>> ordersBySetId = this.getDrugSetsByDrugSetIdList(currentOrders, drugSetId, ",");
			if ( ordersBySetId != null ) {
				List<DrugOrder> ordersToDiscontinue = ordersBySetId.get(drugSetId);
				if ( ordersToDiscontinue != null ) {
					for ( DrugOrder order : ordersToDiscontinue ) {
						this.discontinueOrder(order, discontinueReason, discontinueDate);
					}
				} else log.debug("no orders to discontinue");
			} else log.debug("no ordersBySetId returned for " + drugSetId);
		}
		
		
	}

	public void voidDrugSet(Patient patient, String drugSetId, String voidReason, int whatToVoid) {
		log.debug("in voidDrugSet() method");
		if (Context.isAuthenticated() && patient != null && drugSetId != null ) {
			List<DrugOrder> currentOrders = this.getDrugOrdersByPatient(patient, whatToVoid);
			Map<String, List<DrugOrder>> ordersBySetId = this.getDrugSetsByDrugSetIdList(currentOrders, drugSetId, ",");
			if ( ordersBySetId != null ) {
				List<DrugOrder> ordersToVoid = ordersBySetId.get(drugSetId);
				if ( ordersToVoid != null ) {
					for ( DrugOrder order : ordersToVoid ) {
						this.voidOrder(order, voidReason);
					}
				}
			}
		}		
	}
	
	public void discontinueAllOrders(Patient patient, Concept discontinueReason, Date discontinueDate) {
		log.debug("In discontinueAll with patient " + patient + " and concept " + discontinueReason + " and date " + discontinueDate);
		
		List<DrugOrder> drugOrders = this.getDrugOrdersByPatient(patient, OrderService.SHOW_CURRENT);
		if ( drugOrders != null ) {
			for (DrugOrder drugOrder : drugOrders) {
				log.debug("discontinuing order: " + drugOrder);
				this.discontinueOrder(drugOrder, discontinueReason, discontinueDate);
			}
		}
	}
}

