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

import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.codehaus.jackson.annotate.JsonIgnore
import org.hibernate.envers.Audited
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue
import org.openmrs.api.context.Context
import java.util.LinkedHashSet
import java.util.Locale

/**
 * Drug
 */
@Indexed
@Audited
@Entity
@Table(name = "drug")
@AttributeOverrides(
    AttributeOverride(name = "name", column = Column(name = "name", length = 255, nullable = true))
)
class Drug() : BaseChangeableOpenmrsMetadata() {
    
    @DocumentId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drug_id")
    var drugId: Int? = null
    
    @Column(name = "combination", nullable = false)
    var combination: Boolean = false
    
    @ManyToOne
    @JoinColumn(name = "dosage_form")
    var dosageForm: Concept? = null
    
    @Column(name = "maximum_daily_dose", length = 22)
    var maximumDailyDose: Double? = null
    
    @Column(name = "minimum_daily_dose", length = 22)
    var minimumDailyDose: Double? = null
    
    @Column(name = "strength", length = 255)
    var strength: String? = null
    
    @ManyToOne
    @JoinColumn(name = "dose_limit_units")
    var doseLimitUnits: Concept? = null
    
    @IndexedEmbedded(includePaths = ["conceptId"], includeEmbeddedObjectId = true)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    @ManyToOne(optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    var concept: Concept? = null
    
    @IndexedEmbedded
    @AssociationInverseSide(
        inversePath = ObjectPath([PropertyValue(propertyName = "drug")])
    )
    @OneToMany(mappedBy = "drug", cascade = [CascadeType.ALL], orphanRemoval = true)
    private var _drugReferenceMaps: MutableSet<DrugReferenceMap>? = null
    
    @OneToMany(mappedBy = "drug", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var ingredients: MutableCollection<DrugIngredient> = LinkedHashSet()
    
    /** constructor with id */
    constructor(drugId: Int?) : this() {
        this.drugId = drugId
    }
    
    /**
     * Gets the entries concept drug name in the form of CONCEPTNAME (Drug: DRUGNAME)
     * 
     * @param locale
     * @return full drug name (with concept name appended)
     */
    fun getFullName(locale: Locale?): String =
        concept?.let { "${name} (${it.getName(locale).name})" } ?: (name ?: "")
    
    /**
     * Gets whether or not this is a combination drug
     *
     * @return Boolean
     * 
     * @deprecated as of 2.0, use [combination]
     */
    @Deprecated("Use combination property instead", ReplaceWith("combination"))
    @JsonIgnore
    fun isCombination(): Boolean = combination
    
    /**
     * @return Returns the drugReferenceMaps.
     * @since 1.10
     */
    fun getDrugReferenceMaps(): MutableSet<DrugReferenceMap> {
        if (_drugReferenceMaps == null) {
            _drugReferenceMaps = LinkedHashSet()
        }
        return _drugReferenceMaps!!
    }
    
    /**
     * @param drugReferenceMaps The drugReferenceMaps to set.
     * @since 1.10
     */
    fun setDrugReferenceMaps(drugReferenceMaps: MutableSet<DrugReferenceMap>?) {
        this._drugReferenceMaps = drugReferenceMaps
    }
    
    /**
     * Add the given DrugReferenceMap object to this drug's list of drug reference mappings. If there is
     * already a corresponding DrugReferenceMap object for this concept, this one will not be added.
     *
     * @param drugReferenceMap
     * @since 1.10
     *
     * Should set drug as the drug to which a mapping is being added
     *
     * Should should not add duplicate drug reference maps
     */
    fun addDrugReferenceMap(drugReferenceMap: DrugReferenceMap?) {
        drugReferenceMap?.let {
            if (!getDrugReferenceMaps().contains(it)) {
                it.drug = this
                if (it.conceptMapType == null) {
                    it.conceptMapType = Context.getConceptService().defaultConceptMapType
                }
                getDrugReferenceMaps().add(it)
            }
        }
    }
    
    override var id: Int?
        get() = drugId
        set(value) {
            drugId = value
        }
    
    /**
     * Convenience method that returns a display name for the drug, defaults to drug.name
     *
     * @return the display name
     * @since 1.8.5, 1.9.4, 1.10
     */
    val displayName: String
        get() = when {
            !name.isNullOrBlank() -> name!!
            concept != null -> concept!!.name.name
            else -> ""
        }
    
    companion object {
        const val serialVersionUID = 285L
    }
}
