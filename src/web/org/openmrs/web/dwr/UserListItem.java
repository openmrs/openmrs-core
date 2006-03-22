package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;

public class UserListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer userId;
	private String firstName;
	private String lastName;
	private String systemId;
	private String username;
	private String[] roles = new String[0];
	
	public UserListItem() { }
		
	public UserListItem(User user) {

		if (user != null) {
			userId = user.getUserId();
			firstName = user.getFirstName();
			lastName = user.getLastName();
			systemId = user.getSystemId();
			username = user.getUsername();
			int i = 0;
			roles = new String[user.getRoles().size()];
			i = 0;
			for (Role r : user.getRoles()) {
				roles[i++] = r.getRole();
			}
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

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	

}
