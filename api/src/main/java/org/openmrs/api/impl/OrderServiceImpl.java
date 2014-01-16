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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.GenericDrug;
import org.openmrs.Order;
import org.openmrs.Order.OrderAction;
import org.openmrs.Orderable;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.DrugOrderSupport;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the Order-related services class. This class should not be invoked by
 * itself. Spring injection is used to inject this implementation into the ServiceContext. Which
 * implementation is injected is determined by the spring application context file:
 * /metadata/api/spring/applicationContext.xml
 * 
 * @see org.openmrs.api.OrderService
 */
@Transactional
public class OrderServiceImpl extends BaseOpenmrsService implements OrderService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected OrderDAO dao;
	
	/**
	 * Used to store the last used order number
	 */
	private static Integer orderNumberCounter = -1;
	
	public OrderServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.OrderService#setOrderDAO(org.openmrs.api.db.OrderDAO)
	 */
	@Override
	public void setOrderDAO(OrderDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order)
	 */
	@Override
	public Order saveOrder(Order order) throws APIException {
		
		checkIfModifyingSavedOrderNumber(order);
		
		//No editing of orders. We instead create a new one.
		if (order.getOrderId() != null) {
			Order newOrder = new Order();
			newOrder.setConcept(order.getConcept());
			newOrder.setPatient(order.getPatient());
			newOrder.setOrderNumber(order.getOrderNumber());
			newOrder.setStartDate(new Date());
			newOrder.setUuid(UUID.randomUUID().toString());
			
			return saveOrderWithLesserValidation(newOrder);
		} else
			return saveOrderWithLesserValidation(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(org.openmrs.Order)
	 */
	@Override
	public void purgeOrder(Order order) throws APIException {
		Context.getOrderService().purgeOrder(order, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrder(Order)
	 */
	public void purgeOrder(Order order, boolean cascade) throws APIException {
		if (cascade) {
			dao.deleteObsThatReference(order);
		}
		dao.deleteOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#voidOrder(org.openmrs.Order, java.lang.String)
	 */
	@Override
	public Order voidOrder(Order order, String voidReason) throws APIException {
		return dao.saveOrder(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#unvoidOrder(org.openmrs.Order)
	 */
	@Override
	public Order unvoidOrder(Order order) throws APIException {
		return saveOrderWithLesserValidation(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Order getOrder(Integer orderId) throws APIException {
		return Context.getOrderService().getOrder(orderId, Order.class);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer, java.lang.Class)
	 */
	@Override
	@Transactional(readOnly = true)
	public <o extends Order> o getOrder(Integer orderId, Class<o> orderClassType) throws APIException {
		return dao.getOrder(orderId, orderClassType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.Date, java.util.List, java.util.List)
	 */
	@Transactional(readOnly = true)
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters, Date asOfDate,
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude) {
		
		return getOrders(orderClassType, patients, concepts, orderers, encounters, asOfDate, actionsToInclude,
		    actionsToExclude, false);
	}
	
	private <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters, Date asOfDate,
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude, boolean includeVoided) {
		if (orderClassType == null)
			throw new APIException("orderClassType cannot be null.  An order type of Order.class or a subclass is required");
		
		return dao.getOrders(orderClassType, patients, concepts, orderers, encounters, asOfDate, actionsToInclude,
		    actionsToExclude, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByPatient(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrdersByPatient(Patient patient) throws APIException {
		return Context.getOrderService().getOrdersByPatient(patient, false);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, boolean includeVoided) {
		if (patient == null)
			throw new APIException("Unable to get drug orders if not given a patient");
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		// TODO: could use this call to get only ACTIVE.  Getting complete is done with asOfDate.
		// with those two calls you can get rid of all this extra logic
		List<DrugOrder> drugOrders = Context.getOrderService().getOrders(DrugOrder.class, patients, null, null, null, null,
		    null, null);
		
		// loop over the drug orders and add them if they are within the current desired order
		if (drugOrders != null) {
			if (includeVoided)
				return drugOrders;
			
			//TODO should be done with a query
			//remove voided ones
			List<DrugOrder> ret = new ArrayList<DrugOrder>();
			
			for (DrugOrder drugOrder : drugOrders) {
				if (!drugOrder.getVoided())
					ret.add(drugOrder);
			}
			
			return ret;
		}
		
		// default return if no drug orders were found in the database
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatient(org.openmrs.Patient)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException {
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		return Context.getOrderService().getOrders(DrugOrder.class, patients, null, null, null, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getStandardRegimens()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RegimenSuggestion> getStandardRegimens() {
		DrugOrderSupport dos = null;
		List<RegimenSuggestion> standardRegimens = null;
		
		try {
			dos = DrugOrderSupport.getInstance();
		}
		catch (Exception e) {
			log.error("Error getting instance of DrugOrderSupport object", e);
		}
		
		if (dos != null) {
			standardRegimens = dos.getStandardRegimens();
		} else {
			log.error("DrugOrderSupport object is null after new instance");
		}
		
		return standardRegimens;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Order getOrderByUuid(String uuid) throws APIException {
		return dao.getOrderByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderByOrderNumber(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Order getOrderByOrderNumber(String orderNumber) {
		return dao.getOrderByOrderNumber(orderNumber);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderHistoryByConcept(org.openmrs.Patient,
	 *      org.openmrs.Concept)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrderHistoryByConcept(Patient patient, Concept concept) {
		if (patient == null)
			throw new IllegalArgumentException("patient is required");
		
		List<Concept> concepts = new Vector<Concept>();
		concepts.add(concept);
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(patient);
		
		return Context.getOrderService().getOrders(Order.class, patients, concepts, null, null, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getActiveOrdersByPatient(org.openmrs.Patient,
	 *      java.util.Date)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Order> getActiveOrdersByPatient(Patient p, Date date) throws APIException {
		
		if (p == null)
			throw new IllegalArgumentException("patient is required");
		
		if (date == null)
			date = new Date();
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(p);
		
		return Context.getOrderService().getOrders(Order.class, patients, null, null, null, date, null,
		    Arrays.asList(OrderAction.DISCONTINUE));
		
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getActiveDrugOrdersByPatient(org.openmrs.Patient,
	 *      java.util.Date)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<DrugOrder> getActiveDrugOrdersByPatient(Patient p, Date date) {
		if (p == null)
			throw new IllegalArgumentException("patient is required");
		
		if (date == null)
			date = new Date();
		
		List<Patient> patients = new Vector<Patient>();
		patients.add(p);
		
		// TODO: add "NOT discontinued" action to this call
		return Context.getOrderService().getOrders(DrugOrder.class, patients, null, null, null, date, null,
		    Arrays.asList(OrderAction.DISCONTINUE));
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderables(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Orderable<?>> getOrderables(String query) throws APIException {
		
		if (query == null)
			throw new IllegalArgumentException("Orderable concept name is required");
		
		List<Orderable<?>> result = new ArrayList<Orderable<?>>();
		
		// then look for concepts that are drugs
		List<Concept> concepts = Context.getConceptService().getConceptsByName(query);
		if (concepts != null) {
			for (Concept concept : concepts) {
				if (concept.getConceptClass().getUuid().equals(ConceptClass.DRUG_UUID))
					result.add(new GenericDrug(concept));
			}
		}
		
		// and next to try to find drugs by name
		List<Drug> drugs = Context.getConceptService().getDrugs(query);
		if (drugs != null) {
			for (Drug drug : drugs) {
				if (!drug.isRetired())
					result.add(drug);
			}
		}
		
		return result;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, java.lang.String,
	 *      org.openmrs.User, java.util.Date)
	 */
	@Override
	public Order discontinueOrder(Order order, String reason, User user, Date discontinueDate) throws APIException {
		order.setDiscontinuedReason(reason);
		return doDiscontinueOrder(order, user, discontinueDate);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, java.lang.String)
	 */
	@Override
	public Order discontinueOrder(Order order, String reason) throws APIException {
		return Context.getOrderService().discontinueOrder(order, reason, null, null);
	}
	
	/**
	 * Utility method that discontinues an order
	 * 
	 * @param oldOrder the order to discontinue
	 * @param user the user discontinuing the order, default to authenticated user
	 * @param discontinueDate the date when to discontinue the order, default to now. Note that this
	 *            method only uses the passed in discontinue date if it is a future date and the
	 *            order is not yet activated
	 * @return the discontinued order
	 * @throws APIException
	 */
	private Order doDiscontinueOrder(Order oldOrder, User user, Date discontinueDate) throws APIException {
		if (user == null)
			user = Context.getAuthenticatedUser();
		if (discontinueDate == null)
			discontinueDate = new Date();
		else if (discontinueDate.after(new Date()))
			throw new APIException("Cannot discontinue an order in the future");
		if (oldOrder.getDiscontinued())
			throw new APIException("Cannot discontinue an order that is already discontinued");
		if (oldOrder.getAutoExpireDate() != null && OpenmrsUtil.compare(discontinueDate, oldOrder.getAutoExpireDate()) > 0)
			throw new APIException("Cannot discontinue an order after its autoexpire date");
		
		oldOrder.setDiscontinued(Boolean.TRUE);
		oldOrder.setDiscontinuedBy(user);
		oldOrder.setDiscontinuedDate(discontinueDate);
		
		saveOrderWithLesserValidation(oldOrder);
		
		Order newOrder = new Order();
		newOrder.setOrderNumber(getNewOrderNumber());
		newOrder.setConcept(oldOrder.getConcept());
		newOrder.setPatient(oldOrder.getPatient());
		newOrder.setPreviousOrderNumber(oldOrder.getOrderNumber());
		newOrder.setOrderAction(OrderAction.DISCONTINUE);
		newOrder.setStartDate(new Date());
		newOrder.setUuid(UUID.randomUUID().toString());
		
		Context.getOrderService().saveOrder(newOrder);
		
		return oldOrder;
	}
	
	/**
	 * Convenience method to be called within the API to enable persisting changes in an existing
	 * order while surpassing certain validation constraints e.g when discontinuing an order
	 * 
	 * @param order
	 * @return
	 * @throws APIException
	 */
	private Order saveOrderWithLesserValidation(Order order) throws APIException {
		checkIfModifyingSavedOrderNumber(order);
		return dao.saveOrder(order);
	}
	
	private void checkIfModifyingSavedOrderNumber(Order order) {
		String orderNumberInDatabase = dao.getOrderNumberInDatabase(order);
		if (orderNumberInDatabase != null && !orderNumberInDatabase.equals(order.getOrderNumber()))
			throw new APIException("Cannot modify the orderNumber of a saved order");
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderable(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Orderable<?> getOrderable(String identifier) throws APIException {
		if (identifier == null)
			throw new IllegalArgumentException("Orderable identifier is required");
		
		Integer numericIdentifier = GenericDrug.getNumericIdentifier(identifier);
		if (numericIdentifier != null) {
			Concept concept = Context.getConceptService().getConcept(numericIdentifier);
			if (concept != null) {
				return new GenericDrug(concept);
			}
		}
		
		numericIdentifier = Drug.getNumericIdentifier(identifier);
		if (numericIdentifier != null) {
			Drug drug = Context.getConceptService().getDrug(numericIdentifier);
			if (drug != null) {
				return drug;
			}
		}
		
		//Do we have other types to check?
		
		return null;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getNewOrderNumber()
	 */
	@Override
	@Transactional(readOnly = true)
	public String getNewOrderNumber() {
		Integer next;
		synchronized (orderNumberCounter) {
			if (orderNumberCounter < 0) {
				// we've just started up, so we need to fetch this from the DAO
				Integer temp = dao.getHighestOrderId();
				orderNumberCounter = temp == null ? 0 : temp;
			}
			orderNumberCounter += 1;
			next = orderNumberCounter;
		}
		
		return Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_ORDER_ENTRY_ORDER_NUMBER_PREFIX,
		    OpenmrsConstants.ORDER_NUMBER_DEFAULT_PREFIX)
		        + next;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getDrugOrdersByPatientAndIngredient(org.openmrs.Patient,
	 *      org.openmrs.Concept)
	 */
	@Transactional(readOnly = true)
	public List<DrugOrder> getDrugOrdersByPatientAndIngredient(Patient patient, Concept ingredient) throws APIException {
		if (patient == null)
			throw new IllegalArgumentException("patient is required");
		
		if (ingredient == null)
			throw new IllegalArgumentException("ingredient is required");
		
		return dao.getDrugOrdersByPatientAndIngredient(patient, ingredient);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByPatient(org.openmrs.Patient, java.lang.Boolean)
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrdersByPatient(Patient patient, boolean includeVoided) throws APIException {
		if (patient == null)
			throw new APIException("Unable to get orders if I am not given a patient");
		
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);
		
		return getOrders(Order.class, patients, null, null, null, null, null, null, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByEncounter(org.openmrs.Encounter)
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrdersByEncounter(Encounter encounter) throws APIException {
		if (encounter == null)
			throw new APIException("Unable to get orders if I am not given an encounter");
		
		List<Encounter> encounters = new ArrayList<Encounter>();
		encounters.add(encounter);
		
		return Context.getOrderService().getOrders(Order.class, null, null, null, encounters, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrdersByOrderer(org.openmrs.User)
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrdersByOrderer(User orderer) throws APIException {
		if (orderer == null)
			throw new APIException("Unable to get orders if I am not given an orderer");
		
		List<User> orderers = new ArrayList<User>();
		orderers.add(orderer);
		
		return Context.getOrderService().getOrders(Order.class, null, null, orderers, null, null, null, null);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderHistoryByOrderNumber(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public List<Order> getOrderHistoryByOrderNumber(String orderNumber) {
		return dao.getOrderHistoryByOrderNumber(orderNumber);
	}
	
}
