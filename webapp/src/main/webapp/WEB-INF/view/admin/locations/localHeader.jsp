<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	
	<openmrs:hasPrivilege privilege="Manage Locations">
		<li <c:if test='<%= request.getRequestURI().contains("location") && !request.getRequestURI().contains("Tag") && !request.getRequestURI().contains("hierarchy") && !request.getRequestURI().contains("Attribute") && !request.getRequestURI().contains("addressTemplate") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/locations/location.list">
				<spring:message code="Location.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Location Tags">
		<li <c:if test='<%= request.getRequestURI().contains("locationTag") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/locations/locationTag.list">
				<spring:message code="LocationTag.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Locations">
		<li <c:if test='<%= request.getRequestURI().contains("hierarchy") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/locations/hierarchy.list">
				<spring:message code="Location.hierarchy.view"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Location Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("locationAttributeTypes") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/locations/locationAttributeTypes.list">
				<spring:message code="LocationAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
    <openmrs:hasPrivilege privilege="Manage Address Templates">
		<li <c:if test='<%= request.getRequestURI().contains("addressTemplate") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/locations/addressTemplate.form">
				<spring:message code="AddressTemplate.Manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.locations.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
	
</ul>
