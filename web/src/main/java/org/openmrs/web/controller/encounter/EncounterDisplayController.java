/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Field;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This is the java controller for the /openmrs/admin/encounters/encounterDisplay.list page. The jsp
 * for this display popup is located at /web/WEB-INF/view/encounters/encounterDisplay.jsp
 */
public class EncounterDisplayController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The page that obs are put on if they are not given a page number in their associated
	 * FormField object
	 */
	public static final Integer DEFAULT_PAGE_NUMBER = 999;
	
	/**
	 * This is the method called to produce the data and objects for the jsp page
	 *
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (Context.isAuthenticated()) {
			
			String encounterId = request.getParameter("encounterId");
			if (encounterId == null || encounterId.length() == 0) {
				throw new IllegalArgumentException("encounterId is a required parameter");
			}
			
			model.put("encounterId", Integer.valueOf(encounterId));
			
			Encounter encounter = Context.getEncounterService().getEncounter(Integer.valueOf(encounterId));
			model.put("encounter", encounter);
			
			List<FormField> formFields = new ArrayList<FormField>();
			if (encounter.getForm() != null && encounter.getForm().getFormFields() != null) {
				formFields.addAll(encounter.getForm().getFormFields());
			}
			
			// mapping from concept to FieldHolder. there should be only one
			// fieldholder (aka one row) per unique concept in the obs for an encounter 
			// these are the rows that will be returned and displayed
			// the values of this map will be returned and displayed as the rows
			// in the jsp view
			Map<Concept, FieldHolder> rowMapping = new HashMap<Concept, FieldHolder>();
			
			// loop over all obs in this encounter
			for (Obs obs : encounter.getObsAtTopLevel(false)) {
				// the question for this obs
				Concept conceptForThisObs = obs.getConcept();
				
				// try to get a unique formfield for this concept.  If one exists,
				// remove that formfield from the list of formfields
				FormField formField = popFormFieldForConcept(formFields, conceptForThisObs);
				
				// try to get a previously added row 
				FieldHolder fieldHolder = rowMapping.get(conceptForThisObs);
				if (fieldHolder != null) {
					// there is already a row that uses the same concept as its
					// question.  lets put this obs in that same row
					fieldHolder.addObservation(obs);
				} else {
					// if we don't have a row for this concept yet, create one
					
					// if this observation was added to the encounter magically
					// (meaning there isn't a formfield for it) create a generic
					// formfield to use with this obs
					if (formField == null) {
						formField = new FormField();
						formField.setPageNumber(DEFAULT_PAGE_NUMBER);
						formField.setFieldNumber(null);
					}
					
					fieldHolder = new FieldHolder(formField, obs);
					
					// add this row to the list of all possible rows
					rowMapping.put(conceptForThisObs, fieldHolder);
				}
				
			}
			
			// now that we're done processing all of the obs, get all of the
			// rows that we will return
			// this is not the object we will give to the jsp view, the jsp
			// view only sees the rows on a per page basis
			List<FieldHolder> rows = new ArrayList<FieldHolder>();
			rows.addAll(rowMapping.values());
			Collections.sort(rows);
			
			String usePages = Context.getAdministrationService().getGlobalProperty("dashboard.encounters.usePages", "true")
			        .toLowerCase();
			if (usePages.equals("smart")) {
				// if more than 50% of fields have page numbers, then use pages
				int with = 0;
				int without = 0;
				for (FieldHolder holder : rows) {
					if (holder.getPageNumber() == DEFAULT_PAGE_NUMBER) {
						++without;
					} else {
						++with;
					}
				}
				usePages = "" + (with > without);
			}
			
			// this is a mapping from page number to list of rows on that page
			// this is the object returned to the view that should be looped over
			// and displayed.  Each value part of the key-value pair is sorted
			// according to FieldHolder.compareTo
			Map<Integer, List<FieldHolder>> pages = new HashMap<Integer, List<FieldHolder>>();
			
			if (Boolean.valueOf(usePages).booleanValue()) {
				// if we're doing pages
				// loop over all of the rows and put them in pages
				for (FieldHolder row : rows) {
					Integer pageNumber = row.getPageNumber();
					List<FieldHolder> thisPage = pages.get(pageNumber);
					
					// if a page (set of rows) for this pagNumber doesn't exist yet, create it
					if (thisPage == null) {
						thisPage = new ArrayList<FieldHolder>();
						pages.put(pageNumber, thisPage);
					}
					
					// add this row to its page
					thisPage.add(row);
				}
			} else {
				// if we're not doing pages, put all rows on the first page
				List<FieldHolder> pageOneRows = new ArrayList<FieldHolder>();
				pageOneRows.addAll(rows);
				pages.put(0, pageOneRows);
			}
			
			model.put("showBlankFields", "true".equals(request.getParameter("showBlankFields")));
			model.put("usePages", Boolean.valueOf(usePages).booleanValue());
			model.put("pageNumbers", pages.keySet());
			model.put("pages", pages);
			model.put("orders", encounter.getOrders());
			model.put("locale", Context.getLocale());
		}
		return new ModelAndView("/encounters/encounterDisplay", "model", model);
	}
	
	/**
	 * This will look through all form fields and find the one that has a concept that matches the
	 * given concept If one is found, that formfield is removed from the given list
	 * <code>formFields</code> If there are none found, null is returned.
	 *
	 * @param formFields list of FormFields to rifle through
	 * @param conceptToSearchFor concept to look for in <code>formFields</code>
	 * @return FormField object from <code>formFields</code> or null
	 */
	private FormField popFormFieldForConcept(List<FormField> formFields, Concept conceptToSearchFor) {
		//drop out if a null concept was passed in
		if (conceptToSearchFor == null) {
			return null;
		}
		
		Integer conceptId = conceptToSearchFor.getConceptId();
		
		// look through all formFields for this concept
		for (FormField formField : formFields) {
			Field field = formField.getField();
			if (field != null) {
				Concept otherConcept = field.getConcept();
				if (otherConcept != null && conceptId.equals(otherConcept.getConceptId())) {
					// found a FormField.  Remove it from the list and
					// return it
					formFields.remove(formField);
					return formField;
				}
			}
		}
		
		// no formField with concept = conceptToSearchFor was found
		return null;
	}
	
	/**
	 * This class represents one row to display on the jsp page
	 */
	public class FieldHolder implements Comparable<FieldHolder> {
		
		/**
		 * The formfield that represents the labeling of this row This is also used in the sorting.
		 * See
		 * {@link #compareTo(org.openmrs.web.controller.encounter.EncounterDisplayController.FieldHolder)}
		 * See {@link #getLabel()} for the labeling
		 */
		private FormField formField = null;
		
		/**
		 * these are the rows in the obsGroup table If this is not an obs grouper, this will contain
		 * only one obs
		 */
		private List<Obs> obs;
		
		/**
		 * these are the column names in the obsGroup table
		 */
		private LinkedHashSet<Concept> groupMemberConcepts;
		
		/**
		 * A row must be created with both a FormField to act as its label and an obs that is the
		 * first of possibly several rows to display
		 *
		 * @throws Exception if the obsToAdd is an invalid type (meaning its contained in another
		 *             obs group)
		 */
		public FieldHolder(FormField formField, Obs obsToAdd) throws Exception {
			obs = new LinkedList<Obs>();
			groupMemberConcepts = new LinkedHashSet<Concept>();
			this.formField = formField;
			addObservation(obsToAdd);
			if (obsToAdd.getObsGroup() != null) {
				throw new Exception(
				        "FieldHolders only contain top-level obs.  This obs is contained in some other group, it is added automagically there. "
				                + obsToAdd);
			}
		}
		
		/**
		 * public getter method for the formfield that is the label for this row
		 *
		 * @return FormField for this row
		 */
		public FormField getFormField() {
			return formField;
		}
		
		/**
		 * public getter for the columns (that are unique concepts across all obs in this
		 * FieldHolder)
		 *
		 * @return unique concepts across these obs
		 */
		public Set<Concept> getGroupMemberConcepts() {
			return groupMemberConcepts;
		}
		
		/**
		 * public getter for the obs that are the different rows for this FieldHolder. If this isn't
		 * a grouping type of row, the set could still have multiple obs in it because there are
		 * multiple questions (obs) in this encounter that are asking the same thing (same concept)
		 *
		 * @return List of obs for this row
		 */
		public List<Obs> getObs() {
			return obs;
		}
		
		/**
		 * Convenience method to know whether this row is an obs grouping and should be displayed
		 * with a table or if its a single one-and-done obs and should just be shown as one value
		 *
		 * @return true/false whether this holder is for an obs grouping
		 */
		public boolean isObsGrouping() {
			if (obs == null || groupMemberConcepts == null) {
				return false;
			}
			
			return groupMemberConcepts.size() > 1 || obs.get(0).isObsGrouping();
		}
		
		/**
		 * List of columns for each obs grouper in this fieldholder. Not every grouper will have
		 * every column (concept), so some cells will be null. The columns are determined by
		 * getObsGroupConcepts()
		 *
		 * @return a matrix of columns
		 */
		public Map<Obs, List<List<Obs>>> getObsGroupMatrix() {
			Map<Obs, List<List<Obs>>> matrix = new HashMap<Obs, List<List<Obs>>>();
			
			for (Obs obsGrouper : obs) {
				List<List<Obs>> obsRow = new LinkedList<List<Obs>>();
				// create a hashmap of concept-->obs for these groupedObs
				Map<Concept, List<Obs>> conceptToObsMap = new HashMap<Concept, List<Obs>>();
				for (Obs groupedObs : obsGrouper.getGroupMembers()) {
					List<Obs> obsMatchingThisConcept = conceptToObsMap.get(groupedObs.getConcept());
					if (obsMatchingThisConcept == null) {
						obsMatchingThisConcept = new LinkedList<Obs>();
					}
					obsMatchingThisConcept.add(groupedObs);
					conceptToObsMap.put(groupedObs.getConcept(), obsMatchingThisConcept);
				}
				
				// loop over each possible concept and put the obs in the 
				// row in the order of the columns.  if no obs is found, 
				// the cell is blank (null)
				for (Concept concept : groupMemberConcepts) {
					// loop over each obs in this group
					obsRow.add(conceptToObsMap.get(concept));
				}
				matrix.put(obsGrouper, obsRow);
			}
			
			return matrix;
		}
		
		/**
		 * Add another obs grouper to this row This method shouldn't be called with obs that are
		 * within another grouped obs. This should only be called with the parent obs grouper.
		 *
		 * @param obsToAdd Obs that should be an obs grouper
		 */
		public void addObservation(Obs obsToAdd) {
			
			// if we are in an obs grouping, make sure that we know 
			// about each concept in each of the underling grouped obs
			if (obsToAdd.isObsGrouping()) {
				for (Obs groupedObs : obsToAdd.getGroupMembers()) {
					groupMemberConcepts.add(groupedObs.getConcept());
				}
			} else {
				// this is not a grouping, but its concept is being used
				// in another row, so just add it to the list of concepts
				// in this row.  The values won't be spit out in a table
				// but they will be displayed together
				groupMemberConcepts.add(obsToAdd.getConcept());
			}
			
			// add the given obs to the list of obs for this row 
			obs.add(obsToAdd);
		}
		
		/**
		 * Use this row's FormField to make comparisons about where to put it in relation to the
		 * FieldHolder's
		 *
		 * @see org.openmrs.FormField#compareTo(org.openmrs.FormField)
		 */
		public int compareTo(FieldHolder other) {
			int temp = OpenmrsUtil
			        .compareWithNullAsGreatest(formField.getPageNumber(), other.getFormField().getPageNumber());
			if (temp == 0) {
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFieldNumber(), other.getFormField()
				        .getFieldNumber());
			}
			if (temp == 0) {
				temp = OpenmrsUtil.compareWithNullAsGreatest(formField.getFieldPart(), other.getFormField().getFieldPart());
			}
			if (temp == 0 && formField.getPageNumber() == null && formField.getFieldNumber() == null
			        && formField.getFieldPart() == null) {
				temp = OpenmrsUtil
				        .compareWithNullAsGreatest(formField.getSortWeight(), other.getFormField().getSortWeight());
			}
			return temp;
		}
		
		/**
		 * Indicates whether some other object is "equal to" this one.
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof FieldHolder)) {
				return false;
			}
			FieldHolder other = (FieldHolder) obj;
			return compareTo(other) == 0;
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(getPageNumber()).build();
		}
		
		/**
		 * Convenience method to get the label that this field should have. This is produced from
		 * the formfield associated with this row
		 *
		 * @return String representing the label to be displayed for this row
		 */
		public String getLabel() {
			String label = "";
			if (formField.getFieldNumber() != null) {
				label = formField.getFieldNumber() + ".";
			}
			
			if (formField.getFieldPart() != null) {
				label += formField.getFieldPart();
			}
			
			if ("".equals(label)) {
				return "--";
			} else {
				return label;
			}
		}
		
		/**
		 * Convenience method to get the page number for this row. This just checks the associated
		 * form field for its assigned page number. If the formfield doesn't have a page, this row
		 * is thrown on the DEFAULT_PAGE_NUMBERth page.
		 *
		 * @return page number for this row
		 */
		public Integer getPageNumber() {
			if (formField == null || formField.getPageNumber() == null) {
				return DEFAULT_PAGE_NUMBER;
			}
			
			return formField.getPageNumber();
		}
	}
}
