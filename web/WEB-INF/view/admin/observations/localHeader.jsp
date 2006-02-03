<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Observations,Edit Observations,Delete Observations,View Observations">
		<li <c:if test="<%= request.getRequestURI().contains("observations/index") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/observations/">
				<spring:message code="Obs.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Mime Types">
		<li <c:if test="<%= request.getRequestURI().contains("mimeType") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/observations/mimeType.list">
				<spring:message code="MimeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>