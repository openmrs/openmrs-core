package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;

/**
 * Gets age-related data items for a PatientSet:
 *  age_in_years
 *  age_in_months
 * @author djazayeri
 */
public class AgeDataSelector implements PatientDataSelector {

	public AgeDataSelector() { }
	
	/* (non-Javadoc)
	 * @see org.openmrs.reporting.PatientDataSelector#getDataItemNames()
	 */
	public List<String> getDataItemNames() {
		List<String> ret = new ArrayList<String>();
		ret.add("age_in_years");
		ret.add("age_in_months");
		return null;
	}

	private final static long MS_PER_YEAR = Math.round(365.2422 * 24 * 60 * 60 * 1000);
	private final static long MS_PER_MONTH = Math.round((365.2422 / 12) * 24 * 60 * 60 * 1000);
	
	/**
	 * @param ps	the PatientSet to retrieve data on
	 * @return	age-related data items about ps
	 */
	public PatientDataSet getData(PatientSet ps) {
		long now = System.currentTimeMillis();
		PatientDataSet pds = new PatientDataSet(ps);
		for (Iterator<Patient> i = ps.iterator(); i.hasNext(); ) {
			Patient p = i.next();
			Date birth = p.getBirthdate();
			if (birth != null) {
				long ageInMillis = now - birth.getTime();
				pds.putDataItem(p, "age_in_years", new Integer((int) (ageInMillis / MS_PER_YEAR)));
				pds.putDataItem(p, "age_in_months", new Integer((int) (ageInMillis / MS_PER_MONTH)));
			} else {
				pds.putDataItem(p, "age_in_years", null);
				pds.putDataItem(p, "age_in_months", null);
			}
		}
		return pds;
	}

}
