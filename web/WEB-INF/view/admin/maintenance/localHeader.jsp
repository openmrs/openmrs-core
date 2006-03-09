<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Edit Patients">
		<li <c:if test="<%= request.getRequestURI().contains("mrnGenerator") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/mrnGenerator.htm">
				<spring:message code="MRNGenerator.generate.identifiers"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Audit">
		<li <c:if test="<%= request.getRequestURI().contains("auditPatientIdentifiers") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/auditPatientIdentifiers.htm">
				<spring:message code="AuditPatientIdentifiers.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>