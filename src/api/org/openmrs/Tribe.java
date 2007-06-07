package org.openmrs;

/**
 * Tribe
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Tribe implements java.io.Serializable {

	public static final long serialVersionUID = 113232L;

	// Fields

	private Integer tribeId;
	private Boolean retired = false;
	private String name;

	// Constructors

	/** default constructor */
	public Tribe() {
	}

	/** constructor with id */
	public Tribe(Integer tribeId) {
		this.tribeId = tribeId;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Tribe) {
			Tribe t = (Tribe)obj;
			if (this.getTribeId() != null && t.getTribeId() != null)
				return (this.getTribeId().equals(t.getTribeId()));
			/*return (this.getName().matches(t.getName()) &&
					this.isRetired() == t.isRetired()); */
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getTribeId() == null) return super.hashCode();
		return this.getTribeId().hashCode();
	}

	// Property accessors

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
	 * @return Returns the retired status.
	 */
	public Boolean isRetired() {
		if (retired == null)
			return false;
		return retired;
	}
	
	public Boolean getRetired() {
		return isRetired();
	}

	/**
	 * @param retired The retired status to set.
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * @return Returns the tribeId.
	 */
	public Integer getTribeId() {
		return tribeId;
	}

	/**
	 * @param tribeId The tribeId to set.
	 */
	public void setTribeId(Integer tribeId) {
		this.tribeId = tribeId;
	}


}