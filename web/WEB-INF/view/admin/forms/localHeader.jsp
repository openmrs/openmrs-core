<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Forms,Edit Forms,Delete Forms,View Forms">
		<li <c:if test='<%= request.getRequestURI().contains("forms/form") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/form.list" class="retired">
				<spring:message code="Form.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit Forms">
		<li <c:if test='<%= request.getRequestURI().contains("forms/fields") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/field.list" class="retired">
				<spring:message code="Field.manage"/>
			</a>
		</li>
		<!-- commenting out link until page is implemented
		<li <c:if test='<%= request.getRequestURI().contains("fieldAnswers") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/fieldAnswer.list" class="retired">
				<spring:message code="FieldAnswer.manage"/>
			</a>
		</li>
		-->
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Field Types">
		<li <c:if test='<%= request.getRequestURI().contains("fieldType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/forms/fieldType.list">
				<spring:message code="FieldType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.forms.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>