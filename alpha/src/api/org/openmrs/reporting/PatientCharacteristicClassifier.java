package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.context.Context;

public class PatientCharacteristicClassifier extends AbstractReportObject
		implements PatientClassifier {
	
	private boolean partitionByGender = false;
	private boolean partitionByAge = false; 
	
	public PatientCharacteristicClassifier(boolean partitionByGender, boolean partitionByAge) {
		this.partitionByGender = partitionByGender;
		this.partitionByAge = partitionByAge;
	}

	public PatientCharacteristicClassifier() { }
	
	public boolean isPartitionByAge() {
		return partitionByAge;
	}

	public void setPartitionByAge(boolean partitionByAge) {
		this.partitionByAge = partitionByAge;
	}

	public boolean isPartitionByGender() {
		return partitionByGender;
	}

	public void setPartitionByGender(boolean partitionByGender) {
		this.partitionByGender = partitionByGender;
	}

	public Map<String, PatientSet> partition(PatientSet input) {
		Map<String, PatientSet> ret = new HashMap<String, PatientSet>();
		
		Map<Integer, Map<String, Object>> characteristics = Context.getPatientSetService().getCharacteristics(input);
		for (Map.Entry<Integer, Map<String, Object>> e : characteristics.entrySet()) {
			Map<String, Object> holder = e.getValue();
			String key = "";
			if (partitionByGender) {
				key += holder.get("gender") + " ";
			}
			if (partitionByAge) {
				Number age = (Number) holder.get("age_years");
				if (age != null) {
					if (age.doubleValue() <= 15) {
						key += "child ";
					} else {
						key += "adult ";
					}
				} else {
					key += "age_unknown ";
				}
			}
			key = key.trim();
			PatientSet ps = ret.get(key);
			if (ps == null) {
				ps = new PatientSet();
				ret.put(key, ps);
			}
			ps.add(e.getKey());
		}
		return ret;
	}

	public String getDescription() {
		if (partitionByGender) {
			if (partitionByAge) {
				return "Partition by gender and adult/child";
			} else {
				return "Partition by gender";
			}
		} else {
			if (partitionByAge) {
				return "Partition adult/child";
			} else {
				return "Partition by nothing";
			}
		}
	}

}
