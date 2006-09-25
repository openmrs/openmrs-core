package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openmrs.api.APIException;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.reporting.db.ReportDAO;
import org.openmrs.reporting.db.ReportObjectDAO;
import org.openmrs.util.OpenmrsConstants;

public class ReportService {
	private Context context;
	private DAOContext daoContext;
	private ReportObjectFactory reportObjectFactory;
	
	public ReportService(Context c, DAOContext d, ReportObjectFactory r) {
		this.context = c;
		this.daoContext = d;
		this.reportObjectFactory = r;
	}
	
	private ReportDAO dao() {
		return daoContext.getReportDAO();
	}

	private ReportObjectDAO reportObjectDAO() {
		return daoContext.getReportObjectDAO();
	}
	
	public ReportObjectFactory getReportObjectFactory() {
		return reportObjectFactory;
	}
	
	public Set<Report> getAllReports() {
		return dao().getAllReports();
	}
	
	public Report getReport(Integer reportId) throws APIException {
		return dao().getReport(reportId);
	}
	
	public void createReport(Report report) throws APIException {
		dao().createReport(report);
	}
	
	public void deleteReport(Report report) throws APIException {
		dao().deleteReport(report);
	}
	
	public void updateReport(Report report) throws APIException {
		dao().updateReport(report);
	}

	public Set<AbstractReportObject> getAllReportObjects() {
		return reportObjectDAO().getAllReportObjects();
	}
		
	public AbstractReportObject getReportObject(Integer reportObjectId) throws APIException {
		return reportObjectDAO().getReportObject(reportObjectId);
	}
	
	public Set<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws APIException {
		return reportObjectDAO().getReportObjectsByType(reportObjectType);
	}

	public PatientFilter getPatientFilterById(Integer filterId) throws APIException {
		return (PatientFilter)getReportObject(filterId);
	}
	
	public PatientFilter getPatientFilterByName(String filterName) throws APIException {
		// TODO: push this into the DAO layer
		for (PatientFilter pf : getAllPatientFilters()) {
			AbstractReportObject o = (AbstractReportObject) pf;
			if (filterName.equals(o.getName())) {
				return pf;
			}
		}
		return null;
	}
	
	public ArrayList<PatientFilter> getAllPatientFilters() throws APIException {
		ArrayList<PatientFilter> allPatientFilters = new ArrayList<PatientFilter>();
		Set<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTFILTER);
		if ( allMatchingObjects != null ) {
			for ( AbstractReportObject aro : allMatchingObjects ) {
				allPatientFilters.add((PatientFilter)aro);
			}
		}
		return allPatientFilters;
	}

	public void createReportObject(AbstractReportObject reportObject) throws APIException {
		reportObjectDAO().createReportObject(reportObject);
	}
	
	public void deleteReport(AbstractReportObject reportObject) throws APIException {
		reportObjectDAO().deleteReportObject(reportObject);
	}
	
	public void updateReportObject(AbstractReportObject reportObject) throws APIException {
		reportObjectDAO().updateReportObject(reportObject);
	}

	public Set<String> getReportObjectTypes() {
		Set<String> availableTypes = this.reportObjectFactory.getReportObjectTypes();
		return availableTypes;
	}
	
	public Set<String> getReportObjectSubTypes(String type) {
		Set<String> availableTypes = this.reportObjectFactory.getReportObjectSubTypes(type);
		return availableTypes;
	}

	public boolean isSubTypeOfType(String type, String subType) {
		return this.reportObjectFactory.isSubTypeOfType(type, subType);
	}

	public String getReportObjectClassBySubType(String subType) {
		String className = this.reportObjectFactory.getReportObjectClassBySubType(subType);
		return className;
	}

	public Set<String> getAllReportObjectClasses() {
		Set<String> allClasses = this.reportObjectFactory.getAllReportObjectClasses();
		return allClasses;
	}

	public String getReportObjectValidatorByClass(String currentClassName) {
		String validatorClass = this.reportObjectFactory.getReportObjectValidatorByClass(currentClassName);
		return validatorClass;
	}
	
	public String getDefaultReportObjectValidator() {
		String defaultValidator = this.reportObjectFactory.getDefaultValidator();
		return defaultValidator;
	}

	/*
	 * placeholders for testing -DJ
	 */
	static Map<Integer, PatientFilter> tempFilters;
	private void fillTempFilters() {
		if (tempFilters != null) {
			return;
		}
		tempFilters = new HashMap<Integer, PatientFilter>();
		{
			PatientCharacteristicFilter temp;
			temp = new PatientCharacteristicFilter("M", null, null);
			temp.setReportObjectId(1);
			tempFilters.put(new Integer(1), temp);
			temp = new PatientCharacteristicFilter("F", null, null);
			temp.setReportObjectId(2);
			tempFilters.put(new Integer(2), temp);
			//temp = new PatientCharacteristicFilter(null, new Date(78, 3, 11), null);
			temp.setReportObjectId(3);
			tempFilters.put(new Integer(3), temp);	
		}
		{
			NumericObsPatientFilter temp;
			temp = new NumericObsPatientFilter(
					context.getConceptService().getConcept(new Integer(5497)),
					PatientSetService.Modifier.LESS_THAN,
					new Double(350));
			temp.setReportObjectId(4);
			tempFilters.put(new Integer(4), temp);
			temp = new NumericObsPatientFilter(
					context.getConceptService().getConcept(new Integer(5497)),
					PatientSetService.Modifier.LESS_EQUAL,
					new Double(350));
			temp.setReportObjectId(5);
			tempFilters.put(new Integer(5), temp);
			temp = new NumericObsPatientFilter(
					context.getConceptService().getConcept(new Integer(5497)),
					PatientSetService.Modifier.LESS_THAN,
					new Double(200));
			temp.setReportObjectId(6);
			tempFilters.put(new Integer(6), temp);
		}
	}
	
	/*
	public PatientFilter getPatientFilterById(Integer filterId) throws APIException {
		fillTempFilters();
		return tempFilters.get(filterId);
	}
	
	public Collection<PatientFilter> getAllPatientFilters() throws APIException {
		fillTempFilters();
		return Collections.unmodifiableCollection(tempFilters.values());
	}
	*/

}
