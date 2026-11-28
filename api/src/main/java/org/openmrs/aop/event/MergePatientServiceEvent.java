/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop.event;

import org.openmrs.Patient;
import org.openmrs.event.BaseSessionEvent;

import java.util.Set;

/**
 * Published manually by {@link org.openmrs.api.impl.PatientServiceImpl#mergePatients(Patient, Patient)}
 * after a patient merge completes, since {@code mergePatients} does not match the
 * save/create/void/unvoid/retire/unretire/purge naming convention recognised by
 * {@link org.openmrs.aop.OpenmrsServiceEventAdvice}.
 *
 * @since 2.9.0
 */
public class MergePatientServiceEvent extends BaseSessionEvent {
	private static final long serialVersionUID = 1L;

	private Patient winner;

	private Patient loser;

	public MergePatientServiceEvent() {
	}

	public MergePatientServiceEvent(Patient winner, Patient loser) {
		this(winner, loser, Set.of());
	}

	public MergePatientServiceEvent(Patient winner, Patient loser, Set<String> tags) {
		super(tags);
		this.winner = winner;
		this.loser = loser;
	}

	public Patient getWinner() {
		return winner;
	}

	public void setWinner(Patient winner) {
		this.winner = winner;
	}

	public Patient getLoser() {
		return loser;
	}

	public void setLoser(Patient loser) {
		this.loser = loser;
	}
}
