<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Providers,Edit Providers,Purge Providers,View Providers">
		<li <c:if test='<%= request.getRequestURI().contains("provider/index") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/provider/index.htm">
				<spring:message code="Provider.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Provider Attribute Types,View Provider Attribute Types,Purge Provider Attribute Types">
		<li <c:if test='<%= request.getRequestURI().contains("provider/providerAttributeTypeList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/provider/providerAttributeType.list">
				<spring:message code="ProviderAttributeType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>	
	<openmrs:extensionPoint pointId="org.openmrs.admin.provider.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>