<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Modules">
		<li <c:if test='<%= request.getRequestURI().contains("modules/moduleList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/modules/module.list">
				<spring:message code="Module.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Modules">
		<li <c:if test='<%= request.getRequestURI().contains("modules/moduleProperties") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/modules/moduleProperties.form">
				<spring:message code="Module.manageProperties"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.modules.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>