<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Implementation Id">
		<li <c:if test='<%= request.getRequestURI().contains("implementationid") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/implementationid.form">
				<openmrs:message code="ImplementationId.set"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Audit">
		<li <c:if test='<%= request.getRequestURI().contains("systemInfo") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/systemInfo.htm">
				<openmrs:message code="SystemInfo.overview"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Patients">
		<li <c:if test='<%= request.getRequestURI().contains("quickReport") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/quickReport.htm">
				<openmrs:message code="QuickReport.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Global Properties">
		<li <c:if test='<%= request.getRequestURI().contains("settings") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/settings.list">
				<openmrs:message code="Settings.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Global Properties">
		<li <c:if test='<%= request.getRequestURI().contains("globalProps") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/globalProps.form">
				<openmrs:message code="GlobalProperty.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Server Log">
		<li <c:if test='<%= request.getRequestURI().contains("serverLog") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/serverLog.form">
				<openmrs:message code="ServerLog.view"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Database Changes">
		<li <c:if test='<%= request.getRequestURI().contains("databaseChangesInfo") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/databaseChangesInfo.list">
				<openmrs:message code="DatabaseChangesInfo.overview"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Global Properties">
		<li <c:if test='<%= request.getRequestURI().contains("localesAndThemes") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/localesAndThemes.form">
				<openmrs:message code="LocalesAndThemes.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Current Users">
		<li <c:if test='<%= request.getRequestURI().contains("currentUsers") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/maintenance/currentUsers.list">
				<openmrs:message code="ViewCurrentUsers.list"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.maintenance.localHeader" type="html">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>
</ul>