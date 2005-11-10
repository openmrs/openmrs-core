<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("users/user") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/users/user.list">
			<spring:message code="User.manage"/>
		</a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("role") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/users/role.list">
			<spring:message code="Role.manage"/>
		</a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("privilege") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/users/privilege.list">
			<spring:message code="Privilege.manage"/>
		</a>
	</li>
</ul>