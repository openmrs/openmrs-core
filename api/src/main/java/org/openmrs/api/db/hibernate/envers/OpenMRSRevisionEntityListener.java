/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.api.db.hibernate.envers;

import org.hibernate.envers.RevisionListener;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;

import java.util.Date;

public class OpenMRSRevisionEntityListener implements RevisionListener {
	@Override
	public void newRevision(Object o) {
		OpenMRSRevisionEntity customRevisionEntity = (OpenMRSRevisionEntity) o;
		if(Context.getUserContext().isAuthenticated()) {
			customRevisionEntity.setChangedBy(Context.getUserContext().getAuthenticatedUser().getUserId());
		}
		else if(Daemon.getDaemonThreadUser() != null) {
			customRevisionEntity.setChangedBy(Daemon.getDaemonThreadUser().getUserId());
		}
		customRevisionEntity.setChangedOn(new Date());
	}
}
