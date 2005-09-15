package org.openmrs.api.ibatis;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.OrderService
 * 
 * @see org.openmrs.api.OrderService
 * 
 * @author Ben Wolfe
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
			order.setCreator(context.getAuthenticatedUser());
			SqlMap.instance().insert("createOrder", order);
			if (order.isDrugOrder())
				SqlMap.instance().insert("createDrugOrder", order);
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
		DrugOrder drugOrder;
		DrugOrder tmpDrugOrder;
		try {
			order = (Order) SqlMap.instance().queryForObject("getOrder", orderId);
			tmpDrugOrder = (DrugOrder) SqlMap.instance().queryForObject("getDrugOrder", orderId);
			if (tmpDrugOrder != null) {
				drugOrder = (DrugOrder) order;
				drugOrder.setDrug(tmpDrugOrder.getDrug());
				drugOrder.setDose(tmpDrugOrder.getDose());
				drugOrder.setUnits(tmpDrugOrder.getUnits());
				drugOrder.setFrequency(tmpDrugOrder.getFrequency());
				drugOrder.setPrn(tmpDrugOrder.isPrn());
				drugOrder.setComplex(tmpDrugOrder.isComplex());
				drugOrder.setQuantity(tmpDrugOrder.getQuantity());
				drugOrder.setCreator(tmpDrugOrder.getCreator());
				drugOrder.setDateCreated(tmpDrugOrder.getDateCreated());
				order = drugOrder;
			}
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
					this.createOrder(order);
				} else {
					SqlMap.instance().update("updateOrder", order);
					if (order.isDrugOrder())
						SqlMap.instance().update("updateDrugOrder", order);
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
	// TODO discontinue drug order too?
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
				if (order.isDrugOrder())
					SqlMap.instance().delete("deleteDrugOrder", order);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.OrderService#voidOrder(Order, String)
	 */
	public void voidOrder(Order order, String reason) throws APIException {
		//order.setVoided(true);
		order.setVoidedBy(context.getAuthenticatedUser());
		order.setVoidReason(reason);
		try {
			SqlMap.instance().update("voidOrder", order);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.OrderService#unvoidOrder(Order)
	 */
	// TODO changedBy for Order ?
	public void unvoidOrder(Order order) {
		//order.setChangedBy(context.getAuthenticatedUser());
		try {
			SqlMap.instance().update("unvoidOrder", order);
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws APIException {

		OrderType orderType;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				orderType = (OrderType)SqlMap.instance().queryForObject("getOrderType", orderTypeId);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return orderType;
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrderTypes()
	 */
	public List<OrderType> getOrderTypes() throws APIException {
		
		List<OrderType> orderTypes;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				orderTypes = SqlMap.instance().queryForList("getAllOrderTypes", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return orderTypes;
	}
	
}
