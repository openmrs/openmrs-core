package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.context.Context;

public class HibernateOrderService implements
		OrderService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateOrderService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.OrderService#createOrder(org.openmrs.Order)
	 */
	public void createOrder(Order order) throws APIException {
		log.debug("creating order");
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		order.setCreator(context.getAuthenticatedUser());
		order.setDateCreated(new Date());
		session.save(order);
		
		tx.commit();
		HibernateUtil.disconnectSession();
	}

	/**
	 * @see org.openmrs.api.OrderService#deleteOrder(org.openmrs.Order)
	 */
	public void deleteOrder(Order order) throws APIException {
		log.debug("deleting order #" + order.getOrderId());
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(order);
		
		tx.commit();
		HibernateUtil.disconnectSession();
		
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	public Order getOrder(Integer orderId) throws APIException {
		log.debug("getting order #" + orderId);
		
		Session session = HibernateUtil.currentSession();
		
		Order order = new Order();
		order = (Order)session.get(Order.class, orderId);
		
		HibernateUtil.disconnectSession();
		
		return order;
	}

	/**
	 * @see org.openmrs.api.OrderService#updateOrder(org.openmrs.Order)
	 */
	public void updateOrder(Order order) {
		log.debug("updating order #" + order.getOrderId());
		
		if (order.getOrderId() == null)
			createOrder(order);
		else {
			Session session = HibernateUtil.currentSession();
			
			session.merge(order);
			session.update(order);
			HibernateUtil.disconnectSession();
		}
	}

	/**
	 * @see org.openmrs.api.OrderService#voidOrder(org.openmrs.Order, java.lang.String)
	 */
	public void voidOrder(Order order, String reason) {
		log.debug("voiding order #" + order.getOrderId());
		
		order.setVoided(true);
		order.setVoidedBy(context.getAuthenticatedUser());
		order.setDateVoided(new Date());
		order.setVoidReason(reason);
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, java.lang.String)
	 */
	public void discontinueOrder(Order order, String reason) throws APIException {
		log.debug("discontinuing order #" + order.getOrderId());
		
		order.setDiscontinued(true);
		order.setDiscontinuedBy(context.getAuthenticatedUser());
		order.setDiscontinuedDate(new Date());
		order.setDiscontinuedReason(reason);
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws APIException {
		log.debug("getting orderType #" + orderTypeId);

		Session session = HibernateUtil.currentSession();
		
		OrderType orderType = (OrderType)session.get(OrderType.class, orderTypeId);
		
		HibernateUtil.disconnectSession();
		
		return orderType;
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrderTypes()
	 */
	public List<OrderType> getOrderTypes() throws APIException {
		log.debug("getting all order types");

		Session session = HibernateUtil.currentSession();
		
		List<OrderType> orderTypes = session.createCriteria(OrderType.class).list();
		
		HibernateUtil.disconnectSession();
		
		return orderTypes;
	}

	/**
	 * @see org.openmrs.api.OrderService#undiscontinueOrder(org.openmrs.Order)
	 */
	public void undiscontinueOrder(Order order) throws APIException {
		log.debug("undiscontinuing order #" + order.getOrderId());

		order.setDiscontinued(false);
		order.setDiscontinuedBy(null);
		order.setDiscontinuedDate(null);
		order.setDiscontinuedReason("");
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.OrderService#unvoidOrder(org.openmrs.Order)
	 */
	public void unvoidOrder(Order order) throws APIException {
		log.debug("unvoiding order #" + order.getOrderId());
		
		order.setVoided(false);
		order.setVoidedBy(null);
		order.setDateVoided(null);
		order.setVoidReason("");
		updateOrder(order);
	}	
	
}
