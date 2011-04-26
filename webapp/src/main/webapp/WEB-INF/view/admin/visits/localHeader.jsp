<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Visit Types,View Visit Types">
		<li <c:if test='<%= request.getRequestURI().contains("visits/vistTypeList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/visits/visitType.list">
				<spring:message code="VisitType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Visit Attribute Types,View Visit Attribute Types,Purge Visit Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("visits/vistAttributeTypeList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/visits/visitAttributeType.list">
				<spring:message code="VisitAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.visits.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>