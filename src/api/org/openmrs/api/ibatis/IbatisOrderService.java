package org.openmrs.api.ibatis;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.OrderService
 * 
 * @see org.openmrs.api.OrderService
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class IbatisOrderService implements OrderService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	/**
	 * Service must be constructed within a <code>context</code>
	 * @param context
	 * @see org.openmrs.context.Context
	 */
	public IbatisOrderService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.OrderService#createOrder(Order)
	 */
	public Order createOrder(Order order) throws APIException {
		try {
			SqlMap.instance().insert("createOrder", order);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return order;
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrder(Integer)
	 */
	public Order getOrder(Integer orderId) throws APIException {
		Order order;
		try {
			order = (Order) SqlMap.instance().queryForObject("getOrder", orderId);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return order;
	}

	/**
	 * @see org.openmrs.api.OrderService#updateOrder(Order)
	 */
	public void updateOrder(Order order) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (order.getCreator() == null) {
					order.setCreator(context.getAuthenticatedUser());
					SqlMap.instance().insert("createOrder", order);
				} else {
					SqlMap.instance().update("updateOrder", order);
				}
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

	}

	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(Order, String)
	 */
	public void discontinueOrder(Order order, String reason) throws APIException {
		//order.setDiscontinued(true);
		order.setDiscontinuedBy(context.getAuthenticatedUser());
		order.setDiscontinuedReason(reason);
		try {
			SqlMap.instance().update("discontinueOrder", order);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.OrderService#undiscontinueOrder(Order)
	 */
	public void undiscontinueOrder(Order order) {
		//order.setChangedBy(context.getAuthenticatedUser());
		try {
			SqlMap.instance().update("undiscontinueOrder", order);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.OrderService#deleteOrder(Order)
	 */
	public void deleteOrder(Order order) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteOrder", order);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

}
