package org.openmrs.api;

import org.openmrs.Order;

public interface OrderService {

	/**
	 * Creates a new order record
	 * 
	 * @param order to be created
	 * @return newly created order
	 * @throws APIException
	 */
	public Order createOrder(Order order) throws APIException;

	/**
	 * Get order by internal identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws APIException
	 */
	public Order getOrder(Integer orderId) throws APIException;
		
	/**
	 * Discontinue order record (functionally delete order from system)
	 * 
	 * @param order order to be discontinued
	 * @param reason reason for voiding order
	 */
	public void discontinueOrder(Order order, String reason) throws APIException;
	
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
	
}
