<ul id="navList">
	<li id="homeNavLink" class="firstChild">
		<a href="${pageContext.request.contextPath}/"><spring:message code="Navigation.home"/></a>
	</li>

	<li id="findPatientNavLink">
		<a href="${pageContext.request.contextPath}/findPatient.htm">
			<openmrs:hasPrivilege privilege="Add Patients">
				<spring:message code="Navigation.findCreatePatient"/>
			</openmrs:hasPrivilege>
			<openmrs:hasPrivilege privilege="Add Patients" inverse="true">
				<spring:message code="Navigation.findPatient"/>
			</openmrs:hasPrivilege>
		</a>
	</li>
	
	<openmrs:hasPrivilege privilege="View Concepts">
		<li id="dictionaryNavLink">
			<a href="${pageContext.request.contextPath}/dictionary"><spring:message code="Navigation.dictionary"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:extensionPoint pointId="org.openmrs.gutter.tools" type="html">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<li>
			<a href="${pageContext.request.contextPath}/${extension.url}"><spring:message code="${extension.label}"/></a>
			</li>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>

	<openmrs:hasPrivilege privilege="View Administration Functions">
		<li id="administrationNavLink">
			<a href="${pageContext.request.contextPath}/admin"><spring:message code="Navigation.administration"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	
</ul>