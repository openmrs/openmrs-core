package org.openmrs;

import java.util.Date;
import java.util.List;

/**
 * Concept 
 */
public class Concept implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptId;
	private boolean retired;
	private String name;
	private String shortName;
	private String description;
	private ConceptDatatype conceptDatatype;
	private ConceptClass conceptClass;
	private String units;
	private String icd10;
	private String loinc;
	private String version;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private List names;
	private List answers;
	private List synonyms;

	// Constructors

	/** default constructor */
	public Concept() {
	}

	/** constructor with id */
	public Concept(Integer conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return Returns the answers.
	 */
	public List getAnswers() {
		return answers;
	}

	/**
	 * @param answers The answers to set.
	 */
	public void setAnswers(List answers) {
		this.answers = answers;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the conceptClass.
	 */
	public ConceptClass getConceptClass() {
		return conceptClass;
	}

	/**
	 * @param conceptClass The conceptClass to set.
	 */
	public void setConceptClass(ConceptClass conceptClass) {
		this.conceptClass = conceptClass;
	}

	/**
	 * @return Returns the conceptDatatype.
	 */
	public ConceptDatatype getConceptDatatype() {
		return conceptDatatype;
	}

	/**
	 * @param conceptDatatype The conceptDatatype to set.
	 */
	public void setConceptDatatype(ConceptDatatype conceptDatatype) {
		this.conceptDatatype = conceptDatatype;
	}

	/**
	 * @return Returns the conceptId.
	 */
	public Integer getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId The conceptId to set.
	 */
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the icd10.
	 */
	public String getIcd10() {
		return icd10;
	}

	/**
	 * @param icd10 The icd10 to set.
	 */
	public void setIcd10(String icd10) {
		this.icd10 = icd10;
	}

	/**
	 * @return Returns the loinc.
	 */
	public String getLoinc() {
		return loinc;
	}

	/**
	 * @param loinc The loinc to set.
	 */
	public void setLoinc(String loinc) {
		this.loinc = loinc;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the names.
	 */
	public List getNames() {
		return names;
	}

	/**
	 * @param names The names to set.
	 */
	public void setNames(List names) {
		this.names = names;
	}

	/**
	 * @return Returns the retired.
	 */
	public boolean isRetired() {
		return retired;
	}

	/**
	 * @param retired The retired to set.
	 */
	public void setRetired(boolean retired) {
		this.retired = retired;
	}

	/**
	 * @return Returns the shortName.
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName The shortName to set.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return Returns the synonyms.
	 */
	public List getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms The synonyms to set.
	 */
	public void setSynonyms(List synonyms) {
		this.synonyms = synonyms;
	}

	/**
	 * @return Returns the units.
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units The units to set.
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	// Property accessors

}