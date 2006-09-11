package org.openmrs.web.controller.encounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class EncounterDisplayController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public class FieldLabel implements Comparable<FieldLabel> {
		private Integer pageNumber = 999;
		private Integer fieldNumber;
		private String fieldPart;
		private Float sortWeight;
		
		public FieldLabel() { }
		public FieldLabel(FormField ff) {
			setPageNumber(ff.getPageNumber());
			fieldNumber = ff.getFieldNumber();
			fieldPart = ff.getFieldPart();
			sortWeight = ff.getSortWeight();
		}
		public int compareTo(FieldLabel other) {
			int temp = OpenmrsUtil.comparewithNullAsGreatest(pageNumber, other.pageNumber);
			if (temp == 0)
				temp = OpenmrsUtil.comparewithNullAsGreatest(fieldNumber, other.fieldNumber);
			if (temp == 0)
				temp = OpenmrsUtil.comparewithNullAsGreatest(fieldPart, other.fieldPart);
			if (temp == 0)
				temp = OpenmrsUtil.comparewithNullAsGreatest(sortWeight, other.sortWeight);
			return temp;
		}
		public Float getSortWeight() {
			return sortWeight;
		}
		public void setSortWeight(Float sortWeight) {
			this.sortWeight = sortWeight;
		}
		public Integer getFieldNumber() {
			return fieldNumber;
		}
		public void setFieldNumber(Integer fieldNumber) {
			this.fieldNumber = fieldNumber;
		}
		public String getFieldPart() {
			return fieldPart;
		}
		public void setFieldPart(String fieldPart) {
			this.fieldPart = fieldPart;
		}
		public Integer getPageNumber() {
			return pageNumber;
		}
		public void setPageNumber(Integer pageNumber) {
			this.pageNumber = pageNumber == null ? 999 : pageNumber;
		}
		public String toString() {
			return (fieldNumber == null ? "" : fieldNumber) + ". " + (fieldPart == null ? "" : fieldPart);
		}
	}
	
	public class ObsGroupHolder {
		private FieldHolder parent;
		private Collection<Obs> observations;
		public ObsGroupHolder() {
			observations = new ArrayList<Obs>();
		}
		public void addObs(Obs o) {
			observations.add(o);
		}
		public Collection<Obs> getObservations() {
			return observations;
		}
		public void setObservations(Collection<Obs> observations) {
			this.observations = observations;
		}
		public FieldHolder getParent() {
			return parent;
		}
		public void setParent(FieldHolder parent) {
			this.parent = parent;
		}
		public List<List<Obs>> getObservationsByConcepts() {
			List<List<Obs>> ret = new ArrayList<List<Obs>>();
			for (Concept c : parent.getObsGroupConcepts()) {
				List<Obs> list = new ArrayList<Obs>();
				for (Obs o : observations)
					if (o.getConcept().equals(c))
						list.add(o);
				ret.add(list);
			}
			return ret;
		}
	}
	
	public class FieldHolder {
		private FieldLabel label;
		private Map<Concept, List<Obs>> observations;
		private Map<Integer, ObsGroupHolder> obsGroups;
		private LinkedHashSet<Concept> obsGroupConcepts;
		public FieldHolder() {
			observations = new LinkedHashMap<Concept, List<Obs>>();
			obsGroups = new TreeMap<Integer, ObsGroupHolder>();
			obsGroupConcepts = new LinkedHashSet<Concept>();
		}
		public FieldLabel getLabel() {
			return label;
		}
		public void setLabel(FieldLabel label) {
			this.label = label;
		}
		public Map<Concept, List<Obs>> getObservations() {
			return observations;
		}
		public void setObservations(Map<Concept, List<Obs>> observations) {
			this.observations = observations;
		}
		public Map<Integer, ObsGroupHolder> getObsGroups() {
			return obsGroups;
		}
		public void setObsGroups(Map<Integer, ObsGroupHolder> obsGroups) {
			this.obsGroups = obsGroups;
		}
		public LinkedHashSet<Concept> getObsGroupConcepts() {
			return obsGroupConcepts;
		}
		public void setObsGroupConcepts(LinkedHashSet<Concept> obsGroupConcepts) {
			this.obsGroupConcepts = obsGroupConcepts;
		}
		public void addConceptInConstruct(Concept c) {
			obsGroupConcepts.add(c);
		}
		public void addObservation(Obs o) {
			Integer obsGroupId = o.getObsGroupId();
			boolean obsGroupAnyway = obsGroupId == null && obsGroupConcepts.contains(o.getConcept()); 
			if (obsGroupId == null && !obsGroupAnyway) {
				List<Obs> obsForConcept = observations.get(o.getConcept());
				if (obsForConcept == null) {
					obsForConcept = new ArrayList<Obs>();
					observations.put(o.getConcept(), obsForConcept);
				}
				obsForConcept.add(o);
			} else {
				if (obsGroupAnyway)
					obsGroupId = o.getObsId(); // TODO: this relies on the convention that obsGroupId equals the obsId of one of the obs in that group. It would be nice to drop this requirement 
				ObsGroupHolder group = obsGroups.get(obsGroupId); 
				if (group == null) {
					group = new ObsGroupHolder();
					group.setParent(this);
					obsGroups.put(obsGroupId, group);
				}
				group.getObservations().add(o);
				obsGroupConcepts.add(o.getConcept());
			}
		}
	}
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Map model = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
		
	    	String encounterId = request.getParameter("encounterId");
	    	if (encounterId == null || encounterId.length() == 0)
	    		throw new IllegalArgumentException("encounterId is a required parameter");
	    	
	    	model.put("encounterId", Integer.valueOf(encounterId));
	    	
	    	Encounter encounter = context.getEncounterService().getEncounter(Integer.valueOf(encounterId));
	    	model.put("encounter", encounter);
	    	
			Form form = encounter.getForm();
			List<FormField> fields = new ArrayList<FormField>();
			
			// build up a Map<(fieldNumber + '.' + fieldPart), FieldHolder>. Later we'll take the FieldHolders and sort them as a list
			SortedMap<FieldLabel, FieldHolder> data = new TreeMap<FieldLabel, FieldHolder>();
			// find out which fieldLabel each concept maps to 
			Map<Concept, FieldLabel> conceptToFieldLabel = new HashMap<Concept, FieldLabel>();
			// some obs might not actually belong to a field
			List<Obs> otherObs = new ArrayList<Obs>();

			if (form != null) {
				fields = new ArrayList<FormField>(form.getFormFields());
				for (FormField ff : fields) {
					Concept c = ff.getField().getConcept();
					if (c != null) {
						Concept conceptInConstruct = null;
						if (ff.getParent() != null && ff.getParent().getParent() != null && ff.getParent().getField().getConcept() != null)
							conceptInConstruct = c;
						
						while (ff.getFieldNumber() == null && ff.getParent() != null) {
							ff = ff.getParent();
						}
						FieldLabel label = new FieldLabel(ff);
						conceptToFieldLabel.put(c, label);
						FieldHolder fh = data.get(label);
						if (fh == null) {
							fh = new FieldHolder();
							fh.setLabel(label);
							data.put(label, fh);
						}
						if (conceptInConstruct != null)
							fh.addConceptInConstruct(conceptInConstruct);
					}
				}
			}
			for (Obs o : encounter.getObs()) {
				FieldLabel label = conceptToFieldLabel.get(o.getConcept());
				if (label == null || !data.containsKey(label)) {
					otherObs.add(o);
				} else {
					data.get(label).addObservation(o);
				}
			}
			if (otherObs.size() > 0) {
				FieldLabel label = new FieldLabel();
				label.setPageNumber(999);
				label.setFieldNumber(999);
				FieldHolder holder = new FieldHolder();
				holder.setLabel(label);
				for (Obs obs : otherObs)
					holder.addObservation(obs);
				data.put(label, holder);
			}
	
			SortedSet<Integer> pageNumbers = new TreeSet<Integer>();
			for (FieldLabel fl : data.keySet()) {
				pageNumbers.add(fl.getPageNumber());
			}		
			
			List<Order> orders = new ArrayList<Order>(encounter.getOrders());
					
			model.put("showBlankFields", "true".equals(request.getParameter("showBlankFields")));
			model.put("pageNumbers", pageNumbers);
			model.put("form", form);
			model.put("data", data.values());
			model.put("otherObs", otherObs);
			model.put("orders", orders);
			model.put("locale", context.getLocale());
		}
		return new ModelAndView("/encounters/encounterDisplay", "model", model);
	}

}
