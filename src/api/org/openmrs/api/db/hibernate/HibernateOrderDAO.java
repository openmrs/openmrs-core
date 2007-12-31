package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.openmrs.Concept;
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
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateOrderDAO() { }

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public void createOrderType(OrderType orderType) throws DAOException {
		orderType.setCreator(Context.getAuthenticatedUser());
		orderType.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(orderType);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateOrderType(org.openmrs.OrderType)
	 */
	public void updateOrderType(OrderType orderType) throws DAOException {
		if (orderType.getOrderTypeId() == null)
			createOrderType(orderType);
		else
			sessionFactory.getCurrentSession().saveOrUpdate(orderType);
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException {
		sessionFactory.getCurrentSession().delete(orderType);
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public void createOrder(Order order) throws DAOException {
		log.debug("In createOrder method");
		order.setCreator(Context.getAuthenticatedUser());
		order.setDateCreated(new Date());
		sessionFactory.getCurrentSession().save(order);
		log.debug("Ending createOrder method");
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrder(org.openmrs.Order)
	 */
	public void deleteOrder(Order order) throws DAOException {
		sessionFactory.getCurrentSession().delete(order);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrder(org.openmrs.Order)
	 */
	public void voidOrder(Order order, String voidReason) throws DAOException {
		order.setVoided(new Boolean(true));
		order.setVoidReason(voidReason);
		order.setVoidedBy(Context.getAuthenticatedUser());
		order.setDateVoided(new Date());
		updateOrder(order);
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrder(java.lang.Integer)
	 */
	public Order getOrder(Integer orderId) throws DAOException {
		log.debug("getting order #" + orderId);
		
		return (Order)sessionFactory.getCurrentSession().get(Order.class, orderId);
	}

	public DrugOrder getDrugOrder(Integer drugOrderId) throws DAOException {
		log.debug("getting order #" + drugOrderId);
		
		return (DrugOrder)sessionFactory.getCurrentSession().get(DrugOrder.class, drugOrderId);
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrderTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<Order> getOrders(boolean showVoided) throws DAOException {
		log.debug("getting all orders, showVoided is " + showVoided);

		String voided = showVoided ? "" : "where voided = 0 ";
				
		return sessionFactory.getCurrentSession().createQuery("from Orders " + voided).list();
	}

	public List<Order> getOrders() throws DAOException {
		return getOrders(true);
	}

	
	/**
	 * @see org.openmrs.api.db.OrderService#getOrdersByPatient()
	 */
	@SuppressWarnings("unchecked")
	public List<Order> getOrdersByPatient(Patient patient, boolean showVoided) throws DAOException {
		log.debug("getting all orders by patient " + patient.getPatientId());

		//String voided = showVoided ? "" : "where voided = 0 ";
		
		Criteria c = sessionFactory.getCurrentSession().createCriteria(Order.class);
		Criteria c1 = c.add(Expression.eq("patient", patient));

		return c1.list();
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
			log.debug("Updating existing order: " + order.getOrderId().toString());
			sessionFactory.getCurrentSession().saveOrUpdate(order);			
		}
	}
	
	/**
	 * @see org.openmrs.api.db.OrderService#discontinueOrder(org.openmrs.Order, java.lang.String)
	 */
	public void discontinueOrder(Order order, Concept reason) throws DAOException {
		discontinueOrder(order, reason, new Date());
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrder(org.openmrs.Order)
	 */
	public void discontinueOrder(Order order, Concept discontinueReason, Date discontinueDate) throws DAOException {
		log.debug("discontinuing order #" + order.getOrderId() + ", date is " + discontinueDate);

		order.setDiscontinued(new Boolean(true));
		order.setDiscontinuedReason(discontinueReason);
		order.setDiscontinuedDate(discontinueDate);
		order.setDiscontinuedBy(Context.getAuthenticatedUser());
		
		log.debug("discontinued is " + order.getDiscontinued());
		sessionFactory.getCurrentSession().update(order);			
	}


	/**
	 * @see org.openmrs.api.db.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws DAOException {
		log.debug("getting orderType #" + orderTypeId);

		return (OrderType)sessionFactory.getCurrentSession().get(OrderType.class, orderTypeId);
	}

	/**
	 * @see org.openmrs.api.db.OrderService#getOrderTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<OrderType> getOrderTypes() throws DAOException {
		log.debug("getting all order types");
		
		return sessionFactory.getCurrentSession().createCriteria(OrderType.class).list();
	}

	/**
	 * @see org.openmrs.api.db.OrderService#undiscontinueOrder(org.openmrs.Order)
	 */
	public void undiscontinueOrder(Order order) throws DAOException {
		log.debug("undiscontinuing order #" + order.getOrderId());

		order.setDiscontinued(false);
		order.setDiscontinuedBy(null);
		order.setDiscontinuedDate(null);
		order.setDiscontinuedReason(null);
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
	@SuppressWarnings("unchecked")
	public List<DrugOrder> getDrugOrders(boolean showVoided) throws DAOException {
		log.debug("getting all drug orders");

		return sessionFactory.getCurrentSession().createCriteria(DrugOrder.class).list();
	}

	public List<DrugOrder> getDrugOrders() throws DAOException {
		return getDrugOrders(false);
	}

	
	@SuppressWarnings("unchecked")
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, boolean showVoided) throws DAOException {
		List<DrugOrder> orders = new Vector<DrugOrder>();
		
		if (patient != null) {
			log.debug("getting all orders by patient " + patient.getPatientId());
			
			Criteria c = sessionFactory.getCurrentSession().createCriteria(DrugOrder.class);
			c = c.add(Expression.eq("patient", patient));
			if (!showVoided)
				c = c.add(Expression.eq("voided", false));
			orders = c.list();
		}
		
		return orders;
	}

	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws DAOException {
		return getDrugOrdersByPatient(patient, false);
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
