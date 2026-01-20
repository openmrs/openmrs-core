/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
