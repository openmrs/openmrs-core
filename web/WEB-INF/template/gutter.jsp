<ul id="navList">
	<li class="firstChild">
		<a href="${pageContext.request.contextPath}/"><spring:message code="Navigation.home"/></a>
	</li>
	
	<li>
		<a href="${pageContext.request.contextPath}/findPatient.htm"><spring:message code="Navigation.findPatient"/></a>
	</li>
	
	<openmrs:hasPrivilege privilege="View Concepts">
		<li>
			<a href="${pageContext.request.contextPath}/dictionary"><spring:message code="Navigation.dictionary"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="View Patient Cohorts">
		<li>
			<a href="${pageContext.request.contextPath}/cohortBuilder.list"><spring:message code="Navigation.analysis"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="View Administration Functions">
		<li>
			<a href="${pageContext.request.contextPath}/admin"><spring:message code="Navigation.administration"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:extensionCount pointId="org.openmrs.gutter.tools" var="howManyTools"/>
	<c:if test="${howManyTools > 0}">
			<openmrs:extensionPoint pointId="org.openmrs.gutter.tools" type="html">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<li>
					<a href="${pageContext.request.contextPath}/${extension.url}"><spring:message code="${extension.label}"/></a>
					</li>
				</openmrs:hasPrivilege>
			</openmrs:extensionPoint>
	</c:if>

	<li>
		<a href="${pageContext.request.contextPath}/options.form"><spring:message code="Navigation.options"/></a>
	</li>
	
</ul>