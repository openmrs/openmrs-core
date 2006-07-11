	<a href="<%= request.getContextPath() %>/">Main</a><br />
	
	<a href="<%= request.getContextPath() %>/formentry">Form Entry</a><br />
	
	<openmrs:hasPrivilege privilege="View Concepts">
		<a href="<%= request.getContextPath() %>/dictionary">Dictionary</a><br />
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="View Administration Functions">
		<a href="<%= request.getContextPath() %>/admin">Administration</a><br />
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="Analyze">
		<a href="<%= request.getContextPath() %>/analysis.list">Analysis</a><br />
	</openmrs:hasPrivilege>
	
	<a href="<%= request.getContextPath() %>/options.form">Options</a><br />
	
	<br /><br />
	<br /><br />
