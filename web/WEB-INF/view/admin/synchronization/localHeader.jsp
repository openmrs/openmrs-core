<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Synchronization">
		<li <c:if test='<%= request.getRequestURI().contains("synchronizationStats") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/synchronization/synchronizationStats.list">
				<spring:message code="Synchronization.stats.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Synchronization">
		<li <c:if test='<%= request.getRequestURI().contains("synchronizationConfig") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/synchronization/synchronizationConfig.list">
				<spring:message code="Synchronization.config.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<li <c:if test='<%= request.getRequestURI().contains("synchronizationHelp") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/synchronization/synchronizationHelp.list">
			<spring:message code="Synchronization.help.title"/>
		</a>
	</li>
	<openmrs:extensionPoint pointId="org.openmrs.admin.maintenance.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test='${fn:endsWith(pageContext.request.requestURI, link.key)}'>class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>