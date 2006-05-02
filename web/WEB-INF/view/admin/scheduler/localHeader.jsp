<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="View Tasks">
		<li <c:if test="<%= request.getRequestURI().contains("scheduler/tasks") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/scheduler/scheduler.list" class="retired">
				<spring:message code="Scheduler.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>