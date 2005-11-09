<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("encounter.") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/encounters/encounter.jsp" class="retired">
			<spring:message code="Encounter.manage"/>
		</a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("location") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/encounters/location.list">
			<spring:message code="Location.manage"/>
		</a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("encounterType") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/encounters/encounterType.list">
			<spring:message code="EncounterType.manage"/>
		</a>
	</li>
</ul>