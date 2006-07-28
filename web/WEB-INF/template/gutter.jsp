	<a href="<%= request.getContextPath() %>/"><spring:message code="Navigation.home"/></a><br />
	
	<a href="<%= request.getContextPath() %>/formentry"><spring:message code="Navigation.findPatient"/></a><br />
	
	<openmrs:hasPrivilege privilege="View Concepts">
		<a href="<%= request.getContextPath() %>/dictionary"><spring:message code="Navigation.dictionary"/></a><br />
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="View Administration Functions">
		<a href="<%= request.getContextPath() %>/admin"><spring:message code="Navigation.administration"/></a><br />
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="Analyze">
		<a href="<%= request.getContextPath() %>/analysis.list"><spring:message code="Navigation.analysis"/></a><br />
	</openmrs:hasPrivilege>
	
	<a href="<%= request.getContextPath() %>/options.form"><spring:message code="Navigation.options"/></a><br />
	
	<br /><br />
	<br /><br />