
package org.openmrs.CurrentUsers;

import javax.servlet.http.HttpSession;

public class SessionManager {
	

	    public static void removeUserFromSession(HttpSession session) {
			CurrentUsersService.removeUser(session);
	    }
	}

