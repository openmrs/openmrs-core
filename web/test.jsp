<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="java.util.List" %>

<html>
<body>

<%

try {
	
	Context context = (Context)session.getValue("__openmrs_context");
	context.authenticate("admin", "test");			
	out.write("<br>Authenticated<br>");
	User user = context.getAuthenticatedUser();
	out.write("As " + user.getFirstName() + " " + user.getLastName());

	UserService us = context.getUserService();		out.write("--Getting userService--<br>");
	String uname = "USER-1";
	User u2 = us.getUserByUsername(uname);	out.write("--Getting by username--<br>");
	
	if (u2 == null)
		out.write("Error: " + uname + " not found<br>");
	else {
		out.write("Got " + u2.getFirstName() + " " + u2.getLastName() + "<br>");
		User creator = u2.getCreator();
		out.write("Created by " + creator.getUsername() + "<br>");
		out.write("Creator privileges: ");
		for (Iterator i = creator.getRoles().iterator(); i.hasNext();) {
			Role role = (Role)i.next();
			out.write("Role: " + role.getRole());
			for (Iterator i2 = role.getPrivileges().iterator(); i2.hasNext();) {
				Privilege p = (Privilege)i2.next();
				out.write("Priv: " + p.getPrivilege());
			}
		}
	}
	
	out.write("<br><br>Users:<br><table border=1>");
	out.write("<tr><th>username</th><th>first</th><th>last</th><th>Creator</th><th>Roles</th></tr>");
	List users = us.getUsersByRole((Role)u2.getRoles().toArray()[0]);
	for (Iterator i3 = users.iterator(); i3.hasNext();) {
		User tmpuser = (User)i3.next();
		out.write("<tr>");
		out.write("<td>" + tmpuser.getUsername() + "</td>");
		out.write("<td>" + tmpuser.getFirstName()+ "</td>");
		out.write("<td>" + tmpuser.getLastName() + "</td>");
		out.write("<td>" + tmpuser.getCreator().getUsername() + "</td>");
		out.write("<td>" + tmpuser.getRoles() + "</td>");
		out.write("</tr>");
	}
	out.write("</table>");
	
	
} catch (Exception e) {
	out.println("Uh oh, error: <b>" + e + "</b>");
}
%>

 <br><br> done 

</html>