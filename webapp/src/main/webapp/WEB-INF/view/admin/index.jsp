<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Administration Functions" otherwise="/login.htm" redirect="/admin/index.htm" />

<openmrs:message var="pageTitle" code="admin.titlebar" scope="page"/>
<openmrs:message var="pageTitle" code="admin.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<style>
	.adminMenuList #menu li {
		display: list-item;
		border-left-width: 0px;
		
	}
	.adminMenuList #menu li.first {
		display: none;
	}
	.adminMenuList #menu {
		list-style: none;
		margin-left: 10px;
		margin-top: 0;
	}
	h4 {
		margin-bottom: 0;
	}
</style>

<h2><openmrs:message code="admin.header"/></h2>

<table border="0" width="93%">
	<tbody>
	<tr>
	
		<td valign="top" width="30%">
		
			<openmrs:hasPrivilege privilege="View Users,Manage Roles,Manage Privileges">
				<div class="adminMenuList">
					<h4><openmrs:message code="User.header"/></h4>
						<%@ include file="users/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Patients,Manage Identifier Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Patient.header"/></h4>
						<%@ include file="patients/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Manage Relationships,Manage Person Attribute Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Person.header"/></h4>
						<%@ include file="person/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Visit Types,Manage Visit Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Visit.header"/></h4>
						<%@ include file="visits/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Encounters,Manage Encounter Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Encounter.header"/></h4>
						<%@ include file="encounters/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Providers,Manage Providers,Manage Provider Attribute Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Provider.header"/></h4>
						<%@ include file="provider/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Manage Locations">
				<div class="adminMenuList">
					<h4><openmrs:message code="Location.header"/></h4>
						<%@ include file="locations/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Encounters,Manage Mime Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Obs.header"/></h4>
						<%@ include file="observations/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>

			<openmrs:hasPrivilege privilege="View Scheduler,Manage Scheduler">
				<div class="adminMenuList">
					<h4><openmrs:message code="Scheduler.header"/></h4>
						<%@ include file="scheduler/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
		
			<openmrs:hasPrivilege privilege="Manage Programs">
				<div class="adminMenuList">
					<h4><openmrs:message code="Program.header"/></h4>
						<%@ include file="programs/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
		
		</td>
		
		<td valign="top" width="30%">
		
			<openmrs:hasPrivilege privilege="View Concepts,Manage Concept Classes,Manage Concept Datatypes,Manage Concept Proposals">
				<div class="adminMenuList">
					<h4><openmrs:message code="Concept.header"/></h4>
						<%@ include file="concepts/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Forms,Manage Field Types">
				<div class="adminMenuList">
					<h4><openmrs:message code="Form.header"/></h4> 
						<%@ include file="forms/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
				<div class="adminMenuList">
					<h4><openmrs:message code="Hl7Messages.header"/></h4>
						<%@ include file="hl7/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Edit Patients,Audit,View Patients">
				<div class="adminMenuList">
					<h4><openmrs:message code="Maintenance.header"/></h4>
						<%@ include file="maintenance/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>

  		</td>
		<td valign="top" width="30%">
			
			<openmrs:hasPrivilege privilege="Manage Modules">
				<div class="adminMenuList">
					<h4><openmrs:message code="Module.header"/></h4>
						<%@ include file="modules/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:extensionPoint pointId="org.openmrs.admin.list" type="html" requiredClass="org.openmrs.module.web.extension.AdministrationSectionExt">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<c:catch var="ex">
						<div class="adminMenuList">
							<h4><openmrs:message code="${extension.title}"/></h4>
							<ul id="menu">
								<c:forEach items="${extension.links}" var="link">
									<li><a href='<openmrs_tag:url value="${link.key}"/>'><openmrs:message code='${link.value}'/></a></li>
								</c:forEach>
							</ul>
						</div>
					</c:catch>
					<c:if test="${not empty ex}">
						<div class="error">
							<openmrs:message code="fix.error.plain"/> <br/>
							<b>${ex}</b>
							<div style="height: 200px; width: 800px; overflow: scroll">
								<c:forEach var="row" items="${ex.cause.stackTrace}">
									${row}<br/>
								</c:forEach>
							</div>
						</div>
					</c:if>
				</openmrs:hasPrivilege>
			</openmrs:extensionPoint>
			
		</td>
			
	</tr>
	</tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>
