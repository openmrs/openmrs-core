/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.envers

import org.hibernate.envers.RevisionListener
import org.openmrs.api.context.Context
import org.openmrs.api.context.Daemon
import java.util.Date

class OpenmrsRevisionEntityListener : RevisionListener {
    
    override fun newRevision(o: Any) {
        val customRevisionEntity = o as OpenmrsRevisionEntity
        when {
            Context.getUserContext().isAuthenticated -> {
                customRevisionEntity.changedBy = Context.getUserContext().authenticatedUser.userId
            }
            Daemon.getDaemonThreadUser() != null -> {
                customRevisionEntity.changedBy = Daemon.getDaemonThreadUser().userId
            }
        }
        customRevisionEntity.changedOn = Date()
    }
}
