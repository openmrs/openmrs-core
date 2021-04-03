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

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.openmrs.api.APIException;
import org.openmrs.api.db.hibernate.search.LuceneAnalyzers;
import org.openmrs.layout.name.NameSupport;
import org.openmrs.layout.name.NameTemplate;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * A Person can have zero to n PersonName(s).
 */
@Indexed
public class PersonName extends BaseChangeableOpenmrsData implements java.io.Serializable, Cloneable, Comparable<PersonName> {
	
	public static final long serialVersionUID = 4353L;

	private static final Logger log = LoggerFactory.getLogger(PersonName.class);

	// Fields
	@DocumentId
	private Integer personNameId;

	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Person person;

	private Boolean preferred = false;

	@Fields({
			@Field(name = "givenNameExact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(8f)),
			@Field(name = "givenNameStart", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(4f)),
			@Field(name = "givenNameAnywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER), boost = @Boost(2f)),
			@Field(name = "givenNameSoundex", analyzer =  @Analyzer(definition = LuceneAnalyzers.SOUNDEX_ANALYZER), boost = @Boost(1f))
	})
	private String givenName;
	private String prefix;

	@Fields({
			@Field(name = "middleNameExact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(4f)),
			@Field(name = "middleNameStart", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(2f)),
			@Field(name = "middleNameAnywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER)),
			@Field(name = "middleNameSoundex", analyzer =  @Analyzer(definition = LuceneAnalyzers.SOUNDEX_ANALYZER), boost = @Boost(1f))
	})
	private String middleName;
	
	private String familyNamePrefix;

	@Fields({
			@Field(name = "familyNameExact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(8f)),
			@Field(name = "familyNameStart", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(4f)),
			@Field(name = "familyNameAnywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER), boost = @Boost(2f)),
			@Field(name = "familyNameSoundex", analyzer =  @Analyzer(definition = LuceneAnalyzers.SOUNDEX_ANALYZER), boost = @Boost(1f))
	})
	private String familyName;

	@Fields({
			@Field(name = "familyName2Exact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(4f)),
			@Field(name = "familyName2Start", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(2f)),
			@Field(name = "familyName2Anywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER)),
			@Field(name = "familyName2Soundex", analyzer =  @Analyzer(definition = LuceneAnalyzers.SOUNDEX_ANALYZER), boost = @Boost(1f))
	})
	private String familyName2;
	
	private String familyNameSuffix;
	
	private String degree;
	
	private static String format = OpenmrsConstants.PERSON_NAME_FORMAT_SHORT;
	
	// Constructors
	
	/** default constructor */
	public PersonName() {
	}
	
	/** constructor with id */
	public PersonName(Integer personNameId) {
		this.personNameId = personNameId;
	}
	
	/**
	 * Convenience constructor with the basic requirements
	 *
	 * @param givenName String this person's first name
	 * @param middleName String this person's middle name
	 * @param familyName String this person's last name
	 */
	public PersonName(String givenName, String middleName, String familyName) {
		this.givenName = givenName;
		this.middleName = middleName;
		this.familyName = familyName;
	}
	
	/**
	 * Compares this PersonName object to the given otherName. This method differs from
	 * {@link #equals(Object)} in that this method compares the inner fields of each name for
	 * equality. Note: Null/empty fields on <code>otherName</code> /will not/ cause a false value to
	 * be returned
	 *
	 * @param otherName PersonName with which to compare
	 * @return boolean true/false whether or not they are the same names
	 * <strong>Should</strong> return true if given middle and family name are equal
	 */
	public boolean equalsContent(PersonName otherName) {
		return new EqualsBuilder().append(defaultString(otherName.getPrefix()), defaultString(prefix)).append(
		    defaultString(otherName.getGivenName()), defaultString(givenName)).append(
		    defaultString(otherName.getMiddleName()), defaultString(middleName)).append(
		    defaultString(otherName.getFamilyNamePrefix()), defaultString(familyNamePrefix)).append(
		    defaultString(otherName.getDegree()), defaultString(degree)).append(defaultString(otherName.getFamilyName()),
		    defaultString(familyName)).append(defaultString(otherName.getFamilyName2()), defaultString(familyName2)).append(
		    defaultString(otherName.getFamilyNameSuffix()), defaultString(familyNameSuffix)).isEquals();
	}
	
	/**
	 * Bitwise copy of the personName object. NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT. The
	 * PersonName.person object in this object AND the cloned object will point at the same person
	 *
	 * @return New PersonName object
	 * <strong>Should</strong> copy every property of given personName
	 */
	public static PersonName newInstance(PersonName pn) {
		if (pn == null) {
			throw new IllegalArgumentException();
		}
		PersonName newName = new PersonName(pn.getPersonNameId());
		if (pn.getGivenName() != null) {
			newName.setGivenName(String.valueOf(pn.getGivenName()));
		}
		if (pn.getMiddleName() != null) {
			newName.setMiddleName(String.valueOf(pn.getMiddleName()));
		}
		if (pn.getFamilyName() != null) {
			newName.setFamilyName(String.valueOf(pn.getFamilyName()));
		}
		if (pn.getFamilyName2() != null) {
			newName.setFamilyName2(String.valueOf(pn.getFamilyName2()));
		}
		if (pn.getFamilyNamePrefix() != null) {
			newName.setFamilyNamePrefix(String.valueOf(pn.getFamilyNamePrefix()));
		}
		if (pn.getFamilyNameSuffix() != null) {
			newName.setFamilyNameSuffix(String.valueOf(pn.getFamilyNameSuffix()));
		}
		if (pn.getPrefix() != null) {
			newName.setPrefix(String.valueOf(pn.getPrefix()));
		}
		if (pn.getDegree() != null) {
			newName.setDegree(String.valueOf(pn.getDegree()));
		}
		if (pn.getVoidReason() != null) {
			newName.setVoidReason(String.valueOf(pn.getVoidReason()));
		}
		
		if (pn.getDateChanged() != null) {
			newName.setDateChanged((Date) pn.getDateChanged().clone());
		}
		if (pn.getDateCreated() != null) {
			newName.setDateCreated((Date) pn.getDateCreated().clone());
		}
		if (pn.getDateVoided() != null) {
			newName.setDateVoided((Date) pn.getDateVoided().clone());
		}
		
		if (pn.getPreferred() != null) {
			newName.setPreferred(pn.getPreferred());
		}
		if (pn.getVoided() != null) {
			newName.setVoided(pn.getVoided());
		}
		
		newName.setPerson(pn.getPerson());
		newName.setVoidedBy(pn.getVoidedBy());
		newName.setChangedBy(pn.getChangedBy());
		newName.setCreator(pn.getCreator());
		
		return newName;
	}
	
	/**
	 * @return Returns the degree.
	 */
	public String getDegree() {
		return degree;
	}
	
	/**
	 * @param degree The degree to set.
	 */
	public void setDegree(String degree) {
		this.degree = degree;
	}
	
	/**
	 * @return Returns the familyName.
	 * <strong>Should</strong> return obscured name if obscure_patients is set to true
	 */
	public String getFamilyName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME;
		}
		return familyName;
	}
	
	/**
	 * @param familyName The familyName to set.
	 */
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	/**
	 * @return Returns the familyName2.
	 * <strong>Should</strong> return null if obscure_patients is set to true
	 */
	public String getFamilyName2() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return null;
		}
		return familyName2;
	}
	
	/**
	 * @param familyName2 The familyName2 to set.
	 */
	public void setFamilyName2(String familyName2) {
		this.familyName2 = familyName2;
	}
	
	/**
	 * @return Returns the familyNamePrefix.
	 * <strong>Should</strong> return null if obscure_patients is set to true
	 */
	public String getFamilyNamePrefix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return null;
		}
		return familyNamePrefix;
	}
	
	/**
	 * @param familyNamePrefix The familyNamePrefix to set.
	 */
	public void setFamilyNamePrefix(String familyNamePrefix) {
		this.familyNamePrefix = familyNamePrefix;
	}
	
	/**
	 * @return Returns the familyNameSuffix.
	 * <strong>Should</strong> return null if obscure_patients is set to true
	 */
	public String getFamilyNameSuffix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return null;
		}
		return familyNameSuffix;
	}
	
