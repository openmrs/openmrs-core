/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import java.io.Serializable

/**
 * Used to store the possible provider roles. A Provider can only have a single role (though a single person
 * could be associated with more than one Provider object).
 * @since 2.8.0
 */
@Entity
@Table(name = "provider_role")
@Audited
class ProviderRole : BaseOpenmrsMetadata(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_role_id")
    var providerRoleId: Int? = null

    override fun getId(): Int? = providerRoleId

    override fun setId(id: Int?) {
        providerRoleId = id
    }

    override fun toString(): String =
        "ProviderRole{providerRoleId=$providerRoleId, name=${name}}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
