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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.db.hibernate.search.bridge.LocaleFieldBridge;

/**
 * ConceptName is the real world term used to express a Concept within the idiom of a particular
 * locale.
 */
@Entity
@Table(name = "concept_name")
@BatchSize(size = 25)
@Indexed
@AnalyzerDef(
	name = "ConceptNameAnalyzer", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
        @TokenFilterDef(factory = StandardFilterFactory.class), 
		@TokenFilterDef(factory = LowerCaseFilterFactory.class), 
		@TokenFilterDef(factory = ASCIIFoldingFilterFactory.class)
	})
@Analyzer(definition = "ConceptNameAnalyzer")
@AttributeOverrides(value = {
	@AttributeOverride(
		name = "uuid",
		column = @Column(name = "uuid", length = 38, unique = true)
	)
})
@Audited
public class ConceptName extends BaseOpenmrsObject implements Auditable, Voidable, java.io.Serializable {
	
	public static final long serialVersionUID = 2L;
	
	@Id
	@Column(name = "concept_name_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@DocumentId
	private Integer conceptNameId;
	@ManyToOne
	@JoinColumn(name = "concept_id",nullable = false)
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Concept concept;
	
	@Field
	@Column(name = "name",nullable = false)
	private String name;
	
	@Field(analyze = Analyze.NO)
	@FieldBridge(impl = LocaleFieldBridge.class)
	@Column(name = "locale", nullable = false, length = 50)
	// ABK: upgraded from a plain string to a full locale object
	private Locale locale; 
	
	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private User creator;
	
	@Column(name="date_created", nullable = false)
	private Date dateCreated;
	
	@Field
	@Column(name="voided", nullable = false, length = 1)
	private Boolean voided = false;
	
	@ManyToOne
	@JoinColumn(name = "voided_by")
	private User voidedBy;
	
	@Column(name = "date_voided", length = 19)
	private Date dateVoided;
	
	@Column(name = "void_reason")
	private String voidReason;
	
	@ManyToMany
	@JoinTable(
		name = "concept_name_tag_map",
		joinColumns = @JoinColumn(name = "concept_name_id"),
		inverseJoinColumns = @JoinColumn(name = "concept_name_tag_id")
	)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Collection<ConceptNameTag> tags;
	
	@Field
	@Enumerated(EnumType.STRING)
	@Column(name = "concept_name_type", length = 50)
	@Type(type = "org.hibernate.type.EnumType", parameters = {
		@Parameter(name = "enumClass", value = "org.openmrs.api.ConceptNameType"),
		@Parameter(name = "useNamed", value = "true")
	})
	private ConceptNameType conceptNameType;
	
	@Field
	@Column(name = "locale_preferred", nullable = false, length = 1)
	private Boolean localePreferred = false;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed", length = 19)
	private Date dateChanged;
	
	// Constructors
	
	/** default constructor */
	public ConceptName() {
	}
	
	/**
	 * Convenience constructor to create a ConceptName object by primary key
	 *
	 * @param conceptNameId
	 */
	public ConceptName(Integer conceptNameId) {
		this.conceptNameId = conceptNameId;
	}
	
	public ConceptName(String name, Locale locale) {
		setName(name);
		setLocale(locale);
	}
	
	/**
	 * @return Returns the conceptId.
	 */
	public Integer getConceptNameId() {
		return conceptNameId;
	}
	
	/**
	 * @param conceptNameId The conceptId to set.
	 */
	public void setConceptNameId(Integer conceptNameId) {
		this.conceptNameId = conceptNameId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (name != null && StringUtils.isBlank(name) && StringUtils.isNotBlank(this.name)
		        && this.getConceptNameType().equals(ConceptNameType.SHORT)) {
			this.setVoided(true);
		} else {
			this.name = name;
		}
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return Returns the creator.
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * Returns whether the ConceptName has been voided.
	 *
	 * @return true if the ConceptName has been voided, false otherwise.
	 * 
	 * @deprecated as of 2.0, use {@link #getVoided()}
	 */
	@Override
	@Deprecated
	@JsonIgnore
	public Boolean isVoided() {
		return getVoided();
	}
	
	/**
	 * Returns whether the ConceptName has been voided.
	 *
	 * @return true if the ConceptName has been voided, false otherwise.
	 */
	@Override
	public Boolean getVoided() {
		return voided;
	}
	
	/**
	 * Sets the voided status of this ConceptName.
	 *
	 * @param voided the voided status to set.
	 */
	@Override
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * Returns the User who voided this ConceptName.
	 *
	 * @return the User who voided this ConceptName, or null if not set
	 */
	@Override
	public User getVoidedBy() {
		return voidedBy;
	}
	
	/**
	 * Sets the User who voided this ConceptName.
	 *
	 * @param voidedBy the user who voided this ConceptName.
	 */
	@Override
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	/**
	 * Returns the Date this ConceptName was voided.
	 *
	 * @return the Date this ConceptName was voided.
	 */
	@Override
	public Date getDateVoided() {
		return dateVoided;
	}
	
	/**
	 * Sets the Data this ConceptName was voided.
	 *
	 * @param dateVoided the date the ConceptName was voided.
	 */
	@Override
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * Returns the reason this ConceptName was voided.
	 *
	 * @return the reason this ConceptName was voided
	 */
	@Override
	public String getVoidReason() {
		return voidReason;
	}
	
	/**
	 * Sets the reason this ConceptName was voided.
	 *
	 * @param voidReason the reason this ConceptName was voided
	 */
	@Override
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * Returns the tags which have been attached to this ConceptName.
	 *
	 * @return the tags.
	 */
	public Collection<ConceptNameTag> getTags() {
		return tags;
	}
	
	/**
	 * Set the tags which are attached to this ConceptName.
	 *
	 * @see Concept#setPreferredName(ConceptName)
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 * @see Concept#setShortName(ConceptName)
	 * @param tags the tags to set.
	 */
	public void setTags(Collection<ConceptNameTag> tags) {
		this.tags = tags;
	}
	
	/**
	 * @return the conceptNameType
	 */
	public ConceptNameType getConceptNameType() {
		return this.conceptNameType;
	}
	
	/**
	 * @param conceptNameType the conceptNameType to set
	 */
	public void setConceptNameType(ConceptNameType conceptNameType) {
		this.conceptNameType = conceptNameType;
	}
	
	/**
	 * Getter for localePreferred
	 *
	 * @return localPreferred
	 * 
	 * @deprecated as of 2.0, use {@link #getLocalePreferred()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isLocalePreferred() {
		return getLocalePreferred();
	}
	
	/**
	 *
	 * @return true if it is the localePreferred name otherwise false
	 */
	public Boolean getLocalePreferred() {
		return localePreferred;
	}
	
	/**
	 * @param localePreferred the localePreferred to set
	 */
	public void setLocalePreferred(Boolean localePreferred) {
		this.localePreferred = localePreferred;
	}
	
	/**
	 * Adds a tag to the concept name. If the tag is new (has no existing occurrences) a new
	 * ConceptNameTag will be created with a blank description.
	 *
	 * @see Concept#setPreferredName(ConceptName)
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 * @see Concept#setShortName(ConceptName)
	 * @param tag human-readable text string for the tag
	 */
	public void addTag(String tag) {
		addTag(tag, "");
	}
	
	/**
	 * Adds a tag to the concept name. If the tag is new (has no existing occurrences) a new
	 * ConceptNameTag will be created with the given description.
	 *
	 * @see Concept#setPreferredName(ConceptName)
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 * @see Concept#setShortName(ConceptName)
	 * @param tag human-readable text string for the tag
	 * @param description description of the tag's purpose
	 */
	public void addTag(String tag, String description) {
		ConceptNameTag nameTag = new ConceptNameTag(tag, description);
		addTag(nameTag);
	}
	
	/**
	 * Attaches a tag to the concept name.
	 *
	 * @see Concept#setPreferredName(ConceptName)
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 * @see Concept#setShortName(ConceptName)
	 * @param tag the tag to add
	 */
	public void addTag(ConceptNameTag tag) {
		if (tags == null) {
			tags = new HashSet<>();
		}
		
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
	}
	
	/**
	 * Removes a tag from the concept name.
	 *
	 * @see Concept#setPreferredName(ConceptName)
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 * @see Concept#setShortName(ConceptName)
	 * @param tag the tag to remove
	 */
	public void removeTag(ConceptNameTag tag) {
		if (tags.contains(tag)) {
			tags.remove(tag);
		}
	}
	
	/**
	 * Checks whether the name has a particular tag.
	 *
	 * @see #isPreferred()
	 * @see #isFullySpecifiedName()
	 * @see #isIndexTerm()
	 * @see #isSynonym()
	 * @see #isShort()
	 * @param tagToFind the tag for which to check
	 * @return true if the tags include the specified tag, false otherwise
	 */
	public Boolean hasTag(ConceptNameTag tagToFind) {
		return hasTag(tagToFind.getTag());
	}
	
	/**
	 * Checks whether the name has a particular tag.
	 *
	 * @see #isPreferred()
	 * @see #isFullySpecifiedName()
	 * @see #isIndexTerm()
	 * @see #isSynonym()
	 * @see #isShort()
	 * @param tagToFind the string of the tag for which to check
	 * @return true if the tags include the specified tag, false otherwise
	 */
	public Boolean hasTag(String tagToFind) {
		boolean foundTag = false;
		if (tags != null) {
			for (ConceptNameTag nameTag : getTags()) {
				if (nameTag.getTag().equals(tagToFind)) {
					foundTag = true;
					break;
				}
			}
		}
		return foundTag;
	}
	
	/**
	 * Checks whether the name is explicitly marked as preferred in a locale with a matching
	 * language. E.g 'en_US' and 'en_UK' for language en
	 *
	 * @see #isPreferredForLocale(Locale)
	 * @param language ISO 639 2-letter code for a language
	 * @return true if the name is preferred in a locale with a matching language code, otherwise
	 *         false
	 */
	public Boolean isPreferredInLanguage(String language) {
		return !StringUtils.isBlank(language) && this.locale != null && isPreferred()
				&& this.locale.getLanguage().equals(language);

	}
	
	/**
	 * Checks whether the name is explicitly marked as preferred in a locale with a matching country
	 * code E.g 'fr_RW' and 'en_RW' for country RW
	 *
	 * @see #isPreferredForLocale(Locale)
	 * @param country ISO 3166 2-letter code for a country
	 * @return true if the name is preferred in a locale with a matching country code, otherwise
	 *         false
	 */
	public Boolean isPreferredInCountry(String country) {
		return !StringUtils.isBlank(country) && this.locale != null && isPreferred()
				&& this.locale.getCountry().equals(country);

	}
	
	/**
	 * Checks whether the name is explicitly marked as preferred for any locale. Note that this
	 * method is different from {@link #isPreferredForLocale(Locale)} in that it checks if the given
	 * name is marked as preferred irrespective of the locale in which it is preferred.
	 *
	 * @see #isPreferredForLocale(Locale)
	 */
	public Boolean isPreferred() {
		return getLocalePreferred();
	}
	
	/**
	 * Checks whether the name is explicitly marked as preferred for the given locale
	 *
	 * @param locale the locale in which the name is preferred
	 * @return true if the name is marked as preferred for the given locale otherwise false.
	 */
	public Boolean isPreferredForLocale(Locale locale) {
		return getLocalePreferred() && this.locale.equals(locale);
	}
	
	/**
	 * Checks whether the concept name is explicitly marked as fully specified
	 *
	 * @return true if the name is marked as 'fully specified' otherwise false
	 * @since Version 1.7
	 */
	public Boolean isFullySpecifiedName() {
		return ConceptNameType.FULLY_SPECIFIED.equals(getConceptNameType());
	}
	
	/**
	 * Convenience method for determining whether this is a short name.
	 *
	 * @return true if the name is marked as a short name, otherwise false
	 */
	public Boolean isShort() {
		return ConceptNameType.SHORT.equals(getConceptNameType());
	}
	
	/**
	 * Convenience method for checking whether this is an index Term.
	 *
	 * @return true if the name is marked as an index term, otherwise false
	 * @since Version 1.7
	 */
	public Boolean isIndexTerm() {
		return ConceptNameType.INDEX_TERM.equals(getConceptNameType());
	}
	
	/**
	 * Convenience method for determining whether this is an index Term for a given locale.
	 *
	 * @param locale The locale in which this concept name should belong as an index term
	 * @return true if the name is marked as an index term, otherwise false
	 */
	public Boolean isIndexTermInLocale(Locale locale) {
		return getConceptNameType() != null && getConceptNameType().equals(ConceptNameType.INDEX_TERM)
		        && locale.equals(getLocale());
	}
	
	/**
	 * Convenience method for determining whether this is a synonym in a given locale.
	 *
	 * @param locale The locale in which this synonym should belong
	 * @return true if the concept name is marked as a synonym in the given locale, otherwise false
	 */
	public Boolean isSynonymInLocale(Locale locale) {
		return getConceptNameType() == null && locale.equals(getLocale());
	}
	
	/**
	 * Convenience method for checking whether this is a a synonym.
	 *
	 * @return true if the name is tagged as a synonym, false otherwise
	 * @since Version 1.7
	 */
	public Boolean isSynonym() {
		return getConceptNameType() == null;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (this.name == null) {
			return "ConceptNameId: " + this.conceptNameId;
		}
		
		return this.name;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptNameId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptNameId(id);
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The user that changed this object
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the date this object was changed
	 */
	@Override
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The date this object was changed
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
}
