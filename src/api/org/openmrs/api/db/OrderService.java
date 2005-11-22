package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.User;

public interface OrderService {

	/**
	 * Creates a new order record
	 * 
	 * @param order to be created
	 * @throws APIException
	 */
	public void createOrder(Order order) throws APIException;

	/**
	 * Get order by internal identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 */
	public Order getOrder(Integer orderId) throws APIException;

	/**
	 * Update order 
	 * 
	 * @param Order order to update
	 * @throws APIException
	 */
	public void updateOrder(Order order) throws APIException;
	
	/**
	 * Get order by orderer
	 * 
	 * @param User orderer
	 * @return orders that were ordered by given User
	 * @throws APIException
	 */
	//public Order getOrder(User orderer) throws APIException;
		
	/**
	 * Discontinue order record
	 * 
	 * @param order order to be discontinued
	 * @param reason reason for discontinuing order
	 */
	public void discontinueOrder(Order order, String reason) throws APIException;

	/**
	 * Undiscontinue order record
	 * 
	 * @param order order to be undiscontinued
	 */
	public void undiscontinueOrder(Order order) throws APIException;

	
	/**
	 * Delete order from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the discontinue
	 * method instead.
	 * 
	 * @param orderId internal identifier of order to be deleted
	 * 
	 * @see #discontinueOrder(Order, String) 
	 */
	public void deleteOrder(Order order) throws APIException;

	/**
	 * Void order record
	 * 
	 * @param order order to be voided
	 * @param reason reason for voiding order
	 */
	public void voidOrder(Order order, String reason) throws APIException;

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
	public List<OrderType> getOrderTypes() throws APIException;

	/**
	 * Get orderType by internal identifier
	 * 
	 * @param orderType id
	 * @return orderType with given internal identifier
	 * @throws APIException
	 */
	public OrderType getOrderType(Integer orderTypeId) throws APIException;
	
}
