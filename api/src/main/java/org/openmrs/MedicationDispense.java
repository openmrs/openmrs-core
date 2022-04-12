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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * The MedicationDispense class records detailed information about the provision of a supply of a medication 
 * with the intention that it is subsequently consumed by a patient (usually in response to a prescription).
 * 
 * @see <a href="https://www.hl7.org/fhir/medicationdispense.html">
 *     		https://www.hl7.org/fhir/medicationdispense.html
 *     	</a>
 * @since 2.6
 */
@Entity
@Table(name = "medication_dispense")
public class MedicationDispense extends BaseFormRecordableOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "medication_dispense_id")
	private Integer medicationDispenseId;

	/**
	 * FHIR:subject
	 * Patient for whom the medication is intended
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	/**
	 * FHIR:context
	 * Encounter when the dispensing event occurred
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;

	/**
	 * FHIR:medication.medicationCodeableConcept
	 * Corresponds to drugOrder.concept
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "concept")
	private Concept concept;
	
	/**
	 * FHIR:medication.reference(Medication)
	 * Corresponds to drugOrder.drug
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "drug_id")
	private Drug drug;

	/**
	 * FHIR:location
	 * Where the dispensed event occurred
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "location_id")
	private Location location;

	/**
	 * FHIR:performer.actor with null for performer.function.
	 * Per <a href="https://www.hl7.org/fhir/medicationdispense-definitions.html#MedicationDispense.performer">
	 *     	https://www.hl7.org/fhir/medicationdispense-definitions.html#MedicationDispense.performer
	 *     </a>specification, It should be assumed that the actor is the dispenser of the medication
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "dispenser")
	private Provider dispenser;

	/**
	 * FHIR:authorizingPrescription
	 * The drug order that led to this dispensing event; 
	 * note that authorizing prescription maps to a "MedicationRequest" FHIR resource
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "drug_order_id")
	private DrugOrder drugOrder;

	/**
	 * FHIR:status
	 * @see <a href="https://www.hl7.org/fhir/valueset-medicationdispense-status.html">
	 *     		https://www.hl7.org/fhir/valueset-medicationdispense-status.html
	 *     	</a>
	 * i.e. preparation, in-progress, cancelled, on-hold, completed, entered-in-error, stopped, declined, unknown
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "status")
	private Concept status;

	/**
	 * FHIR:statusReason.statusReasonCodeableConcept
	 * @see <a href="https://www.hl7.org/fhir/valueset-medicationdispense-status-reason.html">
	 *     		https://www.hl7.org/fhir/valueset-medicationdispense-status-reason.html
	 *     	</a>
	 * i.e "Stock Out"
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "status_reason")
	private Concept statusReason;

	/**
	 * FHIR:type.codeableConcept
	 * @see <a href="https://www.hl7.org/fhir/v3/ActPharmacySupplyType/vs.html">
	 *     		https://www.hl7.org/fhir/v3/ActPharmacySupplyType/vs.html
	 *     	</a> for potential example concepts
	 * i.e. "Refill" and "Partial Fill"
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "type")
	private Concept type;

	/**
	 * FHIR:quantity.value
	 * Relates to drugOrder.quantity
	 */
	@Column(name = "quantity")
	private Double quantity;

	/**
	 * FHIR:quantity.unit and/or quanity.code
	 * Relates to drugOrder.quantityUnits
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "quantity_units")
	private Concept quantityUnits;

	/**
	 * FHIR:dosageInstructions.doseAndRate.dose.doseQuantity
	 * Relates to drugOrder.dose
	 */
	@Column(name = "dose")
	private Double dose;

	/**
	 * FHIR:dosageInstructions.doseAndRate.dose.quantity.unit and/or code
	 * Relates to drugOrder.doseUnits
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "dose_units")
	private Concept doseUnits;

	/**
	 * FHIR:dosageInstructions.route
	 * Relates to drugOrder.route
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "route")
	private Concept route;

	/**
	 * FHIR:DosageInstructions.timing.repeat.frequency+period+periodUnit
	 * @see <a href="https://build.fhir.org/datatypes.html#Timing">https://build.fhir.org/datatypes.html#Timing</a>
	 * Note that we will continue to map this as a single "frequency" concept, although it doesn't map well to FHIR, 
	 * to make consistent with DrugOrder in OpenMRS
	 * Relates to drugOrder.frequency
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "frequency")
	private OrderFrequency frequency;

	/**
	 * FHIR:DosageInstructions.AsNeeded.asNeededBoolean
	 * Relates to drugOrder.asNeeded
	 */
	@Column(name = "as_needed")
	private Boolean asNeeded;

	/**
	 * FHIR:DosageInstructions.patientInstructions
	 * Relates to drugOrder.dosingInstructions
	 */
	@Column(name = "dosing_instructions", length=65535)
	private String dosingInstructions;

	/**
	 * FHIR:whenPrepared
	 * From FHIR: "When product was packaged and reviewed"
	 */
	@Column(name = "date_prepared")
	private Date datePrepared;

	/**
	 * FHIR:whenHandedOver
	 * From FHIR: "When product was given out"
	 */
	@Column(name = "date_handed_over")
	private Date dateHandedOver;

	/**
	 * FHIR:substitution.wasSubstituted
	 * True/false whether a substitution was made during this dispense event
	 */
	@Column(name = "was_substituted")
	private Boolean wasSubstituted;

	/**
	 * FHIR:substitution.type
	 * @see <a href="https://www.hl7.org/fhir/v3/ActSubstanceAdminSubstitutionCode/vs.html">
	 *     		https://www.hl7.org/fhir/v3/ActSubstanceAdminSubstitutionCode/vs.html
	 *      </a>
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "substitution_type")
	private Concept substitutionType;

	/**
	 * FHIR:substitution.reason
	 * @see <a href="https://www.hl7.org/fhir/v3/SubstanceAdminSubstitutionReason/vs.html">
	 *     		https://www.hl7.org/fhir/v3/SubstanceAdminSubstitutionReason/vs.html
	 *      </a>
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "substitution_reason")
	private Concept substitutionReason;

	
	public MedicationDispense() {
	}

	/**
	 * @see BaseOpenmrsObject#getId() 
	 */
	@Override
	public Integer getId() {
		return getMedicationDispenseId();
	}

	/**
	 * @see BaseOpenmrsObject#setId(Integer)
	 */
	@Override
	public void setId(Integer id) {
		setMedicationDispenseId(id);
	}

	public Integer getMedicationDispenseId() {
		return medicationDispenseId;
	}

	public void setMedicationDispenseId(Integer medicationDispenseId) {
		this.medicationDispenseId = medicationDispenseId;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Encounter getEncounter() {
		return encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public Drug getDrug() {
		return drug;
	}

	public void setDrug(Drug drug) {
		this.drug = drug;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Provider getDispenser() {
		return dispenser;
	}

	public void setDispenser(Provider dispenser) {
		this.dispenser = dispenser;
	}

	public DrugOrder getDrugOrder() {
		return drugOrder;
	}

	public void setDrugOrder(DrugOrder drugOrder) {
		this.drugOrder = drugOrder;
	}

	public Concept getStatus() {
		return status;
	}

	public void setStatus(Concept status) {
		this.status = status;
	}

	public Concept getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(Concept statusReason) {
		this.statusReason = statusReason;
	}

	public Concept getType() {
		return type;
	}

	public void setType(Concept type) {
		this.type = type;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Concept getQuantityUnits() {
		return quantityUnits;
	}

	public void setQuantityUnits(Concept quantityUnits) {
		this.quantityUnits = quantityUnits;
	}

	public Double getDose() {
		return dose;
	}

	public void setDose(Double dose) {
		this.dose = dose;
	}

	public Concept getDoseUnits() {
		return doseUnits;
	}

	public void setDoseUnits(Concept doseUnits) {
		this.doseUnits = doseUnits;
	}

	public Concept getRoute() {
		return route;
	}

	public void setRoute(Concept route) {
		this.route = route;
	}

	public OrderFrequency getFrequency() {
		return frequency;
	}

	public void setFrequency(OrderFrequency frequency) {
		this.frequency = frequency;
	}

	public Boolean getAsNeeded() {
		return asNeeded;
	}

	public void setAsNeeded(Boolean asNeeded) {
		this.asNeeded = asNeeded;
	}

	public String getDosingInstructions() {
		return dosingInstructions;
	}

	public void setDosingInstructions(String dosingInstructions) {
		this.dosingInstructions = dosingInstructions;
	}

	public Date getDatePrepared() {
		return datePrepared;
	}

	public void setDatePrepared(Date datePrepared) {
		this.datePrepared = datePrepared;
	}

	public Date getDateHandedOver() {
		return dateHandedOver;
	}

	public void setDateHandedOver(Date dateHandedOver) {
		this.dateHandedOver = dateHandedOver;
	}

	public Boolean getWasSubstituted() {
		return wasSubstituted;
	}

	public void setWasSubstituted(Boolean wasSubstituted) {
		this.wasSubstituted = wasSubstituted;
	}

	public Concept getSubstitutionType() {
		return substitutionType;
	}

	public void setSubstitutionType(Concept substitutionType) {
		this.substitutionType = substitutionType;
	}

	public Concept getSubstitutionReason() {
		return substitutionReason;
	}

	public void setSubstitutionReason(Concept substitutionReason) {
		this.substitutionReason = substitutionReason;
	}
}
