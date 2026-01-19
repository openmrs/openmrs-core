package org.openmrs.api.context;

import java.util.Date;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;

/**
 * Context object used for evaluating concept reference ranges.
 * This allows reference range evaluation to be parameterized
 * by patient, encounter, obs, and date (e.g. for retrospective entries).
 */
public class ConceptReferenceRangeContext {

    private Date date;
    private Patient patient;
    private Encounter encounter;
    private Obs obs;

    public ConceptReferenceRangeContext(Obs obs) {
        this.obs = obs;
        this.encounter = obs.getEncounter();
        this.patient = obs.getPerson() instanceof Patient ? (Patient) obs.getPerson() : null;
        this.date = obs.getObsDatetime();
    }

    public ConceptReferenceRangeContext(Encounter encounter) {
        this.encounter = encounter;
        this.patient = encounter.getPatient();
        this.date = encounter.getEncounterDatetime();
    }

    public ConceptReferenceRangeContext(Patient patient, Date date) {
        this.patient = patient;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public Patient getPatient() {
        return patient;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public Obs getObs() {
        return obs;
    }
}
