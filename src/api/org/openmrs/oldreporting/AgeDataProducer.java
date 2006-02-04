package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.reporting.AbstractReportObject;

/**
 * Gets age-related data items for a PatientSet:
 *  age_in_years
 *  age_in_months
 * @author djazayeri
 */
public class AgeDataProducer extends AbstractReportObject implements DataProducer<Patient> {

	private final static long MS_PER_YEAR = Math.round(365.2422 * 24 * 60 * 60 * 1000);
	private final static long MS_PER_MONTH = Math.round((365.2422 / 12) * 24 * 60 * 60 * 1000);
	
	public AgeDataProducer() { }
	
	public <P extends Patient> DataSet<P> produceData(DataSet<P> dataSet) {
		long now = System.currentTimeMillis();
		for (P p : dataSet.getRowKeys()) {
			Date birth = p.getBirthdate();
			if (birth != null) {
				long ageInMillis = now - birth.getTime();
				dataSet.setValue(p, "birthdate", birth);
				dataSet.setValue(p, "age_in_years", new Integer((int) (ageInMillis / MS_PER_YEAR)));
				dataSet.setValue(p, "age_in_months", new Integer((int) (ageInMillis / MS_PER_MONTH)));
			} else {
				dataSet.setValue(p, "birthdate", null);
				dataSet.setValue(p, "age_in_years", null);
				dataSet.setValue(p, "age_in_months", null);
			}
		}
		return dataSet;
	}
	
	public List<String> columnsProduced() {
		List<String> ret = new ArrayList<String>();
		ret.add("birthdate");
		ret.add("age_in_years");
		ret.add("age_in_months");
		return ret;
	}
	
	public List<String> columnsToDisplay() {
		return columnsProduced();
	}

}
