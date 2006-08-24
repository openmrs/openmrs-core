package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWROrderService {

	protected final Log log = LogFactory.getLog(getClass());
		
	/**
	 */
	public void createDrugOrder(Integer patientId, Integer drugId, Double dose, String units, String frequency, String startDate) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			DrugOrder drugOrder = new DrugOrder();
			Patient patient = context.getPatientService().getPatient(patientId);
			Drug drug = context.getConceptService().getDrug(drugId);
			drugOrder.setDrug(drug);
			Concept concept = drug.getConcept();
			drugOrder.setConcept(concept);
			drugOrder.setOrderType(new OrderType(new Integer(OpenmrsConstants.ORDERTYPE_DRUG)));

			drugOrder.setDose(dose);
			drugOrder.setUnits(units);
			drugOrder.setFrequency(frequency);
			
			Date dStartDate = null;
			if ( startDate != null ) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				try {
					dStartDate = sdf.parse(startDate);
				} catch (ParseException e) {
					dStartDate = null;
				}
			}
			drugOrder.setStartDate(dStartDate);
			
			drugOrder.setDateCreated(new Date());
			drugOrder.setVoided(new Boolean(false));
			context.getOrderService().updateOrder(drugOrder, patient);
		}
	}
	
	public void voidOrder(Integer orderId, String voidReason) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Order o = context.getOrderService().getOrder(orderId);
			context.getOrderService().voidOrder(o, voidReason);
		}
	}
	
	public void discontinueOrder(Integer orderId, String discontinueReason, String discontinueDate) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Date dDiscDate = null;
			if ( discontinueDate != null ) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				try {
					dDiscDate = sdf.parse(discontinueDate);
				} catch (ParseException e) {
					dDiscDate = null;
				}
			}
			
			Order o = context.getOrderService().getOrder(orderId);
			context.getOrderService().discontinueOrder(o, discontinueReason, dDiscDate);
		}
	}

	public Vector<DrugOrderListItem> getDrugOrdersByPatientId(Integer patientId, int whatToShow) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Patient p = context.getPatientService().getPatient(patientId);
			Vector<DrugOrderListItem> ret = new Vector<DrugOrderListItem>();
			List<DrugOrder> drugOrders = context.getOrderService().getDrugOrdersByPatient(p, whatToShow);
			if ( drugOrders != null ) {
				if ( drugOrders.size() > 0 ) {
					for (DrugOrder drugOrder : drugOrders) {
						ret.add(new DrugOrderListItem(drugOrder));
					}
				}
			}
			return ret;
		} else {
			return null;
		}
	}

	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientId(Integer patientId) {
		return getDrugOrdersByPatientId(patientId, OrderService.SHOW_COMPLETE);
	}
	
	public String getUnitsByDrugId(Integer drugId) {
		String ret = "";
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Drug drug = context.getConceptService().getDrug(drugId);
			if ( drug != null ) {
				String drugUnits = drug.getUnits();
				if ( drugUnits != null ) ret = drugUnits;
			}
		}
		
		return ret; 
	}
	
	public Vector<DrugSetItem> getDrugSet(Integer patientId, Integer drugSetId, int whatToShow) {
		log.debug("In getDrugSet() method");
		Vector<DrugSetItem> dsiList = null;
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			ConceptService cs = context.getConceptService();
			Concept c = cs.getConcept(drugSetId);
			if ( c != null && patientId != null && drugSetId != null ) {
				Patient p = context.getPatientService().getPatient(patientId);
				if ( p != null ) {
					OrderService os = context.getOrderService();
					List<Concept> conceptList = new ArrayList<Concept>();
					conceptList.add(c);
					Map<Concept, List<DrugOrder>> orders = os.getDrugSetsByConcepts(os.getDrugOrdersByPatient(p, whatToShow), conceptList);
					DrugSetItem dsi = new DrugSetItem();
					dsi.setDrugSetId(c.getConceptId());
					dsi.setName(c.getName(context.getLocale()).getName());
					if ( orders != null ) {
						if ( orders.size() > 0 ) {
							List<DrugOrder> currList = (List<DrugOrder>)orders.get(orders.keySet().iterator().next());
							if ( currList != null ) {
								dsi.setDrugCount(currList.size());
							} else { 
								dsi.setDrugCount(0);
							}
						} else dsi.setDrugCount(0);
					} else dsi.setDrugCount(0);
					dsiList = new Vector<DrugSetItem>();
					dsiList.add(dsi);
				}
			}
		}
		
		return dsiList;
	}

	public Vector<DrugSetItem> getCurrentDrugSet(Integer patientId, Integer drugSetId) {
		return getDrugSet(patientId, drugSetId, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugSetItem> getCompletedDrugSet(Integer patientId, Integer drugSetId) {
		return getDrugSet(patientId, drugSetId, OrderService.SHOW_COMPLETE);
	}

	
	public Vector<DrugOrderListItem> getDrugOrdersByPatientIdDrugSetId(Integer patientId, Integer drugSetId, int whatToShow) {
		log.debug("Entering getCurrentDrugOrdersByPatientIdDrugSetId method");
		
		Vector<DrugOrderListItem> ret = null;
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			ConceptService cs = context.getConceptService();
			Concept c = cs.getConcept(drugSetId);
			if ( c != null && patientId != null && drugSetId != null ) {
				Patient p = context.getPatientService().getPatient(patientId);
				if ( p != null ) {
					OrderService os = context.getOrderService();
					List<Concept> conceptList = new ArrayList<Concept>();
					conceptList.add(c);
					log.debug("About to get order with pid [" + patientId + "] is " + p + " and cid [" + drugSetId + "] is " + c);
					Map<Concept, List<DrugOrder>> orders = os.getDrugSetsByConcepts(os.getDrugOrdersByPatient(p, whatToShow), conceptList);
					if ( orders != null ) {
						if ( orders.size() > 0 ) {
							List<DrugOrder> currList = (List<DrugOrder>)(orders.get(orders.keySet().iterator().next()));
							if ( currList != null ) {
								for ( DrugOrder drugOrder : currList ) {
									if ( ret == null ) ret = new Vector<DrugOrderListItem>();
									DrugOrderListItem drugOrderItem = new DrugOrderListItem(drugOrder);
									drugOrderItem.setDrugSetId(drugSetId);
									ret.add(drugOrderItem);
								}
							} else log.debug("currList is null");
						} else log.debug("keySet is null");
					} else {
						log.debug("orders is null");
					}
				}
			}
		}

		return ret;
	}

	public Vector<DrugOrderListItem> getCurrentDrugOrdersByPatientIdDrugSetId(Integer patientId, Integer drugSetId) {
		return getDrugOrdersByPatientIdDrugSetId(patientId, drugSetId, OrderService.SHOW_CURRENT);
	}

	public Vector<DrugOrderListItem> getCompletedDrugOrdersByPatientIdDrugSetId(Integer patientId, Integer drugSetId) {
		return getDrugOrdersByPatientIdDrugSetId(patientId, drugSetId, OrderService.SHOW_COMPLETE);
	}

	
	public void voidDrugSet(Integer patientId, Integer drugSetId, String voidReason) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			ConceptService cs = context.getConceptService();
			Concept c = cs.getConcept(drugSetId);
			if ( c != null && patientId != null && drugSetId != null ) {
				Patient p = context.getPatientService().getPatient(patientId);
				if ( p != null ) {
					OrderService os = context.getOrderService();
					List<Concept> conceptList = new ArrayList<Concept>();
					conceptList.add(c);
					log.debug("About to get order with pid [" + patientId + "] is " + p + " and cid [" + drugSetId + "] is " + c);
					Map<Concept, List<DrugOrder>> orders = os.getDrugSetsByConcepts(os.getDrugOrdersByPatient(p, OrderService.SHOW_CURRENT), conceptList);
					if ( orders != null ) {
						if ( orders.size() > 0 ) {
							List<DrugOrder> currList = (List<DrugOrder>)orders.get(orders.keySet().iterator().next());
							if ( currList != null ) {
								for ( DrugOrder drugOrder : currList ) {
									this.voidOrder(drugOrder.getOrderId(), voidReason);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void discontinueDrugSet(Integer patientId, Integer drugSetId, String discontinueReason, String discontinueDate) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			ConceptService cs = context.getConceptService();
			Concept c = cs.getConcept(drugSetId);
			if ( c != null && patientId != null && drugSetId != null ) {
				Patient p = context.getPatientService().getPatient(patientId);
				if ( p != null ) {
					OrderService os = context.getOrderService();
					List<Concept> conceptList = new ArrayList<Concept>();
					conceptList.add(c);
					log.debug("About to get order with pid [" + patientId + "] is " + p + " and cid [" + drugSetId + "] is " + c);
					Map<Concept, List<DrugOrder>> orders = os.getDrugSetsByConcepts(os.getDrugOrdersByPatient(p, OrderService.SHOW_CURRENT), conceptList);
					if ( orders != null ) {
						if ( orders.size() > 0 ) {
							List<DrugOrder> currList = (List<DrugOrder>)orders.get(orders.keySet().iterator().next());
							if ( currList != null ) {
								for ( DrugOrder drugOrder : currList ) {
									this.discontinueOrder(drugOrder.getOrderId(), discontinueReason, discontinueDate);
								}
							}
						}
					}
				}
			}
		}
	}

}



