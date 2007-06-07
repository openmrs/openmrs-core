package org.openmrs.web.controller.encounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class EncounterDisplayController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public class FieldLabel implements Comparable<FieldLabel> {
		private Integer pageNumber = 999;
		private Integer fieldNumber = null;
		private String fieldPart = "";
		private Float sortWeight = null;
		
		public FieldLabel() { }
		public FieldLabel(FormField ff) {
			setPageNumber(ff.getPageNumber());
			fieldNumber = ff.getFieldNumber();
			fieldPart = ff.getFieldPart();
			if (fieldPart == null)
				fieldPart = "";
			sortWeight = ff.getSortWeight();
		}
		public int compareTo(FieldLabel other) {
			int temp = OpenmrsUtil.compareWithNullAsGreatest(pageNumber, other.pageNumber);
			if (temp == 0)
				temp = OpenmrsUtil.compareWithNullAsGreatest(fieldNumber, other.fieldNumber);
			if (temp == 0)
				temp = OpenmrsUtil.compareWithNullAsGreatest(fieldPart, other.fieldPart);
			if (temp == 0 && pageNumber == null && fieldNumber == null && fieldPart == null)
				temp = OpenmrsUtil.compareWithNullAsGreatest(sortWeight, other.sortWeight);
			return temp;
		}
		/*
		public boolean equals(Object o) {
			if (!(o instanceof FieldLabel)) return false;
			FieldLabel other = (FieldLabel)o;
			boolean equalObject = false;
			if (pageNumber != null)
				equalObject = equalObject && pageNumber.equals(other.getPageNumber());
			if (fieldNumber != null)
				equalObject = equalObject && fieldNumber.equals(other.getFieldNumber());
			if (fieldPart != null)
				equalObject = equalObject && pageNumber.equals(other.getFieldPart());
			
			return equalObject;
		}
		*/
		public int hashCode() {
			int ret = 0;
			if (pageNumber != null)
				ret += pageNumber * 100000;
			if (fieldNumber != null)
				ret += fieldNumber.hashCode() * 11;
			if (fieldPart != null)
				ret += fieldPart.hashCode();
			return ret;
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
			String s = (fieldNumber == null ? "" : fieldNumber + ".") + (fieldPart == null ? "" : fieldPart);
			return s.equals("") ? "--" : s; 
		}
		public String uid() {
			return super.toString();
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
			List<List<Obs>> ret = new Vector<List<Obs>>();
			for (Concept c : parent.getObsGroupConceptsWithObs()) {
				List<Obs> list = new Vector<Obs>();
				Integer id = c.getConceptId();
				for (Obs o : observations) {
					if (id.equals(o.getConcept().getConceptId()))
						list.add(o);
				}
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
		/**
		 * @return concepts in obsGroupConcepts for which an obs exists in observations 
		 */
		public List<Concept> getObsGroupConceptsWithObs() {
			List<Concept> ret = new ArrayList<Concept>();
			Set<Concept> withObs = new HashSet<Concept>();
			for (ObsGroupHolder ogh : obsGroups.values())
				for (Obs o : ogh.getObservations())
					withObs.add(o.getConcept());
			ret.addAll(getObsGroupConcepts());
			ret.retainAll(withObs);
			return ret;
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

			//log.error("o: " + o.getObsId());
			//log.error("groupId: " + o.getObsGroupId());
			//log.error("concept: " + o.getConcept());
			
			// just because a field has a parent that is a set doesn't mean its a construct (comment #234)
			boolean obsGroupAnyway = obsGroupId == null && obsGroupConcepts.contains(o.getConcept()); 
			if (obsGroupId == null && !obsGroupAnyway) {
				List<Obs> obsForConcept = observations.get(o.getConcept());
				if (obsForConcept == null) {
					obsForConcept = new ArrayList<Obs>();
					observations.put(o.getConcept(), obsForConcept);
				}
				obsForConcept.add(o);
			} else {
				// commenting out these two lines per comment #234
				if (obsGroupAnyway)
				//if (obsGroupId == null)
					obsGroupId = o.getObsId(); // TODO: this relies on the convention that obsGroupId equals the obsId of one of the obs in that group. It would be nice to drop this requirement 
				ObsGroupHolder group = obsGroups.get(obsGroupId);
				//log.error("group: " + group);
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
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (Context.isAuthenticated()) {
		
	    	String encounterId = request.getParameter("encounterId");
	    	if (encounterId == null || encounterId.length() == 0)
	    		throw new IllegalArgumentException("encounterId is a required parameter");
	    	
	    	model.put("encounterId", Integer.valueOf(encounterId));
	    	
	    	Encounter encounter = Context.getEncounterService().getEncounter(Integer.valueOf(encounterId));
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
						FormField parent;
						if ((parent = ff.getParent()) != null) {
							Concept fieldConcept;
							if (parent.getParent() != null 
								&& (fieldConcept = parent.getField().getConcept()) != null
								&& fieldConcept.isSet())
									conceptInConstruct = c;
						}
						
						// only get parent's form field if this concept is in a construct
						if (conceptInConstruct != null) {
							while (ff.getFieldNumber() == null && ff.getParent() != null) {
								ff = ff.getParent();
							}
						}
						
						FieldLabel label = new FieldLabel(ff);
						conceptToFieldLabel.put(c, label);
						FieldHolder fh = data.get(label);
						if (fh == null) {
							fh = new FieldHolder();
							log.debug("label: " + label + " uid: " + label.uid());
							fh.setLabel(label);
							data.put(label, fh);
						}
						if (conceptInConstruct != null)
							fh.addConceptInConstruct(conceptInConstruct);
					}
				}
			}
			
			for (Obs o : encounter.getObs()) {
				if (o.isVoided())
					continue;
				
				FieldLabel label = conceptToFieldLabel.get(o.getConcept());
				// if the label exists and has a form field
				if (label == null || !data.containsKey(label)) {
					log.debug("obsGroupId: " + o.getObsGroupId());
					otherObs.add(o);
				} else {
					data.get(label).addObservation(o);
				}
			}
			
			if (otherObs.size() > 0) {
				FieldLabel label = new FieldLabel();
				label.setPageNumber(999);
				label.setFieldNumber(null);
				FieldHolder holder = new FieldHolder();
				log.debug("other obs label: " + label);
				holder.setLabel(label);
				for (Obs obs : otherObs)
					holder.addObservation(obs);
				data.put(label, holder);
			}
	
			SortedSet<Integer> pageNumbers = new TreeSet<Integer>();
			
			String usePages = Context.getAdministrationService().getGlobalProperty("dashboard.encounters.usePages", "true").toLowerCase();
			if (usePages.equals("smart")) {
				// if more than 50% of fields have page numbers, then use pages
				int with = 0;
				int without = 0;
				for (FieldLabel l : data.keySet()) {
					if (l.getPageNumber() == null || l.getPageNumber() == 999)
						++without;
					else
						++with;
				}
				usePages = "" + (with > without);
			}
			
			boolean usePageNumbers = Boolean.valueOf(usePages);
			
			if (usePageNumbers) {
				for (FieldLabel fl : data.keySet()) {
					pageNumbers.add(fl.getPageNumber());
				}
			} else {
				pageNumbers.add(0);
			}
			
			// fields are already sorted due to SortedMap earlier
			Map<Integer, List<FieldHolder>> pages = new HashMap<Integer, List<FieldHolder>>();
			for (Map.Entry<FieldLabel, FieldHolder> e : data.entrySet()) {
				Integer pageNumber = usePageNumbers ? e.getKey().getPageNumber() : 0;
				List<FieldHolder> thisPage = pages.get(pageNumber);
				if (thisPage == null) {
					thisPage = new ArrayList<FieldHolder>();
					pages.put(pageNumber, thisPage);
				}
				thisPage.add(e.getValue());
			}
			
			List<Order> orders = new ArrayList<Order>(encounter.getOrders());
					
			model.put("showBlankFields", "true".equals(request.getParameter("showBlankFields")));
			model.put("usePages", usePageNumbers);
			model.put("pageNumbers", pageNumbers);
			model.put("form", form);
			model.put("pages", pages);
			model.put("otherObs", otherObs);
			model.put("orders", orders);
			model.put("locale", Context.getLocale());
		}
		return new ModelAndView("/encounters/encounterDisplay", "model", model);
	}

}
