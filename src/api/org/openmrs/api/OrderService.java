package org.openmrs.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
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

	protected final Log log = LogFactory.getLog(getClass());

	public static final int SHOW_CURRENT = 1;
	public static final int SHOW_ALL = 2;
	public static final int SHOW_COMPLETE = 3;
	public static final int SHOW_NOTVOIDED = 4;

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
	 * Create a new Order
	 * @param Order to create
	 * @throws APIException
	 */
	public void createOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().createOrder(order);
	}

	/**
	 * Update Order
	 * @param Order to update
	 * @throws APIException
	 */
	public void updateOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		// If this order has no encounter, check if the patient exi
		
		getOrderDAO().updateOrder(order);
	}

	/**
	 * Update Order
	 * @param Order to update
	 * @param Patient for whom this order is for
	 * @throws APIException
	 */
	public void updateOrder(Order order, Patient patient) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		// If this order has no encounter, attempt to create a blank one for the patient (if it exists)
		if ( patient != null && order != null ) { 
			if ( order.getEncounter() == null ) {
				Encounter e = new Encounter();
				Location unknownLoc = new Location(new Integer(Location.LOCATION_UNKNOWN));
				e.setLocation(unknownLoc);
				e.setPatient(patient);
				// TODO: this should one day not be required, and thus not require this hack
				if ( order.getOrderer() == null ) {
					User unknownUser = context.getUserService().getUserByUsername("Unknown");
					e.setProvider(unknownUser);
				} else {
					e.setProvider(order.getOrderer());
				}
				e.setEncounterDatetime(order.getStartDate());
				e.setCreator(context.getAuthenticatedUser());
				e.setDateCreated(order.getDateCreated());
				e.setVoided(new Boolean(false));
				context.getEncounterService().updateEncounter(e);
				order.setEncounter(e);
			}
		}
		
		updateOrder(order);
	}

	/**
	 * Delete Order
	 * @param Order to delete
	 * @throws APIException
	 */
	public void deleteOrder(Order order) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().deleteOrder(order);
	}

	/**
	 * Void Order
	 * @param voidReason 
	 * @param Order to void
	 * @throws APIException
	 */
	public void voidOrder(Order order, String voidReason) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().voidOrder(order, voidReason);
	}
	
	/**
	 * Void Order
	 * @param voidReason 
	 * @param Order to void
	 * @throws APIException
	 */
	public void discontinueOrder(Order order, String discontinueReason, Date discontinueDate) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDERS);

		getOrderDAO().discontinueOrder(order, discontinueReason, discontinueDate);
	}

	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @throws APIException
	 */
	public void createOrderType(OrderType orderType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getOrderDAO().createOrderType(orderType);
	}

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getOrderDAO().updateOrderType(orderType);
	}

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ORDER_TYPES);

		getOrderDAO().deleteOrderType(orderType);
	}

		/**
	 * Creates a collection of orders and an encounter to hold them. orders[i].encounter will be set to the new encounter.
	 * If there's an EncounterType with name "Regimen Change", then the newly-created encounter will have that type
	 * @throws APIException if there is no User with username Unknown or no Location with name Unknown.
	 */
	public void createOrdersAndEncounter(Patient p, Collection<Order> orders) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_ORDERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_ORDERS);
		User unknownUser = context.getUserService().getUserByUsername("Unknown");
		Location unknownLocation = context.getEncounterService().getLocationByName("Unknown");
		// TODO: fix this hack
		if (unknownUser == null) {
			unknownUser = context.getAuthenticatedUser();
		}
		if (unknownUser == null || unknownLocation == null) {
			throw new APIException("Couldn't find a Location and a User named 'Unknown'.");
		}
		Encounter e = new Encounter();
		e.setPatient(p);
		e.setProvider(unknownUser);
		e.setLocation(unknownLocation);
		e.setEncounterDatetime(new Date());
		// TODO: Remove hardcoded encounter type
		e.setEncounterType(context.getEncounterService().getEncounterType("Regimen Change"));
		for (Order order : orders) {
			if (order.getCreator() == null) {
				order.setCreator(context.getAuthenticatedUser());
			}
			if (order.getDateCreated() == null) {
				order.setDateCreated(new Date());
			}
			e.addOrder(order);
			order.setEncounter(e);
		}
		context.getEncounterService().createEncounter(e);
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
	 * Get all orders
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<Order> getOrders() throws APIException {
		return getOrderDAO().getOrders();
	}

	/**
	 * Get all drug orders
	 * 
	 * @return drug orders list
	 * @throws APIException
	 */
	public List<DrugOrder> getDrugOrders() throws APIException {
		return getOrderDAO().getDrugOrders();
	}

	/**
	 * Get all orders by User
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<Order> getOrdersByUser(User user) throws APIException {
		//return getOrderDAO().getOrdersByUser(user);
		return null;
	}

	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<Order> getOrdersByPatient(Patient patient) throws APIException {
		return getOrderDAO().getOrdersByPatient(patient);
	}

	public List<DrugOrder> getDrugOrdersByPatient(Patient patient, int whatToShow) {
		if (context != null) {
			List<DrugOrder> ret = new ArrayList<DrugOrder>();
			List<DrugOrder> drugOrders = getDrugOrdersByPatient(patient);
			for (DrugOrder drugOrder : drugOrders) {
				boolean shouldAdd = false;

				if ( whatToShow == OrderService.SHOW_COMPLETE ) {
					if ( !drugOrder.getVoided() && drugOrder.getAutoExpireDate() != null ) {
						if ( drugOrder.getAutoExpireDate().before(new Date())) shouldAdd = true;
					} else if ( !drugOrder.getVoided() && drugOrder.getDiscontinued() ) shouldAdd = true;
				}
					
				if ( whatToShow == OrderService.SHOW_CURRENT ) {
					if ( !drugOrder.getVoided() && !drugOrder.getDiscontinued() && drugOrder.getAutoExpireDate() != null ) {
						if ( !drugOrder.getAutoExpireDate().before(new Date())) shouldAdd = true;
					} else if ( !drugOrder.getVoided() && !drugOrder.getDiscontinued() ) shouldAdd = true;
				}
				if ( whatToShow == OrderService.SHOW_NOTVOIDED && !drugOrder.getVoided() ) shouldAdd = true;
				if ( whatToShow == OrderService.SHOW_ALL ) shouldAdd = true;

				if ( shouldAdd ) ret.add(drugOrder);
			}
			return ret;
		} else {
			return null;
		}
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

	/**
	 * Get all orders by Patient
	 * 
	 * @return orders list
	 * @throws APIException
	 */
	public List<DrugOrder> getDrugOrdersByPatient(Patient patient) throws APIException {
		return getOrderDAO().getDrugOrdersByPatient(patient);
	}

	public Map<ConceptSet,List<DrugOrder>> getDrugSetsByConceptSets(List<DrugOrder> drugOrders, List<ConceptSet> conceptSets) throws APIException {
		log.debug("In getDrugSetsByConceptSets method");
		
		HashMap<ConceptSet,List<DrugOrder>> hmRet = null;
		
		if ( drugOrders != null && conceptSets != null ) {
			log.debug("drugOrders is size " + drugOrders.size() + " and conceptSet is size " + conceptSets.size());
			hmRet = new HashMap<ConceptSet,List<DrugOrder>>();
			
			for ( ConceptSet cSet : conceptSets ) {
				if ( cSet != null ) {
					//log.debug("cSet is " + cSet.getConcept().getName(context.getLocale()));
					Concept cSetConcept = cSet.getConcept();
					for ( DrugOrder drugOrder : drugOrders ) {
						if ( drugOrder != null ) {
							//log.debug("drugOrder is " + drugOrder.getConcept().getName(context.getLocale()));
							Concept drugConcept = drugOrder.getConcept();
							if ( drugConcept != null ) {
								if ( drugConcept.equals(cSetConcept) ) {
									log.debug("cSet [" + cSet.getConcept().getName(context.getLocale())
											+ "] IS EQUAL TO drugOrder [" + drugOrder.getConcept().getName(context.getLocale()));
									if ( hmRet == null ) {
										hmRet = new HashMap<ConceptSet,List<DrugOrder>>();
									}
									List<DrugOrder> drugOrdersAlready = hmRet.get(cSet);
									if ( drugOrdersAlready == null ) {
										drugOrdersAlready = new ArrayList<DrugOrder>();
									}
									drugOrdersAlready.add(drugOrder);
									hmRet.put(cSet, drugOrdersAlready);
									log.debug("drugOrder [" + drugOrder.getConcept().getName(context.getLocale()) + "] added to drugOrders, now size " + drugOrdersAlready.size() + " for cSet [" + cSet.getConcept().getName(context.getLocale()) + "], Map is now size " + hmRet.size());
								} else {
									log.debug("cSet [" + cSet.getConcept().getName(context.getLocale())
											+ "] NOT SAME AS drugOrder [" + drugOrder.getConcept().getName(context.getLocale()));
								}
							}
						}
					}
				}
			}
		} else {
			if ( drugOrders == null ) {
				throw new APIException("List of drugOrders is null in OrderService.getConceptSetsByDrugOrders()");
			} else {
				throw new APIException("List of conceptSets is null in OrderService.getConceptSetsByDrugOrders()");
			}
		}

		return hmRet;
	}

	public Map<Concept,List<DrugOrder>> getDrugSetsByConcepts(List<DrugOrder> drugOrders, List<Concept> drugSets) throws APIException {
		log.debug("In getDrugSetsByConcepts method");

		Set<ConceptSet> conceptSets = null;
		Map<Concept, List<DrugOrder>> hmRet = null;
		
		if ( drugSets != null ) {
			log.debug("drugSets is size " + drugSets.size());
			for ( Concept c : drugSets ) {
				log.debug("Concept c is " + c.getName(context.getLocale()));
				List<ConceptSet> cSet = new ArrayList<ConceptSet>();
				Collection<ConceptSet> relatedConcepts = c.getConceptSets();
				log.debug("related concepts is size" + relatedConcepts.size());
				cSet.addAll(relatedConcepts);
				if ( conceptSets == null ) conceptSets = new HashSet<ConceptSet>();
				conceptSets.addAll(cSet);

				// now we have as a list, let's iterate
				List<ConceptSet> cSetList = null;
				if ( conceptSets != null ) {
					cSetList = new ArrayList<ConceptSet>();
					cSetList.addAll(conceptSets);
					Map<ConceptSet,List<DrugOrder>> ordersBySet = getDrugSetsByConceptSets(drugOrders, cSetList);
					if ( ordersBySet != null ) {
						for (Iterator i = ordersBySet.keySet().iterator(); i.hasNext(); ) {
							if ( hmRet == null ) hmRet = new HashMap<Concept, List<DrugOrder>>(); 
							List<DrugOrder> ordersAlready = hmRet.get(c);
							if ( ordersAlready == null ) ordersAlready = new ArrayList<DrugOrder>();

							// let's make sure there is only one copy of each DrugOrder in this list
							Set<DrugOrder> currSet = new HashSet<DrugOrder>();
							currSet.addAll((List<DrugOrder>)ordersBySet.get(i.next()));
							currSet.addAll(ordersAlready);
							ordersAlready = null;
							ordersAlready = new ArrayList<DrugOrder>();
							ordersAlready.addAll(currSet);
							hmRet.put(c, ordersAlready);
						}
					}
				}
			}
		} else log.debug("drugSets is null");
		
		return hmRet;
	}
}
