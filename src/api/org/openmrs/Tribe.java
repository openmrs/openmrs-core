package org.openmrs;

/**
 * Tribe
 */
public class Tribe implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer tribeId;
	private Boolean retired;
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
				return (this.getTribeId() == t.getTribeId());
			return (this.getName().matches(t.getName()) &&
					this.isRetired().equals(t.isRetired()));
		}
		return false;
	}
	
	// Property accessors

	/**
	 * 
	 */
	public Integer getTribeId() {
		return this.tribeId;
	}

	public void setTribeId(Integer tribeId) {
		this.tribeId = tribeId;
	}

	/**
	 * 
	 */
	public Boolean isRetired() {
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

}