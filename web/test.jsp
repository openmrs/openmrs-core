<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.context.ContextFactory" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.openmrs.api.UserService" %>

<html>
<body>

<%

try {
												out.write("--Getting context--<br>");
	Context c = ContextFactory.getContext();	out.write("--Starting tx--<br>");
	c.startTransaction();						out.write("--Authenticating--<br>");
	c.authenticate("admin", "test");			
	out.write("<br>Authenticated<br>");
	User user = c.getAuthenticatedUser();
	out.write("As " + user.getFirstName() + " " + user.getLastName());

	UserService us = c.getUserService();		out.write("--Getting userService--<br>");
	String uname = "USER-1";
	User u2 = us.getUserByUsername(uname);	out.write("--Getting by username--<br>");
	
	if (u2 == null)
		out.write("Error: " + uname + " not found<br>");
	else {
		out.write("Got " + u2.getFirstName() + " " + u2.getLastName() + "<br>");
		User creator = u2.getCreator();
		out.write("Created by " + creator.getUsername() + "<br>");
		out.write("Creator privileges: ");
		for (Iterator i = u2.getRoles().iterator(); i.hasNext();) {
			Role role = (Role)i.next();
			out.write("Role: " + role.getRole());
			for (Iterator i2 = role.getPrivileges().iterator(); i2.hasNext();) {
				Privilege p = (Privilege)i2.next();
				out.write("Priv: " + p.getPrivilege());
			}
		}
	}
	
	c.endTransaction();

} catch (Exception e) {
	out.println("Uh oh, error: <b>" + e + "</b>");
}
%>

 <br><br> done 

</html>