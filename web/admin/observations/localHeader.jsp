<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("observations/index") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/observations/">
			<spring:message code="Observation.manage"/>
		</a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("mimeType") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/observations/mimeType.list">
			<spring:message code="MimeType.manage"/>
		</a>
	</li>
</ul>