<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Programs">
		<li <c:if test='<%= request.getRequestURI().contains("programs/program") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/programs/program.list">
				<openmrs:message code="Program.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Programs">
		<li <c:if test='<%= request.getRequestURI().contains("programs/conversion") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/programs/conversion.list">
				<openmrs:message code="Program.conversion.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.programs.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>