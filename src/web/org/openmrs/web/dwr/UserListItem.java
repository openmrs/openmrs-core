package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;

public class UserListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer userId;
	private String firstName;
	private String lastName;
	private String systemId;

	public UserListItem() { }
		
	public UserListItem(User user) {

		if (user != null) {
			userId = user.getUserId();
			firstName = user.getFirstName();
			lastName = user.getLastName();
			systemId = user.getSystemId();
		}
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
