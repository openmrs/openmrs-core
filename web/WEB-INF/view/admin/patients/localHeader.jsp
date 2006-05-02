<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Patients,Edit Patients,Delete Patients,View Patients">
		<li <c:if test="<%= request.getRequestURI().contains("patients/index") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/patients/">
				<spring:message code="Patient.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<c:if test="'Still working on this' == 'true'">
		<openmrs:hasPrivilege privilege="Manage Relationships">
			<li <c:if test="<%= request.getRequestURI().contains("relationship") %>">class="active"</c:if>>
				<a href="${pageContext.request.contextPath}/admin/patients/relationship.list">
					<spring:message code="Relationship.manage"/>
				</a>
			</li>
		</openmrs:hasPrivilege>
	</c:if>
	<openmrs:hasPrivilege privilege="Manage Tribes">
		<li <c:if test="<%= request.getRequestURI().contains("tribe") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/patients/tribe.list">
				<spring:message code="Tribe.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Identifier Types">
		<li <c:if test="<%= request.getRequestURI().contains("patientIdentifierType") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/patients/patientIdentifierType.list">
				<spring:message code="PatientIdentifierType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>