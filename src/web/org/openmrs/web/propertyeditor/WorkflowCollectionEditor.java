package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

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
	 * Note that this takes conceptIds, not programWorkflowIds!
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null && StringUtils.hasText(text)) {
			ConceptService cs = context.getConceptService();
			ProgramWorkflowService pws = context.getProgramWorkflowService();
			String[] conceptIds = text.split(" ");
			List<Integer> requestConceptIds = new Vector<Integer>();
			for (String id : conceptIds) {
				id = id.trim();
				if (!id.equals("") && !requestConceptIds.contains(Integer.valueOf(id))) //remove whitespace, blank lines, and duplicate entries
					requestConceptIds.add(Integer.valueOf(id));
			}
			
			Collection<ProgramWorkflow> newConceptList = new HashSet<ProgramWorkflow>();
			for (Integer conceptId : requestConceptIds) {
				ProgramWorkflow workflow = pws.getProgramWorkflowByConceptId(conceptId);
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
