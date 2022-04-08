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

import org.openmrs.util.OpenmrsUtil;

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
 * The MedicationDispenseNote class records extra information about the MedicationDispense
 * that could not be conveyed in the other attributes
 *
 * @see <a href="https://www.hl7.org/fhir/medicationdispense-definitions.html#MedicationDispense.note">
 *     		https://www.hl7.org/fhir/medicationdispense-definitions.html#MedicationDispense.note
 *     	</a>
 *
 * @see <a href="https://www.hl7.org/fhir/datatypes.html#Annotation">
 *     		https://www.hl7.org/fhir/datatypes.html#Annotation
 *     	</a>
 *     	
 * @since 2.6
 */
@Entity
@Table(name = "medication_dispense_note")
public class MedicationDispenseNote extends BaseFormRecordableOpenmrsData 
								    implements Comparable<MedicationDispenseNote> {
	
	public static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "medication_dispense_note_id")
	private Integer medicationDispenseNoteId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "medication_dispense_id")
	private MedicationDispense medicationDispense;

	/**
	 * FHIR:note.author
	 * Individual responsible for the note.  Optional - references a provider.
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "provider_id")
	private Provider provider;

	/**
	 * FHIR:note.time
	 * When the note was made.  In FHIR this is optional, here we are making it required.
	 */
	@Column(name="note_datetime", nullable = false)
	private Date noteDatetime;

	/**
	 * FHIR:note.text
	 * The note - text content.  This is required.
	 */
	@Column(name="note_text", nullable = false, length = 65535)
	private String noteText;
	
	public MedicationDispenseNote() {}

	/**
	 * @see BaseOpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getMedicationDispenseNoteId();
	}

	/**
	 * @see BaseOpenmrsObject#setId(Integer)
	 */
	@Override
	public void setId(Integer id) {
		setMedicationDispenseNoteId(id);
	}

	/**
	 * @see Comparable#compareTo(Object)
	 * Notes are ordered first by noteDatetime, next by dateCreated, and finally by medicationDispenseNoteId
	 */
	@Override
	public int compareTo(MedicationDispenseNote that) {
		int ret = OpenmrsUtil.compareWithNullAsLatest(this.getNoteDatetime(), that.getNoteDatetime());
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsLatest(this.getDateCreated(), that.getDateCreated());
		}
		if (ret == 0) {
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getId(), that.getId());
		}
		return ret;
	}

	public Integer getMedicationDispenseNoteId() {
		return medicationDispenseNoteId;
	}

	public void setMedicationDispenseNoteId(Integer medicationDispenseNoteId) {
		this.medicationDispenseNoteId = medicationDispenseNoteId;
	}

	public MedicationDispense getMedicationDispense() {
		return medicationDispense;
	}

	public void setMedicationDispense(MedicationDispense medicationDispense) {
		this.medicationDispense = medicationDispense;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Date getNoteDatetime() {
		return noteDatetime;
	}

	public void setNoteDatetime(Date noteDatetime) {
		this.noteDatetime = noteDatetime;
	}

	public String getNoteText() {
		return noteText;
	}

	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}
}
