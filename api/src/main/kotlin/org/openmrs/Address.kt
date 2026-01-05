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

/**
 * Defines the standard fields for an Address in OpenMRS.
 *
 * @since 1.7.2
 */
interface Address {
    var address1: String?
    var address2: String?
    var address3: String?
    var address4: String?
    var address5: String?
    var address6: String?

    /** @since 2.0 */
    var address7: String?
    /** @since 2.0 */
    var address8: String?
    /** @since 2.0 */
    var address9: String?
    /** @since 2.0 */
    var address10: String?
    /** @since 2.0 */
    var address11: String?
    /** @since 2.0 */
    var address12: String?
    /** @since 2.0 */
    var address13: String?
    /** @since 2.0 */
    var address14: String?
    /** @since 2.0 */
    var address15: String?

    var cityVillage: String?
    var stateProvince: String?
    var countyDistrict: String?
    var postalCode: String?
    var country: String?
    var latitude: String?
    var longitude: String?
}
