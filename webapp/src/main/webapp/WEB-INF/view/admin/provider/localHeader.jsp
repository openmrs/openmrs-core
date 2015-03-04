<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Providers,Purge Providers,View Providers">
		<li <c:if test='<%= request.getRequestURI().contains("provider/index") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/provider/index.htm">
				<openmrs:message code="Provider.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Provider Attribute Types,View Provider Attribute Types,Purge Provider Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("provider/providerAttributeTypeList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/provider/providerAttributeType.list">
				<openmrs:message code="ProviderAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>	
	<openmrs:extensionPoint pointId="org.openmrs.admin.provider.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>