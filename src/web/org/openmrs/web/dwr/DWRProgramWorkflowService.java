package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRProgramWorkflowService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public PatientProgramItem getPatientProgram(Integer patientProgramId) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			return new PatientProgramItem(context, context.getProgramWorkflowService().getPatientProgram(patientProgramId));
		}
		return null;
	}
	
	public Vector<PatientStateItem> getPatientStates(Integer patientProgramId, Integer programWorkflowId) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<PatientStateItem> ret = new Vector<PatientStateItem>();
			ProgramWorkflowService s = context.getProgramWorkflowService();
			PatientProgram p = s.getPatientProgram(patientProgramId);
			ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
			for (PatientState st : p.statesInWorkflow(wf, false))
				ret.add(new PatientStateItem(context, st));
			return ret;
		}
		return null;
	}

	
	DateFormat ymdDf = new SimpleDateFormat("yyyy-MM-dd");
	public void updatePatientProgram(Integer patientProgramId, String enrollmentDateYmd, String completionDateYmd) throws ParseException {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			PatientProgram pp = context.getProgramWorkflowService().getPatientProgram(patientProgramId);
			Date dateEnrolled = null;
			Date dateCompleted = null;
			if (enrollmentDateYmd != null && enrollmentDateYmd.length() > 0)
				dateEnrolled = ymdDf.parse(enrollmentDateYmd);
			if (completionDateYmd != null && completionDateYmd.length() > 0)
				dateCompleted = ymdDf.parse(completionDateYmd);
			boolean anyChange = OpenmrsUtil.nullSafeEquals(dateEnrolled, pp.getDateEnrolled());
			anyChange |= OpenmrsUtil.nullSafeEquals(dateCompleted, pp.getDateCompleted());
			if (anyChange) {
				pp.setDateEnrolled(dateEnrolled);
				pp.setDateCompleted(dateCompleted);
				context.getProgramWorkflowService().updatePatientProgram(pp);
			}
		}
	}
	
	public void deletePatientProgram(Integer patientProgramId, String reason) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			PatientProgram p = context.getProgramWorkflowService().getPatientProgram(patientProgramId);
			context.getProgramWorkflowService().voidPatientProgram(p, reason);
		}
	}
	
	public Vector<ListItem> getPossibleNextStates(Integer patientProgramId, Integer programWorkflowId) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<ListItem> ret = new Vector<ListItem>();
			List<ProgramWorkflowState> states = context.getProgramWorkflowService()
				.getPossibleNextStates(context.getProgramWorkflowService().getPatientProgram(patientProgramId),
									   context.getProgramWorkflowService().getWorkflow(programWorkflowId));
			for (ProgramWorkflowState state : states) {
				ListItem li = new ListItem();
				li.setId(state.getProgramWorkflowStateId());
				li.setName(state.getConcept().getName(context.getLocale(), false).getName());
				ret.add(li);
			}
			return ret;
		}
		return null;
	}
	
	public void changeToState(Integer patientProgramId, Integer programWorkflowId,
			Integer programWorkflowStateId, String onDateDMY) throws ParseException {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			ProgramWorkflowService s = context.getProgramWorkflowService(); 
			PatientProgram pp = s.getPatientProgram(patientProgramId);
			ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
			ProgramWorkflowState st = s.getState(programWorkflowStateId);
			Date onDate = null;
			if (onDateDMY != null && onDateDMY.length() > 0)
				onDate = ymdDf.parse(onDateDMY);
			s.changeToState(pp, wf, st, onDate);
		}
	}
	
	public void voidLastState(Integer patientProgramId, Integer programWorkflowId, String voidReason) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			ProgramWorkflowService s = context.getProgramWorkflowService();
			PatientProgram pp = s.getPatientProgram(patientProgramId);
			ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
			s.voidLastState(pp, wf, voidReason);
		}
	}
}
