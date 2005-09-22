<%@ page import="org.openmrs.context.HibernateContext" %>
<html>
<body>

<%

try {

	org.openmrs.context.Context c = new HibernateContext();

} catch (Exception e) {
	out.println("{ERROR}");
}
%>

 got here

</html>