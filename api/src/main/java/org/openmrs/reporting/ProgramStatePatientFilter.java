/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.reporting;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsUtil;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ProgramStatePatientFilter extends CachingPatientFilter {
	
	private Program program;
	
	private List<ProgramWorkflowState> stateList;
	
	private Integer withinLastDays;
	
	private Integer withinLastMonths;
	
	private Integer untilDaysAgo;
	
	private Integer untilMonthsAgo;
	
	private Date sinceDate;
	
	private Date untilDate;
	
	public ProgramStatePatientFilter() {
	}
	
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append(".");
		if (getProgram() != null)
			sb.append(getProgram().getProgramId());
		sb.append(".");
		sb.append(
		    OpenmrsUtil.fromDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate,
		        untilDate)).append(".");
		sb.append(
		    OpenmrsUtil.toDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate,
		        untilDate)).append(".");
		if (getStateList() != null)
			for (ProgramWorkflowState s : getStateList())
				sb.append(s.getProgramWorkflowStateId()).append(",");
		return sb.toString();
	}
	
	public String getDescription() {
		StringBuilder ret = new StringBuilder();
		
		//boolean currentlyCase = withinLastDays != null && withinLastDays == 0
		//       && (withinLastMonths == null || withinLastMonths == 0);
		
		ret.append("Patients in program ");
		
		if (getProgram() != null) {
			if (getProgram().getConcept() == null)
				ret.append(" <CONCEPT> ");
			else {
				ret.append(getConceptName(program.getConcept()) + " ");
			}
		}
		
		if (stateList != null && stateList.size() > 0) {
			Map<ProgramWorkflow, Set<ProgramWorkflowState>> map = new HashMap<ProgramWorkflow, Set<ProgramWorkflowState>>();
			for (ProgramWorkflowState state : stateList) {
				ProgramWorkflow workflow = state.getProgramWorkflow();
				OpenmrsUtil.addToSetMap(map, workflow, state);
			}
			boolean first = true;
			for (Map.Entry<ProgramWorkflow, Set<ProgramWorkflowState>> e : map.entrySet()) {
				ret.append(first ? "with " : "or ");
				first = false;
				try {
					ret.append(e.getKey().getConcept().getName().getName());
				}
				catch (NullPointerException ex) {
					ret.append("CONCEPT?");
				}
				if (e.getValue().size() == 1)
					ret.append(" of " + e.getValue().iterator().next().getConcept().getName().getName());
				else {
					ret.append(" in [ ");
					for (Iterator<ProgramWorkflowState> i = e.getValue().iterator(); i.hasNext();) {
						ret.append(i.next().getConcept().getName().getName());
						if (i.hasNext())
							ret.append(" , ");
					}
					ret.append(" ]");
				}
			}
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
			ret.append("on or after " + Context.getDateFormat().format(sinceDate) + " ");
		if (untilDate != null)
			ret.append("on or before " + Context.getDateFormat().format(untilDate) + " ");
		
		return ret.toString();
	}
	
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		PatientSetService service = Context.getPatientSetService();
		return service.getPatientsByProgramAndState(program, stateList, fromDateHelper(), toDateHelper());
	}
	
	public boolean isReadyToRun() {
		return true;
	}
	
	private Date fromDateHelper() {
		Date ret = null;
		if (withinLastDays != null || withinLastMonths != null) {
			Calendar gc = Calendar.getInstance();
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
			Calendar gc = Calendar.getInstance();
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
	
	public List<ProgramWorkflowState> getStateList() {
		return stateList;
	}
	
	public void setStateList(List<ProgramWorkflowState> stateList) {
		this.stateList = stateList;
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
