<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>

<openmrs:require privilege="Manage Users" otherwise="/openmrs/login.html" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<br>

<h2>User Management</h2>	
<br>

	
<%@ include file="/WEB-INF/template/footer.jsp" %>