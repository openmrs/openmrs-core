<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.context.ContextFactory" %>
<%@ page import="org.openmrs.User" %>
<html>
<body>

<%

try {

	Context c = ContextFactory.getContext();
	c.authenticate("1-8", "test");
	User user = c.getAuthenticatedUser();
	out.write(user.getFirstName());

} catch (Exception e) {
	out.println("{ERROR}");
}
%>

 got here

</html>