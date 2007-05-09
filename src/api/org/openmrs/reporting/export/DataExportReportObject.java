package org.openmrs.reporting.export;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;

public class DataExportReportObject extends AbstractReportObject implements Serializable {

	public final static long serialVersionUID = 1231231343212L;
	
	private static Log log = LogFactory.getLog(DataExportReportObject.class);
	
	List<Integer> patientIds = new Vector<Integer>();
	Location location;
	// cohort and cohortDefinition should really be of type Cohort and PatientFilter, but this is temporary, and I want to avoid the known bug with xml serialization of ReportObjects  
	Integer cohortId;
	Integer cohortDefinitionId;
	
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
	
	public boolean equals(Object obj) {
		if (obj instanceof DataExportReportObject) {
			DataExportReportObject c = (DataExportReportObject)obj;
			return (this.getReportObjectId().equals(c.getReportObjectId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getReportObjectId() == null) return super.hashCode();
		int hash = 5;
		hash = 31 * this.getReportObjectId() + hash;
		return hash;
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
	 * @param modifierNum
	 * @param conceptId
	 * @param extras String[]
	 */
	public void addConceptColumn(String columnName, String modifier, Integer modifierNum, String conceptId, String[] extras) {
		columns.add(new ConceptColumn(columnName, modifier, modifierNum, conceptId, extras));
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
	 * Generate a template according to this reports columns
	 * Assumes there is a patientSet object available
	 * @return template string to be evaluated
	 */
	public String generateTemplate() {
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
		
		sb.append("$!{fn.setPatientSet($patientSet)}");
		sb.append("#foreach($patientId in $patientSet.patientIds)\n");
		sb.append("$!{fn.setPatientId($patientId)}");
		if (columns.size() >= 1) {
			sb.append(columns.get(0).toTemplateString());
			for (int i=1; i<columns.size(); i++) {
				sb.append("$!{fn.getSeparator()}");
				sb.append(columns.get(i).toTemplateString());
			}
		}
		else
			log.warn("Report has column size less than 1");
		
		sb.append("\n#end\n\n");
		
		return sb.toString();
	}
	
	/**
	 * Generate the patientSet according to this report's characteristics
	 * @return patientSet to be used with report template
	 */
	public PatientSet generatePatientSet() {
		PatientSetService pss = Context.getPatientSetService();
		
		PatientSet patientSet = null;
		
		if (getPatientIds() == null || getPatientIds().size() == 0)
			patientSet = Context.getPatientSetService().getAllPatients();
		else {
			patientSet = new PatientSet();
			for (Integer p : patientIds)
				patientSet.add(p);
		}
		
		if (location != null && !location.equals(""))
			patientSet = patientSet.intersect(pss.getPatientsHavingLocation(getLocation()));
		
		if (cohortId != null) {
			// hack to hydrate this
			Cohort cohort = Context.getCohortService().getCohort(cohortId);
			if (cohort != null)
				patientSet = patientSet.intersect(cohort.toPatientSet());
		}
		
		if (cohortDefinitionId != null) {
			PatientFilter cohortDefinition = (PatientFilter) Context.getReportService().getReportObject(cohortDefinitionId);
			if (cohortDefinition != null)
				patientSet = cohortDefinition.filter(patientSet);
		}
		
		return patientSet;
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

	public Integer getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(Integer cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public Integer getCohortId() {
		return cohortId;
	}

	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}

}
