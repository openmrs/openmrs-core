package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;

public class LocationListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer locationId;
	private String name;
	
	public LocationListItem() { }
		
	public LocationListItem(Location location) {

		if (location != null) {
			locationId = location.getLocationId();
			name = location.getName();
		}
	}

	public Integer getLocationId() {
		return locationId;
	}

	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}