<ul id="navList">
	<li class="firstChild">
		<a href="<%= request.getContextPath() %>/"><spring:message code="Navigation.home"/></a>
	</li>
	
	<li>
		<a href="<%= request.getContextPath() %>/findPatient.htm"><spring:message code="Navigation.findPatient"/></a>
	</li>
	
	<openmrs:hasPrivilege privilege="View Concepts">
		<li>
			<a href="<%= request.getContextPath() %>/dictionary"><spring:message code="Navigation.dictionary"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="View Administration Functions">
		<li>
			<a href="<%= request.getContextPath() %>/admin"><spring:message code="Navigation.administration"/></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="Analyze">
		<li>
			<a href="<%= request.getContextPath() %>/analysis.list"><spring:message code="Navigation.analysis"/></a>
		</li>
	</openmrs:hasPrivilege>

	<openmrs:extensionCount pointId="org.openmrs.gutter.tools" var="howManyTools"/>
	<c:if test="${howManyTools > 0}">
			<openmrs:extensionPoint pointId="org.openmrs.gutter.tools" type="html">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<li>
					<a href="<%= request.getContextPath() %>/${extension.url}"><spring:message code="${extension.label}"/></a>
					</li>
				</openmrs:hasPrivilege>
			</openmrs:extensionPoint>
	</c:if>

	<li>
		<a href="<%= request.getContextPath() %>/options.form"><spring:message code="Navigation.options"/></a>
	</li>
	
</ul>