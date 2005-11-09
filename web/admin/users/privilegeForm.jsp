<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Users" otherwise="/login.jsp" redirect="/admin/users/privilege.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Privilege.manage.title"/></h2>	

<form method="post">
<table>
	<tr>
		<td><spring:message code="Privilege.privilege"/></td>
		<td>
			<spring:bind path="privilege.privilege">
				<c:if test="${privilege.privilege == null}"><input type="text" name="${status.expression}" value="${status.value}"></c:if>
				<c:if test="${!(privilege.privilege == null)}">${status.value}</c:if>				
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.description"/></td>
		<td>
			<spring:bind path="privilege.description">
				<input type="text" name="description" value="${status.value}"/>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Privilege.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>