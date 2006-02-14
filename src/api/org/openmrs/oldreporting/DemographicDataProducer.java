package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.PatientName;
import org.openmrs.reporting.AbstractReportObject;

public class DemographicDataProducer extends AbstractReportObject implements DataProducer<Patient> {

	public <P extends Patient> DataSet<P> produceData(DataSet<P> dataSet) {
		for (P p : dataSet.getRowKeys()) {
			dataSet.setValue(p, "patient", p);
			dataSet.setValue(p, "patient_id", p.getPatientId());
			{
				Set<PatientName> names = p.getNames();
				PatientName pn = names.iterator().next();
				String name = pn.getGivenName() + " " + pn.getMiddleName() + " " + pn.getFamilyName() + " " + pn.getFamilyName2();
				dataSet.setValue(p, "name", name);
			}
			
			{
				Date birth = p.getBirthdate();
				if (birth != null) {
					dataSet.setValue(p, "birthdate", birth);
				} else {
					dataSet.setValue(p, "birthdate", null);
				}
			}
		}
		return null;
	}

	public List<String> columnsProduced() {
		List<String> ret = new ArrayList<String>();
		ret.add("patient_id");
		ret.add("name");
		ret.add("birthdate");
		return ret;
	}

	public List<String> columnsToDisplay() {
		List<String> ret = new ArrayList<String>();
		ret.add("patient_id");
		ret.add("name");
		return ret;	}

}
