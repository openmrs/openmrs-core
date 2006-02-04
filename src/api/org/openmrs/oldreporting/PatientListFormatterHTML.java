package org.openmrs.oldreporting;

import org.openmrs.Patient;
import org.openmrs.PatientName;

public class PatientListFormatterHTML implements DataTableFormatter {

	public PatientListFormatterHTML() { }
	
	public String format(DataTable table) {
		StringBuffer ret = new StringBuffer();
		ret.append("<table border=1>");
		ret.append("<tr><th>patient_id</th><th>Name</th><th>Gender</th><th>Birthdate</th></tr>");
		for (DataRow row : table.getRows()) {
			Patient patient = (Patient) row.get("patient");
			StringBuffer sb = new StringBuffer();
			for (PatientName name : patient.getNames()) {
				sb.append(name.getGivenName() + " " + name.getFamilyName());
			}
			ret.append("<tr><td>" + patient.getPatientId() + "</td><td>" + sb + "</td><td>" + patient.getGender() + "</td><td>" + patient.getBirthdate() + "</td></tr>");
		}
		ret.append("</table>");
		return ret.toString();
	}
	
}
