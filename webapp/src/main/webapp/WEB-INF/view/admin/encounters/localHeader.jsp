<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Encounters,Edit Encounters,Delete Encounters,View Encounters">
		<li <c:if test='<%= request.getRequestURI().contains("encounters/index") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/encounters/index.htm">
				<spring:message code="Encounter.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Encounter Types">
		<li <c:if test='<%= request.getRequestURI().contains("encounterType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/encounters/encounterType.list">
				<spring:message code="EncounterType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>