<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Groups" otherwise="/login.htm" redirect="/admin/users/group.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Group.manage.title"/></h2>	

<spring:hasBindErrors name="group">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Group.group"/></td>
		<td>
			<spring:bind path="group.group">
				<c:if test="${group.group == null}"><input type="text" name="${status.expression}" value="${status.value}"></c:if>
				<c:if test="${!(group.group == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="group.description">
				<textarea name="description" rows="3" cols="50">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Group.roles"/></td>
		<td valign="top">
			<openmrs:listPicker name="roles" allItems="${roles}" currentItems="${group.roles}" contextPath="${pageContext.request.contextPath}" />
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Group.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>