package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class WorkflowCollectionEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private Context context;
	
	public WorkflowCollectionEditor(Context c) {
		this.context = c;
	}
	
	/**
	 * Note that this takes {programId}.{conceptId}, not programWorkflowIds!
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null && StringUtils.hasText(text)) {
			ConceptService cs = context.getConceptService();
			ProgramWorkflowService pws = context.getProgramWorkflowService();
			String[] conceptIds = text.split(" ");
			List<String> uniqueEntries = new ArrayList<String>();
			for (String id : conceptIds) {
				id = id.trim();
				if (id.equals("") || uniqueEntries.contains(id)) {
					continue;
				}
				uniqueEntries.add(id);
			}
			
			// remove duplicates, keeping the one that already has a programId
			Collections.sort(uniqueEntries, new Comparator<String>() {
					public int compare(String left, String right) {
						Integer l = left.startsWith(".") ? 1 : 0;
						Integer r = right.startsWith(".") ? 1 : 0;
						return l.compareTo(r);
					}
				});
			Set<Integer> conceptIdsSoFar = new HashSet<Integer>();
			for (Iterator<String> i = uniqueEntries.iterator(); i.hasNext(); ) {
				String id = i.next();
				Integer programId = null;
				Integer conceptId = null;
				if (id.startsWith(".")) {
					conceptId = Integer.valueOf(id);
				} else {
					String[] temp = id.split("\\.");
					programId = Integer.valueOf(temp[0]);
					conceptId = Integer.valueOf(temp[1]);
				}
				if (conceptIdsSoFar.contains(conceptId)) {
					i.remove();
				} else {
					conceptIdsSoFar.add(conceptId);
				}
			}
			
			Collection<ProgramWorkflow> newConceptList = new HashSet<ProgramWorkflow>();
			for (String id : uniqueEntries) {
				Integer programId = null;
				Integer conceptId = null;
				if (id.startsWith(".")) {
					conceptId = Integer.valueOf(id);
				} else {
					String[] temp = id.split("\\.");
					programId = Integer.valueOf(temp[0]);
					conceptId = Integer.valueOf(temp[1]);
				}
				ProgramWorkflow workflow = null;
				if (programId != null) {
					workflow = pws.findWorkflowByProgramAndConcept(programId, conceptId);
				}
				if (workflow == null) {
					workflow = new ProgramWorkflow();
					workflow.setConcept(cs.getConcept(conceptId));
				}
				
				newConceptList.add(workflow);
			}

			setValue(newConceptList);
		} else {
			setValue(null);
		}
	}

}
