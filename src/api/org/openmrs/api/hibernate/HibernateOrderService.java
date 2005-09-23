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
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		order.setCreator(context.getAuthenticatedUser());
		order.setDateCreated(new Date());
		session.save(order);
		
		tx.commit();
		HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.OrderService#deleteOrder(org.openmrs.Order)
	 */
	public void deleteOrder(Order order) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(order);
		
		tx.commit();
		HibernateUtil.closeSession();
		
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	public Order getOrder(Integer orderId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Order order = new Order();
		order = (Order)session.get(Order.class, orderId);
		
		HibernateUtil.closeSession();
		
		return order;
	}

	/**
	 * @see org.openmrs.api.OrderService#updateOrder(org.openmrs.Order)
	 */
	public void updateOrder(Order order) {
		
		if (order.getOrderId() == null)
			createOrder(order);
		else {
			Session session = HibernateUtil.currentSession();
			
			session.saveOrUpdate(order);
			//HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.OrderService#voidOrder(org.openmrs.Order, java.lang.String)
	 */
	public void voidOrder(Order order, String reason) {
		order.setVoided(false);
		order.setVoidedBy(context.getAuthenticatedUser());
		order.setDateVoided(new Date());
		order.setVoidReason(reason);
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.OrderService#discontinueOrder(org.openmrs.Order, java.lang.String)
	 */
	public void discontinueOrder(Order order, String reason) throws APIException {
		order.setDiscontinued(false);
		order.setDiscontinuedBy(context.getAuthenticatedUser());
		order.setDiscontinuedDate(new Date());
		order.setVoidReason(reason);
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		OrderType orderType = (OrderType)session.get(OrderType.class, orderTypeId);
		
		//HibernateUtil.closeSession();
		
		return orderType;
	}

	/**
	 * @see org.openmrs.api.OrderService#getOrderTypes()
	 */
	public List<OrderType> getOrderTypes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		return session.createCriteria(OrderType.class).list();
		
		//HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.OrderService#undiscontinueOrder(org.openmrs.Order)
	 */
	public void undiscontinueOrder(Order order) throws APIException {
		order.setDiscontinued(true);
		order.setDiscontinuedBy(null);
		order.setDiscontinuedDate(null);
		order.setDiscontinuedReason("");
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.OrderService#unvoidOrder(org.openmrs.Order)
	 */
	public void unvoidOrder(Order order) throws APIException {
		order.setVoided(true);
		order.setVoidedBy(null);
		order.setDateVoided(null);
		order.setVoidReason("");
		updateOrder(order);
	}	
	
}
