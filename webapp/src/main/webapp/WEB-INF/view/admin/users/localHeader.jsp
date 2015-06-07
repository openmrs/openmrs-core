<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Edit Users">
		<li <c:if test='<%= request.getRequestURI().contains("users/users") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/users/users.list">
				<openmrs:message code="User.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Roles">
		<li <c:if test='<%= request.getRequestURI().contains("role") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/users/role.list">
				<openmrs:message code="Role.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Privileges">
		<li <c:if test='<%= request.getRequestURI().contains("privilege") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/users/privilege.list">
				<openmrs:message code="Privilege.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Alerts">
		<li <c:if test='<%= request.getRequestURI().contains("alert") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/users/alert.list">
				<openmrs:message code="Alert.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.users.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>