<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="View FormEntry Queue">
		<li <c:if test="<%= request.getRequestURI().contains("formentry/formEntryQueue") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/formentry/formEntryQueue.list">
				<spring:message code="FormEntryQueue.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>