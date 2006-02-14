package org.openmrs.reporting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.APIException;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.reporting.db.ReportDAO;

public class ReportService {
	
	private Context context;
	private DAOContext daoContext;
	
	public ReportService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private ReportDAO dao() {
		return daoContext.getReportDAO();
	}

	public java.util.Set<Report> getAllReports() {
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
			temp = new PatientCharacteristicFilter(null, new java.util.Date(78, 3, 11), null);
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
	
	public PatientFilter getPatientFilterById(Integer filterId) throws APIException {
		fillTempFilters();
		return tempFilters.get(filterId);
	}
	
	public Collection<PatientFilter> getAllPatientFilters() throws APIException {
		fillTempFilters();
		return Collections.unmodifiableCollection(tempFilters.values());
	}
}
