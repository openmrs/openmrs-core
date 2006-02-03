package org.openmrs.api;

import java.util.List;

import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Order-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class OrderService {

	private Context context;
	private DAOContext daoContext;
	
	public OrderService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private OrderDAO getOrderDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ORDERS);
		
		return daoContext.getOrderDAO();
	}
	
	/**
	 * Creates a new order record
	 * 
	 * @param order to be created
	 * @throws APIException
	 */
	public void createOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_ORDERS);
		getOrderDAO().createOrder(order);
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

	/**
	 * Update order 
	 * 
	 * @param Order order to update
	 * @throws APIException
	 */
	public void updateOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ORDERS);
		getOrderDAO().updateOrder(order);
	}
	
	/**
	 * Get order by orderer
	 * 
	 * @param User orderer
	 * @return orders that were ordered by given User
	 * @throws APIException
	 */
//	public Order getOrder(User orderer) throws APIException {
		
	/**
	 * Discontinue order record
	 * 
	 * @param order order to be discontinued
	 * @param reason reason for discontinuing order
	 */
	public void discontinueOrder(Order order, String reason) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ORDERS);
		getOrderDAO().discontinueOrder(order, reason);
	}

	/**
	 * Undiscontinue order record
	 * 
	 * @param order order to be undiscontinued
	 */
	public void undiscontinueOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ORDERS);
		getOrderDAO().undiscontinueOrder(order);
	}

	
	/**
	 * Delete order from database. This <b>should not be called</b>
	 * except for testing and administration purposes.  Use the discontinue
	 * method instead.
	 * 
	 * @param orderId internal identifier of order to be deleted
	 * 
	 * @see #discontinueOrder(Order, String) 
	 */
	public void deleteOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_ORDERS);
		getOrderDAO().deleteOrder(order);
	}

	/**
	 * Void order record
	 * 
	 * @param order order to be voided
	 * @param reason reason for voiding order
	 */
	public void voidOrder(Order order, String reason) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ORDERS);
		getOrderDAO().voidOrder(order, reason);
	}

	/**
	 * Unvoid order record
	 * 
	 * @param order order to be unvoided
	 */
	public void unvoidOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ORDERS))
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
	
}
