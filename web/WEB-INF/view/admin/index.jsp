<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Administration Functions" otherwise="/login.htm" redirect="/admin/index.htm" />

<spring:message var="pageTitle" code="index.title" scope="page"/>
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

<h2><spring:message code="admin.title"/></h2>

<table border="0" width="93%">
	<tbody>
	<tr>
	
		<td valign="top" width="30%">
		
			<openmrs:hasPrivilege privilege="View Users,Manage Roles,Manage Privileges">
				<div class="adminMenuList">
					<h4><spring:message code="User.header"/></h4>
						<%@ include file="users/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Patients,Manage Identifier Types">
				<div class="adminMenuList">
					<h4><spring:message code="Patient.header"/></h4>
						<%@ include file="patients/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Manage Relationships,Manage Person Attribute Types">
				<div class="adminMenuList">
					<h4><spring:message code="Person.header"/></h4>
						<%@ include file="person/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Encounters,Manage Encounter Types">
				<div class="adminMenuList">
					<h4><spring:message code="Encounter.header"/></h4>
						<%@ include file="encounters/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Manage Locations">
				<div class="adminMenuList">
					<h4><spring:message code="Location.header"/></h4>
						<%@ include file="locations/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Encounters,Manage Mime Types">
				<div class="adminMenuList">
					<h4><spring:message code="Obs.header"/></h4>
						<%@ include file="observations/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Orders,Manage Order Types">
				<div class="adminMenuList">
					<h4><spring:message code="Order.header"/></h4>
						<%@ include file="orders/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>

			<openmrs:hasPrivilege privilege="View Scheduler,Manage Scheduler">
				<div class="adminMenuList">
					<h4><spring:message code="Scheduler.header"/></h4>
						<%@ include file="scheduler/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
		
			<openmrs:hasPrivilege privilege="Manage Programs">
				<div class="adminMenuList">
					<h4><spring:message code="Program.header"/></h4>
						<%@ include file="programs/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
		
		</td>
		
		<td valign="top" width="30%">
		
			<openmrs:hasPrivilege privilege="View Concepts,Manage Concept Classes,Manage Concept Datatypes,Manage Concept Proposals">
				<div class="adminMenuList">
					<h4><spring:message code="Concept.header"/></h4>
						<%@ include file="concepts/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View Forms,Manage Field Types">
				<div class="adminMenuList">
					<h4><spring:message code="Form.header"/></h4> 
						<%@ include file="forms/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
				<div class="adminMenuList">
					<h4><spring:message code="Hl7Messages.header"/></h4>
						<%@ include file="hl7/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:hasPrivilege privilege="Edit Patients,Audit,View Patients">
				<div class="adminMenuList">
					<h4><spring:message code="Maintenance.header"/></h4>
						<%@ include file="maintenance/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>

  		</td>
		<td valign="top" width="30%">
			
			<openmrs:hasPrivilege privilege="Manage Modules">
				<div class="adminMenuList">
					<h4><spring:message code="Module.header"/></h4>
						<%@ include file="modules/localHeader.jsp" %>
				</div>
			</openmrs:hasPrivilege>
			
			<openmrs:extensionPoint pointId="org.openmrs.admin.list" type="html">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<div class="adminMenuList">
						<h4><spring:message code="${extension.title}"/></h4>
						<ul id="menu">
							<c:forEach items="${extension.links}" var="link">
								<c:choose>
									<c:when test="${fn:startsWith(link.key, 'module/')}">
										<%-- Added for backwards compatibility for most links --%>
										<li><a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a></li>
									</c:when>
									<c:otherwise>
										<%-- Allows for external absolute links  --%>
										<li><a href='<c:url value="${link.key}"/>'><spring:message code='${link.value}'/></a></li>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</ul>
					</div>
				</openmrs:hasPrivilege>
			</openmrs:extensionPoint>
			
		</td>
			
	</tr>
	</tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>
