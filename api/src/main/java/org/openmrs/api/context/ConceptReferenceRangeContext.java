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

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;

/**
 * Context used to evaluate concept reference ranges independently of a fully
 * populated Obs.
 *
 * This allows reference range evaluation at times other than Obs persistence
 * (e.g. retrospective entry), while preserving compatibility with existing
 * criteria expressions.
 */
public class ConceptReferenceRangeContext {

    private final Patient patient;
    private final Concept concept;
    private final Date evaluationDate;
    private final Obs obs;

    /**
     * Construct a context from an existing Obs.
     */
    public ConceptReferenceRangeContext(Obs obs) {
        this.obs = obs;
        this.patient = obs.getPerson() instanceof Patient
                ? (Patient) obs.getPerson()
                : null;
        this.concept = obs.getConcept();
        this.evaluationDate = obs.getObsDatetime();
    }

    /**
     * Construct a context from patient, concept, and date.
     */
    public ConceptReferenceRangeContext(Patient patient, Concept concept, Date evaluationDate) {
        this.patient = patient;
        this.concept = concept;
        this.evaluationDate = evaluationDate;
        this.obs = null;
    }

    /**
     * Convenience constructor using an encounter.
     */
    public ConceptReferenceRangeContext(Encounter encounter, Concept concept) {
        this.patient = encounter.getPatient();
        this.concept = concept;
        this.evaluationDate = encounter.getEncounterDatetime();
        this.obs = null;
    }

    public Patient getPatient() {
        return patient;
    }

    public Concept getConcept() {
        return concept;
    }

    public Date getEvaluationDate() {
        return evaluationDate;
    }

    /**
     * Optional Obs, if this context was constructed from one.
     */
    public Obs getObs() {
        return obs;
    }
}
