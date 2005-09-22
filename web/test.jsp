<%@ page import="org.openmrs.api.hibernate.HibernateUtil" %>
<html>
<body>

<%

org.hibernate.Session s = HibernateUtil.currentSession();
org.openmrs.User user = (org.openmrs.User)s.get(org.openmrs.User.class, 4);
out.println(user.getFirstName());

%>

 got here

</html>