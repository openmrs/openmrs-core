package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
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
import org.openmrs.util.Helper;
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
			PatientProgram p = context.getProgramWorkflowService().getPatientProgram(patientProgramId);
			List<PatientState> states = new ArrayList<PatientState>(p.getStates());
			if (programWorkflowId != null) {
				for (Iterator<PatientState> i = states.iterator(); i.hasNext(); ) {
					PatientState st = i.next();
					if (st.getVoided() || !programWorkflowId.equals(st.getState().getProgramWorkflow().getProgramWorkflowId()))
						i.remove();
				}
			}
			Collections.sort(states, new Comparator<PatientState>() {
					public int compare(PatientState left, PatientState right) {
						int temp = left.getState().getProgramWorkflow().getProgramWorkflowId().compareTo(right.getState().getProgramWorkflow().getProgramWorkflowId());
						if (temp == 0) {
							Date leftDate = left.getStartDate();
							Date rightDate = right.getStartDate();
							if (leftDate == null)
								temp = -1;
							else if (rightDate == null)
								temp = 1;
							else
								temp = leftDate.compareTo(rightDate);
						}
						return temp;
					}
				});
			for (PatientState s : states) {
				ret.add(new PatientStateItem(context, s));
			}
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
			boolean anyChange = Helper.nullSafeEquals(dateEnrolled, pp.getDateEnrolled());
			anyChange |= Helper.nullSafeEquals(dateCompleted, pp.getDateCompleted());
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
			
}
