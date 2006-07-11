package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Order;
import org.openmrs.OrderType;

/**
 * Order-related database functions
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public interface OrderDAO {

	/**
	 * Creates a new order record
	 * 
	 * @param order to be created
	 * @throws DAOException
	 */
	public void createOrder(Order order) throws DAOException;

	/**
	 * Get order by internal identifier
	 * 
	 * @param orderId internal order identifier
	 * @return order with given internal identifier
	 * @throws DAOException
	 */
	public Order getOrder(Integer orderId) throws DAOException;

	/**
	 * Update order 
	 * 
	 * @param Order order to update
	 * @throws DAOException
	 */
	public void updateOrder(Order order) throws DAOException;
	
	/**
	 * Get order by orderer
	 * 
	 * @param User orderer
	 * @return orders that were ordered by given User
	 * @throws DAOException
	 */
	//public Order getOrder(User orderer) throws DAOException;
		
	/**
	 * Discontinue order record
	 * 
	 * @param order order to be discontinued
	 * @param reason reason for discontinuing order
	 */
	public void discontinueOrder(Order order, String reason) throws DAOException;

	/**
	 * Undiscontinue order record
	 * 
	 * @param order order to be undiscontinued
	 */
	public void undiscontinueOrder(Order order) throws DAOException;

	
	/**
	 * Delete order from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the discontinue
	 * method instead.
	 * 
	 * @param orderId internal identifier of order to be deleted
	 * 
	 * @see #discontinueOrder(Order, String) 
	 */
	public void deleteOrder(Order order) throws DAOException;

	/**
	 * Void order record
	 * 
	 * @param order order to be voided
	 * @param reason reason for voiding order
	 */
	public void voidOrder(Order order, String reason) throws DAOException;

	/**
	 * Unvoid order record
	 * 
	 * @param order order to be unvoided
	 */
	public void unvoidOrder(Order order) throws DAOException;

	/**
	 * Get all order types
	 * 
	 * @return order types list
	 * @throws DAOException
	 */
	public List<OrderType> getOrderTypes() throws DAOException;

	/**
	 * Get orderType by internal identifier
	 * 
	 * @param orderType id
	 * @return orderType with given internal identifier
	 * @throws DAOException
	 */
	public OrderType getOrderType(Integer orderTypeId) throws DAOException;
	
}
