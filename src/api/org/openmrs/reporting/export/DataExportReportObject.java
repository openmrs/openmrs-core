package org.openmrs.reporting.export;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientSet;

public class DataExportReportObject extends AbstractReportObject implements Serializable {

	public final static long serialVersionUID = 1231231343212L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	List<Integer> patientIds = new Vector<Integer>();
	Location location;
	
	List<ExportColumn> columns = new Vector<ExportColumn>();

	public final static String TYPE_NAME = "Data Export";
	public final static String SUB_TYPE_NAME = "";
	
	public final static String MODIFIER_ANY = "any";
	public final static String MODIFIER_FIRST = "first";
	public final static String MODIFIER_LAST = "mostRecent";
	public final static String MODIFIER_LAST_NUM = "mostRecentNum";
	
	/**
	 * Default Constructor
	 */
	public DataExportReportObject() {
		super.setType(DataExportReportObject.TYPE_NAME);
		super.setSubType(DataExportReportObject.SUB_TYPE_NAME);		
	}

	/**
	 * Append a simple column
	 * @param columnName
	 * @param columnValue
	 */
	public void addSimpleColumn(String columnName, String columnValue) {
		columns.add(new SimpleColumn(columnName, columnValue));
	}
	
	/**
	 * Append a concept based column
	 * @param columnName
	 * @param modifier
	 * @param columnValue
	 */
	public void addConceptColumn(String columnName, String modifier, String columnValue) {
		columns.add(new ConceptColumn(columnName, modifier, columnValue));
	}
	
	/**
	 * Append a calculated column
	 * @param columnName
	 * @param columnValue
	 */
	public void addCalculatedColumn(String columnName, String columnValue) {
		columns.add(new CalculatedColumn(columnName, columnValue));
	}
	
	/**
	 * Add a patient to the list to be run on
	 * @param p
	 */
	public void addPatientId(Integer p) {
		patientIds.add(p);
	}
	
	/**
	 * Export data using this object's columns and these patients
	 * @param set
	 * @return template string to be evaluated
	 */
	public String export(PatientSet set) {
		StringBuilder sb = new StringBuilder();
		
		// print out the columns
		if (columns.size() >= 1) {
			sb.append(columns.get(0).getTemplateColumnName());
			for (int i=1; i<columns.size(); i++) {
				sb.append("$!{fn.getSeparator()}");
				sb.append(columns.get(i).getTemplateColumnName());
			}
		}
		
		sb.append("\n");
		
		// print out the data
		
		for (Integer patientId : set.getPatientIds()) {
			sb.append("$!{fn.setPatient(" + patientId + ")}");
			if (columns.size() >= 1) {
				sb.append(columns.get(0).toTemplateString());
				for (int i=1; i<columns.size(); i++) {
					sb.append("$!{fn.getSeparator()}");
					sb.append(columns.get(i).toTemplateString());
				}
			}
			else
				log.warn("Report has column size less than 1");
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Export data using this object's columns and this objects characteristics
	 * @param context
	 * @return template string to be evaluated
	 */
	public String export(Context context) {
		PatientSetService pss = context.getPatientSetService();
		PatientSet patientSet = new PatientSet();
		
		for (Integer p : patientIds)
			patientSet.add(p);
		
		if (location != null && !location.equals(""))
			patientSet = pss.getPatientsHavingLocation(getLocation());
		else if (patientIds.size() == 0) {
			// Add all patients
			patientSet = context.getPatientSetService().getAllPatients();
		}
		
		return export(patientSet);
	}
	
	/**
	 * Export data for this patient with this report's columns
	 * @param p
	 * @return template string to be evaluated
	 */
	public String export(Patient p) {
		PatientSet patientSet = new PatientSet();
		
		patientSet.add(p);
		
		return export(patientSet);
	}

	@Override
	public String toString() {
		return "Data Export #" + getReportObjectId();
	}
	
	public List<ExportColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<ExportColumn> columns) {
		this.columns = columns;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Integer> getPatientIds() {
		return patientIds;
	}

	public void setPatientIds(List<Integer> patientIds) {
		this.patientIds = patientIds;
	}

}
