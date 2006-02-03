<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Encounters,Edit Encounters,Delete Encounters,View Encounters">
		<li <c:if test="<%= request.getRequestURI().contains("encounters/index") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/encounters/index.htm">
				<spring:message code="Encounter.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Locations">
		<li <c:if test="<%= request.getRequestURI().contains("location") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/encounters/location.list">
				<spring:message code="Location.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Encounter Types">
		<li <c:if test="<%= request.getRequestURI().contains("encounterType") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/encounters/encounterType.list">
				<spring:message code="EncounterType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>