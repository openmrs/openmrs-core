<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/admin/index.htm" />

<style>
	#menu li {
		display: list-item;
		border-left-width: 0px;
		
	}
	#menu li.first {
		display: none;
	}
	#menu {
		list-style: none;
		margin-left: 10px;
		margin-top: 0;
	}
	h4 {
		margin-bottom: 0;
	}
</style>

<table border="0" width="80%">
<tbody>
<tr>

<td>

<h2><spring:message code="admin.title"/></h2>

<openmrs:hasPrivilege privilege="View Users,Manage Groups,Manage Roles,Manage Privileges">
	<h4>Users</h4>
		<%@ include file="users/localHeader.jsp" %>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="View Patients,Manage Tribes,Manage Identifier Types">
	<h4>Patients</h4>
		<%@ include file="patients/localHeader.jsp" %>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="View Encounters,Manage Locations,Manage Encounter Types">
	<h4>Encounters</h4>
		<%@ include file="encounters/localHeader.jsp" %>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="View Encounters,Manage Mime Types">
	<h4>Observations</h4>
		<%@ include file="observations/localHeader.jsp" %>
</openmrs:hasPrivilege>

</td>

<td>

<openmrs:hasPrivilege privilege="View Orders,Manage Order Types">
	<h4>Orders</h4>
		<%@ include file="orders/localHeader.jsp" %>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="View Concepts,Manage Concept Classes,Manage Concept Datatypes,Manage Concept Proposals">
	<h4>Concepts</h4>
		<%@ include file="concepts/localHeader.jsp" %>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="View Forms,Manage Field Types">
	<h4>Forms</h4> 
		<%@ include file="forms/localHeader.jsp" %>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="View Reports">
	<h4>Reports</h4>
		<%@ include file="reports/localHeader.jsp" %>
</openmrs:hasPrivilege>

</td>

</tr>
</tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>