package org.openmrs.reporting;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;

public class ProgramStatePatientFilter extends AbstractPatientFilter implements
		PatientFilter {

	private Program program;
	private ProgramWorkflowState state;
	private Integer withinLastDays;
	private Integer withinLastMonths;
	private Integer untilDaysAgo;
	private Integer untilMonthsAgo;
	private Date sinceDate;
	private Date untilDate;
	
	public ProgramStatePatientFilter() { }
	
	public String getDescription() {
		StringBuilder ret = new StringBuilder();
		
		ret.append("Patients in program ");
		
		if (getProgram() != null) {
			if (getProgram().getConcept() == null)
				ret.append(" <CONCEPT> ");
			else {
				ret.append(getConceptName(program.getConcept()));
			}
		}
		
		if (state != null) {
			ProgramWorkflow workflow = state.getProgramWorkflow();
			if (workflow.getConcept() == null)
				workflow = Context.getProgramWorkflowService().getWorkflow(state.getProgramWorkflow().getProgramWorkflowId());
			Concept stateConcept = state.getConcept();
			if (stateConcept == null)
				stateConcept = Context.getProgramWorkflowService().getState(state.getProgramWorkflowStateId()).getConcept();
			ret.append("with " + getConceptName(workflow.getConcept()) + " of " + getConceptName(stateConcept) + " ");
		}
		if (withinLastMonths != null || withinLastDays != null) {
			ret.append("within the last ");
			if (withinLastMonths != null)
				ret.append(withinLastMonths + " month(s) ");
			if (withinLastDays != null)
				ret.append(withinLastDays + " day(s) ");
		}
		// TODO untilDaysAgo untilMonthsAgo
		if (sinceDate != null)
			ret.append("on or after " + sinceDate + " ");
		if (untilDate != null)
			ret.append("on or before " + untilDate + " ");

		return ret.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.intersect(service.getPatientsByProgramAndState(program, state, fromDateHelper(), toDateHelper()));
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.subtract(service.getPatientsByProgramAndState(program, state, fromDateHelper(), toDateHelper()));
	}

	public boolean isReadyToRun() {
		return true;
	}

	private Date fromDateHelper() {
		Date ret = null;
		if (withinLastDays != null || withinLastMonths != null) {
			Calendar gc = new GregorianCalendar();
			if (withinLastDays != null)
				gc.add(Calendar.DAY_OF_MONTH, -withinLastDays);
			if (withinLastMonths != null)
				gc.add(Calendar.MONTH, -withinLastMonths);
			ret = gc.getTime();
		}
		if (sinceDate != null && (ret == null || sinceDate.after(ret)))
			ret = sinceDate;
		return ret;
	}
	
	private Date toDateHelper() {
		Date ret = null;
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			Calendar gc = new GregorianCalendar();
			if (untilDaysAgo != null)
				gc.add(Calendar.DAY_OF_MONTH, -untilDaysAgo);
			if (untilMonthsAgo != null)
				gc.add(Calendar.MONTH, -untilMonthsAgo);
			ret = gc.getTime();
		}
		if (untilDate != null && (ret == null || untilDate.before(ret)))
			ret = untilDate;
		return ret;
	}

	// getters and setters
	
	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public Date getSinceDate() {
		return sinceDate;
	}

	public void setSinceDate(Date sinceDate) {
		this.sinceDate = sinceDate;
	}

	public ProgramWorkflowState getState() {
		return state;
	}

	public void setState(ProgramWorkflowState state) {
		this.state = state;
	}

	public Date getUntilDate() {
		return untilDate;
	}

	public void setUntilDate(Date untilDate) {
		this.untilDate = untilDate;
	}

	public Integer getUntilDaysAgo() {
		return untilDaysAgo;
	}

	public void setUntilDaysAgo(Integer untilDaysAgo) {
		this.untilDaysAgo = untilDaysAgo;
	}

	public Integer getUntilMonthsAgo() {
		return untilMonthsAgo;
	}

	public void setUntilMonthsAgo(Integer untilMonthsAgo) {
		this.untilMonthsAgo = untilMonthsAgo;
	}

	public Integer getWithinLastDays() {
		return withinLastDays;
	}

	public void setWithinLastDays(Integer withinLastDays) {
		this.withinLastDays = withinLastDays;
	}

	public Integer getWithinLastMonths() {
		return withinLastMonths;
	}

	public void setWithinLastMonths(Integer withinLastMonths) {
		this.withinLastMonths = withinLastMonths;
	}

}
