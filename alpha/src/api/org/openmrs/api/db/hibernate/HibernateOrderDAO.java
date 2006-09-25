package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.openmrs.ConceptSet;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.OrderDAO;

public class HibernateOrderDAO implements
		OrderDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateOrderDAO() { }
	
	public HibernateOrderDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public void createOrderType(OrderType orderType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		orderType.setCreator(context.getAuthenticatedUser());
		orderType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(orderType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateOrderType(org.openmrs.OrderType)
	 */
	public void updateOrderType(OrderType orderType) throws DAOException {
		if (orderType.getOrderTypeId() == null)
			createOrderType(orderType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(orderType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(orderType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public void createOrder(Order order) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		order.setCreator(context.getAuthenticatedUser());
		order.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(order);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrder(org.openmrs.Order)
	 */
	public void deleteOrder(Order order) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(order);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrder(org.openmrs.Order)
	 */
	public void voidOrder(Order order, String voidReason) throws DAOException {
		order.setVoided(new Boolean(true));
		order.setVoidReason(voidReason);
		order.setVoidedBy(context.getAuthenticatedUser());
		order.setDateVoided(new Date());
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrder(java.lang.Integer)
	 */
	public Order getOrder(Integer orderId) throws DAOException {
		log.debug("getting order #" + orderId);
		
		Session session = HibernateUtil.currentSession();
		
		Order order = new Order();
		order = (Order)session.get(Order.class, orderId);
		
		return order;
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrderTypes()
	 */
	public List<Order> getOrders(boolean showVoided) throws DAOException {
		log.debug("getting all orders, showVoided is " + showVoided);

		String voided = showVoided ? "" : "where voided = 0 ";
		
		Session session = HibernateUtil.currentSession();
				
		List<Order> orders = session.createQuery("from Orders " + voided).list();
		
		return orders;
	}

	public List<Order> getOrders() throws DAOException {
		return getOrders(true);
	}

	
	/**
	 * @see org.openmrs.api.db.OrderService#getOrdersByPatient()
	 */
	public List<Order> getOrdersByPatient(Patient patient, boolean showVoided) throws DAOException {
		log.debug("getting all orders by patient " + patient.getPatientId());

		String voided = showVoided ? "" : "where voided = 0 ";
		
		Session session = HibernateUtil.currentSession();
		
		Criteria c = session.createCriteria(Order.class);
		Criteria c1 = c.createCriteria("encounter", "enc");
		Criteria c2 = c1.add(Expression.eq("enc.patient", patient));

		List<Order> orders = c2.list();
				
		return orders;
	}

	public List<Order> getOrdersByPatient(Patient patient) throws DAOException {
		return getOrdersByPatient(patient, true);
	}

	
	/**
	 * @see org.openmrs.api.db.OrderService#updateOrder(org.openmrs.Order)
	 */
	public void updateOrder(Order order) throws DAOException {
		if (order.getOrderId() == null)
			createOrder(order);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(order);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.db.OrderService#discontinueOrder(org.openmrs.Order, java.lang.String)
	 */
	public void discontinueOrder(Order order, String reason) throws DAOException {
		discontinueOrder(order, reason, new Date());
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrder(org.openmrs.Order)
	 */
	public void discontinueOrder(Order order, String discontinueReason, Date discontinueDate) throws DAOException {
		log.debug("discontinuing order #" + order.getOrderId());

		order.setDiscontinued(new Boolean(true));
		order.setDiscontinuedReason(discontinueReason);
		order.setDiscontinuedDate(discontinueDate);
		order.setDiscontinuedBy(context.getAuthenticatedUser());
		updateOrder(order);
	}


	/**
	 * @see org.openmrs.api.db.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws DAOException {
		log.debug("getting orderType #" + orderTypeId);

		Session session = HibernateUtil.currentSession();
		
		OrderType orderType = (OrderType)session.get(OrderType.class, orderTypeId);
		
		return orderType;
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrderTypes()
	 */
	public List<OrderType> getOrderTypes() throws DAOException {
		log.debug("getting all order types");

		Session session = HibernateUtil.currentSession();
		
		List<OrderType> orderTypes = session.createCriteria(OrderType.class).list();
		
		return orderTypes;
	}

	/**
	 * @see org.openmrs.api.db.OrderService#undiscontinueOrder(org.openmrs.Order)
	 */
	public void undiscontinueOrder(Order order) throws DAOException {
		log.debug("undiscontinuing order #" + order.getOrderId());

		order.setDiscontinued(false);
		order.setDiscontinuedBy(null);
		order.setDiscontinuedDate(null);
		order.setDiscontinuedReason("");
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.db.OrderService#unvoidOrder(org.openmrs.Order)
	 */
	public void unvoidOrder(Order order) throws DAOException {
		log.debug("unvoiding order #" + order.getOrderId());
		
		order.setVoided(false);
		order.setVoidedBy(null);
		order.setDateVoided(null);
		order.setVoidReason("");
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrderTypes()
	 */
	public List<DrugOrder> getDrugOrders(boolean showVoided) throws DAOException {
		log.debug("getting all drug orders");

		Session session = HibernateUtil.currentSession();
		
		List<DrugOrder> drugOrders = session.createCriteria(DrugOrder.class).list();
		
		return drugOrders;
	}

	public List<DrugOrder> getDrugOrders() throws DAOException {
		return getDrugOrders(true);
	}

	
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, boolean showVoided) throws DAOException {
		List<DrugOrder> orders = new Vector<DrugOrder>();
		
		if (patient != null) {
			log.debug("getting all orders by patient " + patient.getPatientId());
	
			Session session = HibernateUtil.currentSession();
			
			Criteria c = session.createCriteria(DrugOrder.class);
			Criteria c1 = c.createCriteria("encounter", "enc");
			Criteria c2 = c1.add(Expression.eq("enc.patient", patient));
			orders = c2.list();
		}
		
		return orders;
	}

	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws DAOException {
		return getDrugOrdersByPatient(patient, true);
	}

	public Map<ConceptSet, List<DrugOrder>> getConceptSetsByDrugOrders(List<DrugOrder> drugOrders) throws DAOException {
		/*
		if (drugOrders != null) {
			log.debug("Attempting to sort " + drugOrders.size() + " into ConceptSets");
	
			Session session = HibernateUtil.currentSession();
			
			Criteria c = session.createCriteria(DrugOrder.class);
			Criteria c1 = c.createCriteria("encounter", "enc");
			Criteria c2 = c1.add(Expression.eq("enc.patient", patient));
			orders = c2.list();
		} else {
			log.error("List of drugOrders is null in HibernateDAO.getConceptSetsByDrugOrders() - cannot return Map");
		}
		*/
		return null;
	}

}
