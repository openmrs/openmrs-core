package org.openmrs;

public class ConceptNameId implements java.io.Serializable {

	public static final long serialVersionUID = 1L;
	
	private Concept concept;
	private String name;
	private String locale;
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	/**
	 * @return Returns the locale.
	 */
	public String getLocale() {
		return locale;
	}
	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(String locale) {
		this.locale = locale;
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
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ConceptNameId))
			return false;
		ConceptNameId cni = (ConceptNameId) obj;
		return (concept.equals(cni.concept)
				&& name.equals(cni.name)
				&& locale.equals(cni.locale));
	}
	
	public int hashCode() {
		return concept.hashCode() + name.hashCode() + locale.hashCode();
	}
	
}
