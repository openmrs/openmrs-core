package org.openmrs.programWorkflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

/*
 *  <bean id="patientProgramSupport" class="org.openmrs.programWorkflow.PatientProgramSupport">
		<property name="exitReasonToStateMappings">
			<props>
				<prop key="DIED">DIED IN TREATMENT</prop>
				<prop key="TRANSFERRED OUT">TRANSFERRED OUT</prop>
				<prop key="DEFAULTED">DEFAULTED</prop>
			</props>
		</property>
    </bean>

 */


public class PatientProgramSupport {

	private static PatientProgramSupport singleton;
	
	private static Log log = LogFactory.getLog(PatientProgramSupport.class);
	
	private Map<String,List<String>> exitReasonToStateMappings;
	
	public PatientProgramSupport() {
		if (singleton == null)
			singleton = this;
	}
	
	public static PatientProgramSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else
			return singleton;
	}

	/**
	 * @return Returns the exitReasonToStateMappings.
	 */
	public Map<String, List<String>> getExitReasonToStateMappings() {
		return exitReasonToStateMappings;
	}

	/**
	 * @param exitReasonToStateMappings The exitReasonToStateMappings to set.
	 */
	public void setExitReasonToStateMappings(
			Map<String, List<String>> exitReasonToStateMappings) {
		this.exitReasonToStateMappings = exitReasonToStateMappings;
	}
	
	public Set<Concept> getStatesByReason(Concept reason) {
		Set<Concept> ret = null;
		
		if ( reason != null ) {
			for ( Map.Entry<String,List<String>> e : exitReasonToStateMappings.entrySet() ) {
				String key = e.getKey();

				Concept c = Context.getConceptService().getConceptByIdOrName(key);
				if ( c != null ) {
					if ( c.equals(reason) ) {
						List<String> vals = e.getValue();
						if ( vals != null ) {
							for ( String val : vals ) {
								Concept state = Context.getConceptService().getConceptByIdOrName(val);
								if ( state != null ) {
									if ( state.getConceptClass().getName().equals("State") ) {
										if ( ret == null ) ret = new HashSet<Concept>();
										ret.add(state);
									} else {
										log.debug("State " + val + " is not actually a state. It is ConceptClass " + state.getConceptClass().getName());
									}
								} else {
									log.debug("State " + val + " did not actually map to a valid concept");
								}
							}
						}
					}
				} else {
					log.debug("Reason " + key + " did not map to a valid concept");
				}
			}
		}
		
		return ret;
	}
}
