package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.order.RegimenSuggestion;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrderService {

	public static final int SHOW_CURRENT = 1;

	public static final int SHOW_ALL = 2;

	public static final int SHOW_COMPLETE = 3;

	public static final int SHOW_NOTVOIDED = 4;

	public void setOrderDAO(OrderDAO dao);

	/**
	 * Create a new Order
	 * @param Order to create
	 * @throws APIException
	 */
	public void createOrder(Order order) throws APIException;

	/**
	 * Update Order
	 * @param Order to update
	 * @throws APIException
	 */
	public void updateOrder(Order order) throws APIException;
	
	/**
	 * Delete Order
	 * @param Order to delete
	 * @throws APIException
	 */
	public void deleteOrder(Order order) throws APIException;

	/**
	 * Void Order
	 * @param voidReason 
	 * @param Order to void
	 * @throws APIException
	 */
	public void voidOrder(Order order, String voidReason) throws APIException;

	/**
	 * Void Order
	 * @param voidReason 
	 * @param Order to void
	 * @throws APIException
	 */
	public void discontinueOrder(Order order, Concept discontinueReason,
			Date discontinueDate) throws APIException;

	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws APIException
	 */
	public void createOrderType(OrderType orderType) throws APIException;

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException;

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException;

	/**
	 * Creates a collection of orders and an encounter to hold them. orders[i].encounter will be set to the new encounter.
	 * If there's an EncounterType with name "Regimen Change", then the newly-created encounter will have that type
	 * @throws APIException if there is no User with username Unknown or no Location with name Unknown.
	 */
	public void createOrdersAndEncounter(Patient p, Collection<Order> orders)
			throws APIException;

	/**
	 * Get order by internal identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Order getOrder(Integer orderId) throws APIException;

	/**
	 * Get all orders
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Order> getOrders() throws APIException;

	/**
	 * Get all drug orders
	 * 
	 * @return drug orders list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<DrugOrder> getDrugOrders() throws APIException;

	/**
	 * Get all orders by User
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Order> getOrdersByUser(User user) throws APIException;

	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Order> getOrdersByPatient(Patient patient) throws APIException;

	@Transactional(readOnly=true)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient,
			int whatToShow);

	/**
	 * Undiscontinue order record
	 * 
	 * @param order order to be undiscontinued
	 */
	public void undiscontinueOrder(Order order) throws APIException;

	/**
	 * Unvoid order record
	 * 
	 * @param order order to be unvoided
	 */
	public void unvoidOrder(Order order) throws APIException;

	/**
	 * Get all order types
	 * 
	 * @return order types list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<OrderType> getOrderTypes() throws APIException;

	/**
	 * Get orderType by internal identifier
	 * 
	 * @param orderType id
	 * @return orderType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public OrderType getOrderType(Integer orderTypeId) throws APIException;

	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient)
			throws APIException;

	@Transactional(readOnly=true)
	public Map<Concept, List<DrugOrder>> getDrugSetsByConcepts(
			List<DrugOrder> drugOrders, List<Concept> drugSets)
			throws APIException;

	@Transactional(readOnly=true)
	public List<RegimenSuggestion> getStandardRegimens();

	@Transactional(readOnly=true)
	public Map<String, List<DrugOrder>> getDrugSetsByDrugSetIdList(
			List<DrugOrder> orderList, String drugSetIdList, String delimiter);

	@Transactional(readOnly=true)
	public Map<String, String> getDrugSetHeadersByDrugSetIdList(
			String drugSetIds);

	public void discontinueDrugSet(Patient patient, String drugSetId,
			Concept discontinueReason, Date discontinueDate);

	public void voidDrugSet(Patient patient, String drugSetId,
			String voidReason, int whatToVoid);

	public void discontinueAllOrders(Patient patient, Concept discontinueReason, Date discontinueDate) throws APIException;
}