/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ObsArchiveHelper;

/**
 * Restores archived observations when an {@link Encounter} is unvoided.
 * <p>
 * After the archiving sweep has moved an encounter's voided obs into {@code obs_archive}, unvoiding
 * the encounter through {@link RequiredDataAdvice} only reaches obs still in the
 * {@code encounter.obs} collection. This handler picks up the archived top-level obs that were
 * voided at the same time as the encounter and passes each to {@link ObsService#unvoidObs(Obs)},
 * which already restores an archived obs together with the children voided with it.
 *
 * @see RequiredDataAdvice
 * @see UnvoidHandler
 * @since 3.0.0
 */
@Handler(supports = Encounter.class)
public class EncounterUnvoidHandler implements UnvoidHandler<Encounter> {

	@Override
	public void handle(Encounter encounter, User originalVoidingUser, Date origParentVoidedDate, String unused) {
		if (encounter.getId() != null) {
			ObsArchiveHelper obsArchiveHelper = Context.getRegisteredComponent("obsArchiveHelper", ObsArchiveHelper.class);
			ObsService obsService = Context.getObsService();

			List<Obs> archivedObs = obsArchiveHelper.getArchivedObsByEncounterId(encounter.getId());
			for (Obs obs : archivedObs) {
				if (obs.getObsGroup() == null && obs.getDateVoided() != null
				        && obs.getDateVoided().equals(origParentVoidedDate) && obs.getVoidedBy() != null
				        && obs.getVoidedBy().equals(originalVoidingUser)) {
					obsService.unvoidObs(obs);
				}
			}
		}
	}
}
