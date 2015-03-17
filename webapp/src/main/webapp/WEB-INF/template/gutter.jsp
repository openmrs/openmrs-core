<ul class="navList" 
	xmlns:spring="http://www.springframework.org/tags"
	xmlns:openmrs="urn:jsptld:/WEB-INF/taglibs/openmrs.tld">
		
	<li id="homeNavLink" class="firstChild">
		<a href="${pageContext.request.contextPath}/"><openmrs:message code="Navigation.home"/></a>
	</li>

	<li id="findPatientNavLink">
		<a href="${pageContext.request.contextPath}/findPatient.htm">
			<openmrs:hasPrivilege privilege="Add Patients">
				<openmrs:message code="Navigation.findCreatePatient"/>
			</openmrs:hasPrivilege>
			<openmrs:hasPrivilege privilege="Add Patients" inverse="true">
				<openmrs:message code="Navigation.findPatient"/>
			</openmrs:hasPrivilege>
		</a>
	</li>
	
	<openmrs:hasPrivilege privilege="View Concepts">
		<li id="dictionaryNavLink">
			<a href="${pageContext.request.contextPath}/dictionary"><openmrs:message code="Navigation.dictionary"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:extensionPoint pointId="org.openmrs.gutter.tools" type="html" 
		requiredClass="org.openmrs.module.web.extension.LinkExt">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<li>
			<a href="<openmrs_tag:url value="${extension.url}"/>"><openmrs:message code="${extension.label}"/></a>
			</li>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>

	<openmrs:hasPrivilege privilege="View Administration Functions">
		<li id="administrationNavLink">
			<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="Navigation.administration"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	
</ul>