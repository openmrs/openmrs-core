<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationships" otherwise="/login.htm" redirect="/admin/person/relationship.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<form method="post" action="">

<table>
	<tr>
		<td><openmrs:message code="">



</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>