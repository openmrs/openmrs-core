<%@ page import="org.openmrs.api.hibernate.HibernateUtil" %>
<html>
<body>

<%

try {
	org.hibernate.Session s = HibernateUtil.currentSession();
	org.openmrs.User user = (org.openmrs.User)s.get(org.openmrs.User.class, new Integer(4));
	out.println(user.getFirstName());
} catch (Exception e) {
	out.println("{ERROR}");
}
%>

 got here

</html>