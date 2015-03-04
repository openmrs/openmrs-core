<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Persons,Edit Persons,Delete Persons,View Persons">
		<li <c:if test='<%= request.getRequestURI().contains("person/index") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/person/index.htm">
				<openmrs:message code="Person.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Relationship Types">
		<li <c:if test='<%= request.getRequestURI().contains("relationshipType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/person/relationshipType.list">
				<openmrs:message code="RelationshipType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Person Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("personAttribute") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/person/personAttributeType.list">
				<openmrs:message code="PersonAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:extensionPoint pointId="org.openmrs.admin.person.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>