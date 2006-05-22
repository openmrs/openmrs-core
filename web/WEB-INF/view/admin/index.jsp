<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="View Administration Functions" otherwise="/login.htm" redirect="/admin/index.htm" />

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

<h2><spring:message code="admin.title"/></h2>

<table border="0" width="80%">
	<tbody>
	<tr>
	
		<td valign="top">
		
			<openmrs:hasPrivilege privilege="View Users,Manage Roles,Manage Privileges">
				<h4><spring:message code="User.header"/></h4>
					<%@ include file="users/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Patients,Manage Tribes,Manage Identifier Types">
				<h4><spring:message code="Patient.header"/></h4>
					<%@ include file="patients/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Encounters,Manage Locations,Manage Encounter Types">
				<h4><spring:message code="Encounter.header"/></h4>
					<%@ include file="encounters/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Encounters,Manage Mime Types">
				<h4><spring:message code="Obs.header"/></h4>
					<%@ include file="observations/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Orders,Manage Order Types">
				<h4><spring:message code="Order.header"/></h4>
					<%@ include file="orders/localHeader.jsp" %>
			</openmrs:hasPrivilege>

			<openmrs:hasPrivilege privilege="View Scheduler,Manage Scheduler">
				<h4><spring:message code="Scheduler.header"/></h4>
					<%@ include file="scheduler/localHeader.jsp" %>
			</openmrs:hasPrivilege>
		
		</td>
		
		<td valign="top">
		
			<openmrs:hasPrivilege privilege="View Concepts,Manage Concept Classes,Manage Concept Datatypes,Manage Concept Proposals">
				<h4><spring:message code="Concept.header"/></h4>
					<%@ include file="concepts/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Forms,Manage Field Types">
				<h4><spring:message code="Form.header"/></h4> 
					<%@ include file="forms/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Reports,View Data Exports">
				<h4><spring:message code="Report.header"/></h4>
					<%@ include file="reports/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Form Entry">
				<h4><spring:message code="FormEntry.header"/></h4>
					<%@ include file="formentry/localHeader.jsp" %>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Edit Patients,Audit,View Patients">
				<h4><spring:message code="Maintenance.header"/></h4>
					<%@ include file="maintenance/localHeader.jsp" %>
			</openmrs:hasPrivilege>

  		</td>
	
	</tr>
	</tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>