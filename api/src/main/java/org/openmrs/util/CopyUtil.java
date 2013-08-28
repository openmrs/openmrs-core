package org.openmrs.util;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.SerializationUtils;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;

import com.google.common.collect.Maps;

public class CopyUtil {
	
	/**
	 * Obs deep copy.
	 * @param source
	 * @return copied obs
	 */
	public static Obs copy(Obs source) {
		return Obs.newInstance(source);
	}
	
	/**
	 * Order deep copy.
	 * @param source
	 * @return copied order
	 */
	public static Order copy(Order source) {
		return (Order) SerializationUtils.clone(source);
	}
	
	/**
	 * Encounter provider deep copy.
	 * @param source
	 * @return copied encounter provider
	 */
	public static EncounterProvider copy(EncounterProvider source) {
		
		EncounterProvider target = new EncounterProvider();
		copyBaseOpenmrsData(source, target);
		
		target.setEncounter(source.getEncounter());
		target.setEncounterRole(source.getEncounterRole());
		target.setProvider(source.getProvider());
		
		return target;
	}
	
	/**
	 * Encounter deep copy.
	 * Copied encounter will not have visit field copied.
	 * @param encounter
	 * @return copied encounter
	 */
	public static Encounter copy(Encounter encounter) {
		
		Map<Order, Order> oldNewOrderMap = Maps.newHashMap();
		
		Encounter target = new Encounter();
		
		copyBaseOpenmrsData(encounter, target);
		
		// Encounter specific data
		target.setEncounterId(encounter.getEncounterId());
		target.setEncounterDatetime(encounter.getEncounterDatetime());
		target.setEncounterType(encounter.getEncounterType());
		target.setForm(encounter.getForm());
		target.setLocation(encounter.getLocation());
		target.setPatient(encounter.getPatient());
		
		//encounter providers deep copy
		for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
			
			EncounterProvider encounterProviderCopy = copy(encounterProvider);
			encounterProviderCopy.setEncounter(target);
			target.getEncounterProviders().add(encounterProviderCopy);
		}
		
		//orders
		for (Order order : encounter.getOrders()) {
			
			Order orderCopy = copy(order);
			orderCopy.setEncounter(target);
			target.addOrder(orderCopy);
			
			oldNewOrderMap.put(order, orderCopy);
		}
		
		//obs
		for (Obs obs : encounter.getAllObs()) {
			
			Obs obsCopy = copy(obs);
			obsCopy.setEncounter(target);
			//refresh order reference
			Order oldOrder = obsCopy.getOrder();
			Order newOrder = oldNewOrderMap.get(oldOrder);
			obsCopy.setOrder(newOrder);
			
			target.addObs(obsCopy);
		}
		
		return target;
	}
	
	private static void copyBaseOpenmrsData(BaseOpenmrsData source, BaseOpenmrsData target) {
		
		target.setChangedBy(source.getChangedBy());
		target.setCreator(source.getCreator());
		target.setDateChanged(source.getDateChanged());
		target.setDateCreated(source.getDateCreated());
		target.setDateVoided(source.getDateVoided());
		target.setId(source.getId());
		target.setUuid(source.getUuid());
		target.setVoided(source.getVoided());
		target.setVoidedBy(source.getVoidedBy());
		target.setVoidReason(source.getVoidReason());
	}
}
