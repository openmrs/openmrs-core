<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.htm" redirect="/admin/users/role.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Role.manage.title"/></h2>	

<spring:hasBindErrors name="role">
	<spring:message code="fix.error"/>
	<br />
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
		<td valign="top"><spring:message code="Role.privileges"/></td>
		<td>
			<openmrs:listPicker name="privileges" allItems="${privileges}" currentItems="${role.privileges}" contextPath="${pageContext.request.contextPath}" />
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Role.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>