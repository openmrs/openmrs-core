package org.openmrs;

/**
 * Tribe
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Tribe implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer tribeId;
	private boolean retired;
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
			return (this.getName().matches(t.getName()) &&
					this.isRetired() == t.isRetired());
		}
		return false;
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