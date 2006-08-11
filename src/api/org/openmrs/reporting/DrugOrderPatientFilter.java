package org.openmrs.reporting;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;

public class DrugOrderPatientFilter extends AbstractPatientFilter implements PatientFilter {

	public enum GroupMethod { ANY, NONE }

	private Integer drugId; // replace this with drug
	private GroupMethod groupMethod;
	private Date onDate; 
	
	public DrugOrderPatientFilter() {
		super.setType("Patient Filter");
		super.setSubType("Drug Order Patient Filter");	
	}
	
	public GroupMethod getGroupMethod() {
		return groupMethod;
	}

	public void setGroupMethod(GroupMethod groupMethod) {
		this.groupMethod = groupMethod;
	}

	public java.util.Date getOnDate() {
		return onDate;
	}

	public void setOnDate(Date onDate) {
		this.onDate = onDate;
	}

	public Integer getDrugId() {
		return drugId;
	}

	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}

	public PatientSet filter(Context context, PatientSet input) {
		Set<Integer> drugIds = new HashSet<Integer>();
		if (groupMethod != null && groupMethod == GroupMethod.NONE) {
			drugIds = null;
		} else {
			if (drugId != null)
				drugIds.add(drugId);
		}
		PatientSetService service = context.getPatientSetService();
		return service.getPatientsHavingDrugOrder(input.getPatientIds(), drugIds, onDate);
	}

	public PatientSet filterInverse(Context context, PatientSet input) {
		Set<Integer> drugIds = new HashSet<Integer>();
		if (groupMethod != null && groupMethod == GroupMethod.NONE) {
			drugIds = null;
		} else {
			if (drugId != null)
				drugIds.add(drugId);
		}
		PatientSetService service = context.getPatientSetService();
		PatientSet temp = service.getPatientsHavingDrugOrder(input.getPatientIds(), drugIds, onDate);
		return input.subtract(temp);
	}

}
