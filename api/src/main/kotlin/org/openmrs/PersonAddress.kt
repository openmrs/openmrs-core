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

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.defaultString
import org.apache.commons.lang3.builder.EqualsBuilder
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.openmrs.util.OpenmrsUtil
import org.openmrs.util.compareWithNullAsLatest
import java.io.Serializable
import java.util.*

/**
 * This class is the representation of a person's address. This class is many-to-one to the Person
 * class, so a Person/Patient/User can have zero to n addresses
 */
@Entity
@Table(name = "person_address")
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class PersonAddress() : BaseChangeableOpenmrsData(), Serializable, Cloneable, Comparable<PersonAddress>, Address {

    @Id
    @Column(name = "person_address_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var personAddressId: Int? = null

    @ManyToOne
    @JoinColumn(name = "person_id")
    var person: Person? = null

    @Column(name = "preferred", length = 1, nullable = false)
    private var preferred: Boolean? = false

    @Column(name = "address1")
    override var address1: String? = null

    @Column(name = "address2")
    override var address2: String? = null

    @Column(name = "address3")
    override var address3: String? = null

    @Column(name = "address4")
    override var address4: String? = null

    @Column(name = "address5")
    override var address5: String? = null

    @Column(name = "address6")
    override var address6: String? = null

    @Column(name = "address7")
    override var address7: String? = null

    @Column(name = "address8")
    override var address8: String? = null

    @Column(name = "address9")
    override var address9: String? = null

    @Column(name = "address10")
    override var address10: String? = null

    @Column(name = "address11")
    override var address11: String? = null

    @Column(name = "address12")
    override var address12: String? = null

    @Column(name = "address13")
    override var address13: String? = null

    @Column(name = "address14")
    override var address14: String? = null

    @Column(name = "address15")
    override var address15: String? = null

    @Column(name = "city_village")
    override var cityVillage: String? = null

    @Column(name = "county_district")
    override var countyDistrict: String? = null

    @Column(name = "state_province")
    override var stateProvince: String? = null

    @Column(name = "country")
    override var country: String? = null

    @Column(name = "postal_code", length = 50)
    override var postalCode: String? = null

    @Column(name = "latitude", length = 50)
    override var latitude: String? = null

    @Column(name = "longitude", length = 50)
    override var longitude: String? = null

    @Column(name = "start_date", length = 19)
    var startDate: Date? = null

    @Column(name = "end_date", length = 19)
    var endDate: Date? = null

    constructor(personAddressId: Int?) : this() {
        this.personAddressId = personAddressId
    }

    override fun toString(): String =
        "a1:$address1, a2:$address2, cv:$cityVillage, sp:$stateProvince, " +
        "c:$country, cd:$countyDistrict, nc:$address3, pc:$postalCode, " +
        "lat:$latitude, long:$longitude"

    /**
     * Compares this PersonAddress object to the given otherAddress. This method differs from
     * [equals] in that this method compares the inner fields of each address for
     * equality. Note: Null/empty fields on `otherAddress` /will not/ cause a false value
     * to be returned
     *
     * @param otherAddress PersonAddress with which to compare
     * @return boolean true/false whether or not they are the same addresses
     */
    fun equalsContent(otherAddress: PersonAddress): Boolean {
        return EqualsBuilder()
            .append(defaultString(otherAddress.address1), defaultString(address1))
            .append(defaultString(otherAddress.address2), defaultString(address2))
            .append(defaultString(otherAddress.address3), defaultString(address3))
            .append(defaultString(otherAddress.address4), defaultString(address4))
            .append(defaultString(otherAddress.address5), defaultString(address5))
            .append(defaultString(otherAddress.address6), defaultString(address6))
            .append(defaultString(otherAddress.address7), defaultString(address7))
            .append(defaultString(otherAddress.address8), defaultString(address8))
            .append(defaultString(otherAddress.address9), defaultString(address9))
            .append(defaultString(otherAddress.address10), defaultString(address10))
            .append(defaultString(otherAddress.address11), defaultString(address11))
            .append(defaultString(otherAddress.address12), defaultString(address12))
            .append(defaultString(otherAddress.address13), defaultString(address13))
            .append(defaultString(otherAddress.address14), defaultString(address14))
            .append(defaultString(otherAddress.address15), defaultString(address15))
            .append(defaultString(otherAddress.cityVillage), defaultString(cityVillage))
            .append(defaultString(otherAddress.countyDistrict), defaultString(countyDistrict))
            .append(defaultString(otherAddress.stateProvince), defaultString(stateProvince))
            .append(defaultString(otherAddress.country), defaultString(country))
            .append(defaultString(otherAddress.postalCode), defaultString(postalCode))
            .append(defaultString(otherAddress.latitude), defaultString(latitude))
            .append(defaultString(otherAddress.longitude), defaultString(longitude))
            .append(otherAddress.startDate, startDate)
            .append(otherAddress.endDate, endDate)
            .isEquals
    }

    /**
     * bitwise copy of the personAddress object. NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT. The
     * PersonAddress.person object in this object AND the cloned object will point at the same
     * person
     *
     * @return New PersonAddress object
     */
    public override fun clone(): Any {
        return try {
            super.clone()
        } catch (e: CloneNotSupportedException) {
            throw InternalError("PersonAddress should be cloneable")
        }
    }

    fun getPreferred(): Boolean = preferred ?: false

    fun setPreferred(preferred: Boolean?) {
        this.preferred = preferred
    }

    @Deprecated("as of 2.0, use getPreferred()")
    @JsonIgnore
    fun isPreferred(): Boolean = getPreferred()

    /**
     * Convenience method to test whether any of the fields in this address are set
     *
     * @return whether any of the address fields (address1, address2, cityVillage, stateProvince,
     *         country, countyDistrict, neighborhoodCell, postalCode, latitude, longitude, etc) are
     *         whitespace, empty ("") or null.
     */
    fun isBlank(): Boolean =
        StringUtils.isBlank(address1) && StringUtils.isBlank(address2) &&
        StringUtils.isBlank(address3) && StringUtils.isBlank(address4) &&
        StringUtils.isBlank(address5) && StringUtils.isBlank(address6) &&
        StringUtils.isBlank(cityVillage) && StringUtils.isBlank(stateProvince) &&
        StringUtils.isBlank(country) && StringUtils.isBlank(countyDistrict) &&
        StringUtils.isBlank(postalCode) && StringUtils.isBlank(latitude) &&
        StringUtils.isBlank(longitude)

    /**
     * Returns true if the address' endDate is null
     *
     * @return true or false
     * @since 1.9
     */
    fun isActive(): Boolean = endDate == null

    /**
     * Makes an address inactive by setting its endDate to the current time
     *
     * @since 1.9
     */
    fun deactivate() {
        endDate = Calendar.getInstance().time
    }

    /**
     * Makes an address active by setting its endDate to null
     *
     * @since 1.9
     */
    fun activate() {
        endDate = null
    }

    override fun getId(): Int? = personAddressId

    override fun setId(id: Int?) {
        personAddressId = id
    }

    /**
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    override fun compareTo(other: PersonAddress): Int {
        var retValue = voided!!.compareTo(other.voided!!)
        if (retValue == 0) {
            retValue = other.getPreferred().compareTo(getPreferred())
        }
        if (retValue == 0 && dateCreated != null) {
            retValue = dateCreated.compareWithNullAsLatest(other.dateCreated)
        }
        if (retValue == 0) {
            retValue = OpenmrsUtil.compareWithNullAsGreatest(personAddressId, other.personAddressId)
        }

        // if we've gotten this far, just check all address values. If they are
        // equal, leave the objects at 0. If not, arbitrarily pick retValue=1
        // and return that (they are not equal).
        if (retValue == 0 && !equalsContent(other)) {
            retValue = 1
        }

        return retValue
    }

    companion object {
        private const val serialVersionUID = 343333L
    }
}
