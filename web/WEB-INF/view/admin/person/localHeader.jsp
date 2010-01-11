<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Relationship Types">
		<li <c:if test='<%= request.getRequestURI().contains("relationshipType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/person/relationshipType.list">
				<spring:message code="RelationshipType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Person Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("personAttribute") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/person/personAttributeType.list">
				<spring:message code="PersonAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:extensionPoint pointId="org.openmrs.admin.person.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>