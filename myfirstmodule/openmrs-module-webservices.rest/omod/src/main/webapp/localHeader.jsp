<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin/index.htm"><spring:message code="admin.title.short"/></a>
	</li>
	
	<openmrs:hasPrivilege privilege="Manage Global Properties">
		<li <c:if test='<%= request.getRequestURI().contains("rest/settings") %>'>class="active"</c:if>>
			<a href="settings.form">
				<spring:message code="webservices.rest.manage.settings"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	
	<li <c:if test='<%= request.getRequestURI().contains("rest/test") %>'>class="active"</c:if>>
		<a href="test.htm">
			<spring:message code="webservices.rest.test"/>
		</a>
	</li>
	
	<li <c:if test='<%= request.getRequestURI().contains("rest/help") %>'>class="active"</c:if>>
		<a href="help.htm">
			<spring:message code="webservices.rest.help"/>
		</a>
	</li>
	
	<openmrs:extensionPoint pointId="org.openmrs.module.webservices.rest.admin.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>