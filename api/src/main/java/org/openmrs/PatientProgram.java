/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PatientProgram
 */
public class PatientProgram extends BaseChangeableOpenmrsData implements Customizable<PatientProgramAttribute>{
	
	public static final long serialVersionUID = 0L;
	
	// ******************
	// Properties
	// ******************
	
	private Integer patientProgramId;
	
	private Patient patient;
	
	private Program program;
	
	private Location location;
	
	private Date dateEnrolled;
	
	private Date dateCompleted;
	
	private Concept outcome;
	
	private Set<PatientState> states = new HashSet<>();
         
	private Set<PatientProgramAttribute> attributes = new LinkedHashSet<>();
	
	// ******************
	// Constructors
	// ******************
	
	/** Default Constructor */
	public PatientProgram() {
	}
	
	/** Constructor with id */
	public PatientProgram(Integer patientProgramId) {
		setPatientProgramId(patientProgramId);
	}

	/**
	 * Does a mostly-shallow copy of this PatientProgram. Does not copy patientProgramId. The
	 * 'states' property will be deep-copied.
	 * 
	 * @return a shallow copy of this PatientProgram
	 */
	public PatientProgram copy() {
		return copyHelper(new PatientProgram());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of PatientProgram to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes.
	 * 
	 * @param target a PatientProgram that will have the state of <code>this</code> copied into it
	 * @return the PatientProgram that was passed in, with state copied into it
	 */
	protected PatientProgram copyHelper(PatientProgram target) {
		target.setPatient(this.getPatient());
		target.setProgram(this.getProgram());
		target.setLocation(this.getLocation());
		target.setDateEnrolled(this.getDateEnrolled());
		target.setDateCompleted(target.getDateCompleted());
		Set<PatientState> statesCopy = new HashSet<>();
		if (this.getStates() != null) {
			for (PatientState s : this.getStates()) {
				PatientState stateCopy = s.copy();
				stateCopy.setPatientProgram(target);
				statesCopy.add(stateCopy);
			}
		}
		target.setStates(statesCopy);
		target.setCreator(this.getCreator());
		target.setDateCreated(this.getDateCreated());
		target.setChangedBy(this.getChangedBy());
		target.setDateChanged(this.getDateChanged());
		target.setVoided(this.getVoided());
		target.setVoidedBy(this.getVoidedBy());
		target.setDateVoided(this.getDateVoided());
		target.setVoidReason(this.getVoidReason());
		return target;
	}
	
	// ******************
	// Instance methods
	// ******************
	
	/**
	 * Returns true if the associated {@link Patient} is enrolled in the associated {@link Program}
	 * on the passed {@link Date}
	 * 
	 * @param onDate - Date to check for PatientProgram enrollment
	 * @return boolean - true if the associated {@link Patient} is enrolled in the associated
	 *         {@link Program} on the passed {@link Date}
	 */
	public boolean getActive(Date onDate) {
		if (onDate == null) {
			onDate = new Date();
		}
		return !getVoided() && (getDateEnrolled() == null || OpenmrsUtil.compare(getDateEnrolled(), onDate) <= 0)
		        && (getDateCompleted() == null || OpenmrsUtil.compare(getDateCompleted(), onDate) > 0);
	}
	
	/**
	 * Returns true if the associated {@link Patient} is currently enrolled in the associated
	 * {@link Program}
	 * 
	 * @return boolean - true if the associated {@link Patient} is currently enrolled in the
	 *         associated {@link Program}
	 */
	public boolean getActive() {
		return getActive(null);
	}
	
	/**
	 * Returns the {@link PatientState} associated with this PatientProgram that has an id that
	 * matches the passed <code>patientStateId</code>
	 * 
	 * @param patientStateId - The identifier to use to lookup a {@link PatientState}
	 * @return PatientState that has an id that matches the passed <code>patientStateId</code>
	 */
	public PatientState getPatientState(Integer patientStateId) {
		for (PatientState s : getStates()) {
			if (s.getPatientStateId() != null && s.getPatientStateId().equals(patientStateId)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Attempts to transition the PatientProgram to the passed {@link ProgramWorkflowState} on the
	 * passed {@link Date} by ending the most recent {@link PatientState} in the
	 * {@link PatientProgram} and creating a new one with the passed {@link ProgramWorkflowState}
	 * This will throw an IllegalArgumentException if the transition is invalid
	 * 
	 * @param programWorkflowState - The {@link ProgramWorkflowState} to transition to
	 * @param onDate - The {@link Date} of the transition
	 * @throws IllegalArgumentException
	 */
	public void transitionToState(ProgramWorkflowState programWorkflowState, Date onDate) {
		PatientState lastState = getCurrentState(programWorkflowState.getProgramWorkflow());
		if (lastState != null && onDate == null) {
			throw new IllegalArgumentException("You can't change from a non-null state without giving a change date");
		}
		if (lastState != null && lastState.getEndDate() != null) {
			throw new IllegalArgumentException("You can't change out of a state that has an end date already");
		}
		if (lastState != null && lastState.getStartDate() != null
		        && OpenmrsUtil.compare(lastState.getStartDate(), onDate) > 0) {
			throw new IllegalArgumentException("You can't change out of a state before that state started");
		}
		if (lastState != null
		        && !programWorkflowState.getProgramWorkflow().isLegalTransition(lastState.getState(), programWorkflowState)) {
			throw new IllegalArgumentException("You can't change from state " + lastState.getState() + " to "
			        + programWorkflowState);
		}
		if (lastState != null) {
			lastState.setEndDate(onDate);
		}
		
		PatientState newState = new PatientState();
		newState.setPatientProgram(this);
		newState.setState(programWorkflowState);
		newState.setStartDate(onDate);

		if (newState.getPatientProgram() != null && newState.getPatientProgram().getDateCompleted() != null) {
			newState.setEndDate(newState.getPatientProgram().getDateCompleted());
		}
		
		if (programWorkflowState.getTerminal()) {
			setDateCompleted(onDate);
		}
		
		getStates().add(newState);
	}
	
	/**
	 * Attempts to void the latest {@link PatientState} in the {@link PatientProgram} If earlier
	 * PatientStates exist, it will try to reset the endDate to null so that the next latest state
	 * becomes the current {@link PatientState}
	 * 
	 * @param workflow - The {@link ProgramWorkflow} whose last {@link PatientState} within the
	 *            current {@link PatientProgram} we want to void
	 * @param voidBy - The user who is voiding the {@link PatientState}
	 * @param voidDate - The date to void the {@link PatientState}
	 * @param voidReason - The reason for voiding the {@link PatientState}
	 * <strong>Should</strong> void state with endDate null if startDates equal
	 */
	public void voidLastState(ProgramWorkflow workflow, User voidBy, Date voidDate, String voidReason) {
		List<PatientState> states = statesInWorkflow(workflow, false);
		if (voidDate == null) {
			voidDate = new Date();
		}
		PatientState last = null;
		PatientState nextToLast = null;
		if (!states.isEmpty()) {
			last = states.get(states.size() - 1);
		}
		if (states.size() > 1) {
			nextToLast = states.get(states.size() - 2);
		}
		if (last != null) {
			last.setVoided(true);
			last.setVoidedBy(voidBy);
			last.setDateVoided(voidDate);
			last.setVoidReason(voidReason);
		}
		if (nextToLast != null && nextToLast.getEndDate() != null) {
			nextToLast.setEndDate(nextToLast.getPatientProgram() != null
			        && nextToLast.getPatientProgram().getDateCompleted() != null ? nextToLast.getPatientProgram()
			        .getDateCompleted() : null);
			nextToLast.setDateChanged(voidDate);
			nextToLast.setChangedBy(voidBy);
		}
	}
	
	/**
	 * Returns the current {@link PatientState} for the passed {@link ProgramWorkflow} within this
	 * {@link PatientProgram}.
	 * 
	 * @param programWorkflow The ProgramWorkflow whose current {@link PatientState} we want to
	 *            retrieve
	 * @return PatientState The current {@link PatientState} for the passed {@link ProgramWorkflow}
	 *         within this {@link PatientProgram}
	 */
	public PatientState getCurrentState(ProgramWorkflow programWorkflow) {
		Date now = new Date();
		PatientState currentState = null;
		
		for (PatientState state : getSortedStates()) {
			//states are sorted with the most current state at the last position
			if ((programWorkflow == null || state.getState().getProgramWorkflow().equals(programWorkflow))
			        && state.getActive(now)) {
				currentState = state;
			}
		}
		return currentState;
	}
	
	/**
	 * Returns a Set&lt;PatientState&gt; of all current {@link PatientState}s for the
	 * {@link PatientProgram}
	 * 
	 * @return Set&lt;PatientState&gt; of all current {@link PatientState}s for the {@link PatientProgram}
	 */
	public Set<PatientState> getCurrentStates() {
		Set<PatientState> ret = new HashSet<>();
		Date now = new Date();
		for (PatientState state : getStates()) {
			if (state.getActive(now)) {
				ret.add(state);
			}
		}
		return ret;
	}
	
	/**
	 * Returns a Set&lt;PatientState&gt; of all recent {@link PatientState}s for each workflow of the
	 * {@link PatientProgram}
	 *
	 * @return Set&lt;PatientState&gt; of all recent {@link PatientState}s for the {@link PatientProgram}
	 */
	public Set<PatientState> getMostRecentStateInEachWorkflow() {
		HashMap<ProgramWorkflow,PatientState> map = new HashMap<>();

		for (PatientState state : getSortedStates()) {
			if (!state.isVoided()) {
				ProgramWorkflow workflow = state.getState().getProgramWorkflow();
				map.put(workflow,state);
			}
		}

		Set<PatientState> ret = new HashSet<>();
		for (Map.Entry<ProgramWorkflow, PatientState> entry : map.entrySet()) {
			ret.add(entry.getValue());
		}

		return ret;
	}

	/**
	 * Returns a List&lt;PatientState&gt; of all {@link PatientState}s in the passed
	 * {@link ProgramWorkflow} for the {@link PatientProgram}
	 * 
	 * @param programWorkflow - The {@link ProgramWorkflow} to check
	 * @param includeVoided - If true, return voided {@link PatientState}s in the returned
	 *            {@link List}
	 * @return List&lt;PatientState&gt; of all {@link PatientState}s in the passed {@link ProgramWorkflow}
	 *         for the {@link PatientProgram}
	 */
	public List<PatientState> statesInWorkflow(ProgramWorkflow programWorkflow, boolean includeVoided) {
		List<PatientState> ret = new ArrayList<>();
		for (PatientState st : getSortedStates()) {
			if (st.getState().getProgramWorkflow().equals(programWorkflow) && (includeVoided || !st.getVoided())) {
				ret.add(st);
			}
		}
		return ret;
	}
	
	/** @see Object#toString() */
	@Override
	public String toString() {
		return "PatientProgram(id=" + getPatientProgramId() + ", patient=" + getPatient() + ", program=" + getProgram()
		        + ")";
	}
	
	// ******************
	// Property Access
	// ******************
	
	public Concept getOutcome() {
		return outcome;
	}
	
	public void setOutcome(Concept concept) {
		this.outcome = concept;
	}
	
	public Date getDateCompleted() {
		return dateCompleted;
	}
	
	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	
	public Date getDateEnrolled() {
		return dateEnrolled;
	}
	
	public void setDateEnrolled(Date dateEnrolled) {
		this.dateEnrolled = dateEnrolled;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Integer getPatientProgramId() {
		return patientProgramId;
	}
	
	public void setPatientProgramId(Integer patientProgramId) {
		this.patientProgramId = patientProgramId;
	}
	
	public Program getProgram() {
		return program;
	}
	
	public void setProgram(Program program) {
		this.program = program;
	}
	
	public Set<PatientState> getStates() {
		return states;
	}
	
	public void setStates(Set<PatientState> states) {
		this.states = states;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getPatientProgramId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPatientProgramId(id);
	}
	
	/**
	 * @since 1.8
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @since 1.8
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * @return states sorted by {@link PatientState#compareTo(PatientState)}
	 */
	private List<PatientState> getSortedStates() {
		List<PatientState> sortedStates = new ArrayList<>(getStates());
		Collections.sort(sortedStates);
		return sortedStates;
	}

        @Override
        public Set<PatientProgramAttribute> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<PatientProgramAttribute> getActiveAttributes() {
            ArrayList<PatientProgramAttribute> ret = new ArrayList<>();

            if (this.getAttributes() != null) {
                for (PatientProgramAttribute attr : this.getAttributes()) {
                    if (!attr.isVoided()) {
                        ret.add(attr);
                    }
                }
            }

            return ret;
        }

        @Override
        public List<PatientProgramAttribute> getActiveAttributes(CustomValueDescriptor ofType) {
            ArrayList<PatientProgramAttribute> ret = new ArrayList<>();

            if (this.getAttributes() != null) {
                for (PatientProgramAttribute attr : this.getAttributes()) {
                    if (attr.getAttributeType().equals(ofType) && !attr.isVoided()) {
                        ret.add(attr);
                    }
                }
            }

            return ret;
        }

        @Override
        public void addAttribute(PatientProgramAttribute attribute) {
            if (this.getAttributes() == null) {
                this.setAttributes(new LinkedHashSet<>());
            }

            this.getAttributes().add(attribute);
            attribute.setOwner(this);
        }

        public void setAttributes(Set<PatientProgramAttribute> attributes) {
            this.attributes = attributes;
        }

        public void setAttribute(PatientProgramAttribute attribute) {
            if (this.getAttributes() == null) {
                this.addAttribute(attribute);
            } else {
                if (this.getActiveAttributes(attribute.getAttributeType()).size() == 1) {
                    PatientProgramAttribute patientProgramAttribute = this.getActiveAttributes(attribute.getAttributeType()).get(0);
                    if (!patientProgramAttribute.getValue().equals(attribute.getValue())) {
                        if (patientProgramAttribute.getId() != null) {
                            patientProgramAttribute.setVoided(Boolean.TRUE);
                        } else {
                            this.getAttributes().remove(patientProgramAttribute);
                        }

                        this.getAttributes().add(attribute);
                        attribute.setOwner(this);
                    }
                } else {
                    for (PatientProgramAttribute existing : this.getActiveAttributes(attribute.getAttributeType())) {
                        if (existing.getAttributeType().equals(attribute.getAttributeType())) {
                            if (existing.getId() != null) {
                                existing.setVoided(Boolean.TRUE);
                            } else {
                                this.getAttributes().remove(existing);
                            }
                        }
                    }

                    this.getAttributes().add(attribute);
                    attribute.setOwner(this);
                }
            }
        }
}