	/**
	 * @param familyNameSuffix The familyNameSuffix to set.
	 */
	public void setFamilyNameSuffix(String familyNameSuffix) {
		this.familyNameSuffix = familyNameSuffix;
	}
	
	/**
	 * @return Returns the givenName.
	 * <strong>Should</strong> return obscured name if obscure_patients is set to true
	 */
	public String getGivenName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME;
		}
		return givenName;
	}
	
	/**
	 * @param givenName The givenName to set.
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	/**
	 * @return Returns the middleName.
	 * <strong>Should</strong> return obscured name if obscure_patients is set to true
	 */
	public String getMiddleName() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME;
		}
		return middleName;
	}
	
	/**
	 * @param middleName The middleName to set.
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	/**
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return Returns the personNameId.
	 */
	public Integer getPersonNameId() {
		return personNameId;
	}
	
	/**
	 * @param personNameId The personNameId to set.
	 */
	public void setPersonNameId(Integer personNameId) {
		this.personNameId = personNameId;
	}
	
	/**
	 * @return Returns the preferred.
	 *
	 * @deprecated as of 2.0, use {@link #getPreferred()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isPreferred() {
		return getPreferred();
	}
	
	public Boolean getPreferred() {
		if (preferred == null) {
			return Boolean.FALSE;
		}
		return preferred;
	}
	
	/**
	 * @param preferred The preferred to set.
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/**
	 * @return Returns the prefix.
	 * <strong>Should</strong> return null if obscure_patients is set to true
	 */
	public String getPrefix() {
		if (OpenmrsConstants.OBSCURE_PATIENTS) {
			return null;
		}
		return prefix;
	}
	
	/**
	 * @param prefix The prefix to set.
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * Convenience method to get all the names of this PersonName and concatenating them together
	 * with spaces in between. If any part of {@link #getPrefix()}, {@link #getGivenName()},
	 * {@link #getMiddleName()}, etc are null, they are not included in the returned name
	 *
	 * @return all of the parts of this {@link PersonName} joined with spaces
	 * <strong>Should</strong> not put spaces around an empty middle name
	 */
	public String getFullName() {
		NameTemplate nameTemplate = null;
		try {
			nameTemplate = NameSupport.getInstance().getDefaultLayoutTemplate();
		}
		catch (APIException ex) {
			log.warn("No name layout format set");
		}
		
		if (nameTemplate != null) {
			return nameTemplate.format(this);
		}

		List<String> temp = new ArrayList<>();
		if (StringUtils.hasText(getPrefix())) {
			temp.add(getPrefix());
		}
		if (StringUtils.hasText(getGivenName())) {
			temp.add(getGivenName());
		}
		if (StringUtils.hasText(getMiddleName())) {
			temp.add(getMiddleName());
		}
		if (OpenmrsConstants.PERSON_NAME_FORMAT_LONG.equals(PersonName.getFormat())) {
			
			if (StringUtils.hasText(getFamilyNamePrefix())) {
				temp.add(getFamilyNamePrefix());
			}
			if (StringUtils.hasText(getFamilyName())) {
				temp.add(getFamilyName());
			}
			if (StringUtils.hasText(getFamilyName2())) {
				temp.add(getFamilyName2());
			}
			if (StringUtils.hasText(getFamilyNameSuffix())) {
				temp.add(getFamilyNameSuffix());
			}
			if (StringUtils.hasText(getDegree())) {
				temp.add(getDegree());
			}
		} else {
			
			if (StringUtils.hasText(getFamilyName())) {
				temp.add(getFamilyName());
			}
		}
		
		String nameString = StringUtils.collectionToDelimitedString(temp, " ");
		
		return nameString.trim();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//This should not be changed due to extensive usage in UI.
		return getFullName();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getPersonNameId();
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * <strong>Should</strong> return negative if other name is voided
	 * <strong>Should</strong> return negative if this name is preferred
	 * <strong>Should</strong> return negative if other familyName is greater
	 * <strong>Should</strong> return negative if other familyName2 is greater
	 * <strong>Should</strong> return negative if other givenName is greater
	 * <strong>Should</strong> return negative if other middleName is greater
	 * <strong>Should</strong> return negative if other familynamePrefix is greater
	 * <strong>Should</strong> return negative if other familyNameSuffix is greater
	 * <strong>Should</strong> return negative if other dateCreated is greater
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@Override
	public int compareTo(PersonName other) {
		DefaultComparator pnDefaultComparator = new DefaultComparator();
		return pnDefaultComparator.compare(this, other);
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPersonNameId(id);
		
	}
	
	public static void setFormat(String format) {
		if (StringUtils.isEmpty(format)) {
			PersonName.format = OpenmrsConstants.PERSON_NAME_FORMAT_SHORT;
		} else {
			PersonName.format = format;
		}
	}
	
	public static String getFormat() {
		return PersonName.format;
	}
	
	/**
	 Provides a default comparator.
	 @since 1.12
	 **/
	public static class DefaultComparator implements Comparator<PersonName>, Serializable {

		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(PersonName pn1, PersonName pn2) {
			int ret = pn1.getVoided().compareTo(pn2.getVoided());
			if (ret == 0) {
				ret = pn2.getPreferred().compareTo(pn1.getPreferred());
			}
			if (ret == 0) {
				ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.getFamilyName(), pn2.getFamilyName());
			}
			if (ret == 0) {
				ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.getFamilyName2(), pn2.getFamilyName2());
			}
			if (ret == 0) {
				ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.getGivenName(), pn2.getGivenName());
			}
			if (ret == 0) {
				ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.getMiddleName(), pn2.getMiddleName());
			}
			if (ret == 0) {
				ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.getFamilyNamePrefix(), pn2.getFamilyNamePrefix());
			}
			if (ret == 0) {
				ret = OpenmrsUtil.compareWithNullAsGreatest(pn1.getFamilyNameSuffix(), pn2.getFamilyNameSuffix());
			}
			if (ret == 0 && pn1.getDateCreated() != null) {
				ret = OpenmrsUtil.compareWithNullAsLatest(pn1.getDateCreated(), pn2.getDateCreated());
			}
			
			// if we've gotten this far, just check all name values. If they are
			// equal, leave the objects at 0. If not, arbitrarily pick retValue=1
			// and return that (they are not equal).
			if (ret == 0 && !pn1.equalsContent(pn2)) {
				ret = 1;
			}
			
			return ret;
		}
	}
	
}
