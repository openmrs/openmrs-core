<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Roles" otherwise="/login.htm" redirect="/admin/users/role.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Role.manage.title"/></h2>	

<spring:hasBindErrors name="role">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Role.role"/></td>
		<td>
			<spring:bind path="role.role">
				<c:if test="${role.role == null}"><input type="text" name="${status.expression}" value="${status.value}"></c:if>
				<c:if test="${!(role.role == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="role.description">
				<textarea name="description" rows="3" cols="50">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Role.parentRoles"/></td>
		<td>
			<i><spring:message code="Role.parentRoles.description" /></i><br/>
			<c:if test="${role.role == superuser}"><spring:message code="Role.superuser.hasAllRolesAndPrivileges"/></c:if>
			<c:if test="${role.role != superuser}">
				<openmrs:listPicker name="parentRoles" allItems="${parentRoles}" currentItems="${role.parentRoles}" contextPath="${pageContext.request.contextPath}" />
			</c:if>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Role.privileges"/></td>
		<td>
			<c:if test="${role.role == superuser}"><spring:message code="Role.superuser.hasAllRolesAndPrivileges"/></c:if>
			<c:if test="${role.role != superuser}">
				<openmrs:listPicker name="privileges" allItems="${privileges}" currentItems="${role.privileges}" contextPath="${pageContext.request.contextPath}" />
			</c:if>
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Role.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>


<%@ include file="/WEB-INF/template/footer.jsp" %>