package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Concept 
 */
public class Concept implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer conceptId;
	private Boolean retired;
	private String name;
	private String nameAlt;
	private Integer correctConcept;
	private String shortName;
	private String description;
	private String formText;
	private String icd10;
	private String loinc;
	private User creator;
	private Date dateCreated;
	private String version;
	private User changedBy;
	private Date dateChanged;
	private String units;
	private ConceptDatatype conceptDatatype;
	private Set conceptNames;
	private Set drugs;
	private Set fieldAnswers;
	private Set conceptAnswersByConceptId;
	private Set conceptAnswersByAnswerConcept;
	private Set fields;
	
	private Set conceptWords;
	private Set conceptSynonyms;

	private Set drugIngredientsByConceptId;
	private Set drugIngredientsByIngredientId;
	private ConceptNumeric conceptNumeric;
	private Set obsByConceptId;
	private Set obsByValueCoded;
	private Set conceptSetsByConceptSet;
	private Set conceptSetsByConceptId;
	private ConceptClass conceptClass;

	// Constructors

	/** default constructor */
	public Concept() {
	}

	/** constructor with id */
	public Concept(Integer conceptId) {
		this.conceptId = conceptId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getConceptId() {
		return this.conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * 
	 */
	public Boolean getRetired() {
		return this.retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getNameAlt() {
		return this.nameAlt;
	}

	public void setNameAlt(String nameAlt) {
		this.nameAlt = nameAlt;
	}

	/**
	 * 
	 */
	public Integer getCorrectConcept() {
		return this.correctConcept;
	}

	public void setCorrectConcept(Integer correctConcept) {
		this.correctConcept = correctConcept;
	}

	/**
	 * 
	 */
	public String getShortName() {
		return this.shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public String getFormText() {
		return this.formText;
	}

	public void setFormText(String formText) {
		this.formText = formText;
	}

	/**
	 * 
	 */
	public String getIcd10() {
		return this.icd10;
	}

	public void setIcd10(String icd10) {
		this.icd10 = icd10;
	}

	/**
	 * 
	 */
	public String getLoinc() {
		return this.loinc;
	}

	public void setLoinc(String loinc) {
		this.loinc = loinc;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * 
	 */
	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	/**
	 * 
	 */
	public String getUnits() {
		return this.units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * 
	 */
	public ConceptDatatype getConceptDatatype() {
		return this.conceptDatatype;
	}

	public void setConceptDatatype(ConceptDatatype conceptDatatype) {
		this.conceptDatatype = conceptDatatype;
	}

	/**
	 * 
	 */
	public Set getConceptNames() {
		return this.conceptNames;
	}

	public void setConceptNames(Set conceptNames) {
		this.conceptNames = conceptNames;
	}

	/**
	 * 
	 */
	public Set getDrugs() {
		return this.drugs;
	}

	public void setDrugs(Set drugs) {
		this.drugs = drugs;
	}

	/**
	 * 
	 */
	public Set getFieldAnswers() {
		return this.fieldAnswers;
	}

	public void setFieldAnswers(Set fieldAnswers) {
		this.fieldAnswers = fieldAnswers;
	}

	/**
	 * 
	 */
	public Set getConceptAnswersByConceptId() {
		return this.conceptAnswersByConceptId;
	}

	public void setConceptAnswersByConceptId(Set conceptAnswersByConceptId) {
		this.conceptAnswersByConceptId = conceptAnswersByConceptId;
	}

	/**
	 * 
	 */
	public Set getConceptAnswersByAnswerConcept() {
		return this.conceptAnswersByAnswerConcept;
	}

	public void setConceptAnswersByAnswerConcept(
			Set conceptAnswersByAnswerConcept) {
		this.conceptAnswersByAnswerConcept = conceptAnswersByAnswerConcept;
	}

	/**
	 * 
	 */
	public Set getFields() {
		return this.fields;
	}

	public void setFields(Set fields) {
		this.fields = fields;
	}

	/**
	 * 
	 */
	public Set getConceptWords() {
		return this.conceptWords;
	}

	public void setConceptWords(Set conceptWords) {
		this.conceptWords = conceptWords;
	}

	/**
	 * 
	 */
	public Set getConceptSynonyms() {
		return this.conceptSynonyms;
	}

	public void setConceptSynonyms(Set conceptSynonyms) {
		this.conceptSynonyms = conceptSynonyms;
	}

	/**
	 * 
	 */
	public Set getDrugIngredientsByConceptId() {
		return this.drugIngredientsByConceptId;
	}

	public void setDrugIngredientsByConceptId(Set drugIngredientsByConceptId) {
		this.drugIngredientsByConceptId = drugIngredientsByConceptId;
	}

	/**
	 * 
	 */
	public Set getDrugIngredientsByIngredientId() {
		return this.drugIngredientsByIngredientId;
	}

	public void setDrugIngredientsByIngredientId(
			Set drugIngredientsByIngredientId) {
		this.drugIngredientsByIngredientId = drugIngredientsByIngredientId;
	}

	/**
	 * 
	 */
	public ConceptNumeric getConceptNumeric() {
		return this.conceptNumeric;
	}

	public void setConceptNumeric(ConceptNumeric conceptNumeric) {
		this.conceptNumeric = conceptNumeric;
	}

	/**
	 * 
	 */
	public Set getObsByConceptId() {
		return this.obsByConceptId;
	}

	public void setObsByConceptId(Set obsByConceptId) {
		this.obsByConceptId = obsByConceptId;
	}

	/**
	 * 
	 */
	public Set getObsByValueCoded() {
		return this.obsByValueCoded;
	}

	public void setObsByValueCoded(Set obsByValueCoded) {
		this.obsByValueCoded = obsByValueCoded;
	}

	/**
	 * 
	 */
	public Set getConceptSetsByConceptSet() {
		return this.conceptSetsByConceptSet;
	}

	public void setConceptSetsByConceptSet(Set conceptSetsByConceptSet) {
		this.conceptSetsByConceptSet = conceptSetsByConceptSet;
	}

	/**
	 * 
	 */
	public Set getConceptSetsByConceptId() {
		return this.conceptSetsByConceptId;
	}

	public void setConceptSetsByConceptId(Set conceptSetsByConceptId) {
		this.conceptSetsByConceptId = conceptSetsByConceptId;
	}

	/**
	 * 
	 */
	public ConceptClass getConceptClass() {
		return this.conceptClass;
	}

	public void setConceptClass(ConceptClass conceptClass) {
		this.conceptClass = conceptClass;
	}

	/**
	 * 
	 */
	public User getChangedBy() {
		return this.changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

}