<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Visit Types,View Visit Types">
		<li <c:if test='<%= request.getRequestURI().contains("visits/visitTypeList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/visits/visitType.list">
				<openmrs:message code="VisitType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Visit Attribute Types,View Visit Attribute Types,Purge Visit Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("visits/visitAttributeTypeList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/visits/visitAttributeType.list">
				<openmrs:message code="VisitAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Configure Visits">
		<li <c:if test='<%= request.getRequestURI().contains("configureVisits") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/visits/configureVisits.list">
				<openmrs:message code="Visit.configure"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.visits.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>