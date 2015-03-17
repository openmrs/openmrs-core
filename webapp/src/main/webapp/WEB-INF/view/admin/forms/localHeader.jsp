<%@page import="org.openmrs.util.PrivilegeConstants"%>

<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="<%= PrivilegeConstants.MANAGE_FORMS %>">
		<li <c:if test='<%= request.getRequestURI().contains("forms/form") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/form.list">
				<openmrs:message code="Form.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit Forms">
		<li <c:if test='<%= request.getRequestURI().contains("fieldForm") || request.getRequestURI().contains("fieldList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/field.list">
				<openmrs:message code="Field.manage"/>
			</a>
		</li>
		<!-- commenting out link until page is implemented
		<li <c:if test='<%= request.getRequestURI().contains("fieldAnswers") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/fieldAnswer.list" class="retired">
				<openmrs:message code="FieldAnswer.manage"/>
			</a>
		</li>
		-->
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Field Types">
		<li <c:if test='<%= request.getRequestURI().contains("fieldType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/fieldType.list">
				<openmrs:message code="FieldType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="<%= PrivilegeConstants.MANAGE_FORMS %>">
		<li <c:if test='<%= request.getRequestURI().contains("auditField") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/auditField.form">
				<openmrs:message code="FormField.auditButton"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.forms.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>