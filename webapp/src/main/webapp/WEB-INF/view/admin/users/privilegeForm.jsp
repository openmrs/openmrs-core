<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Privileges" otherwise="/login.htm" redirect="/admin/users/privilege.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Privilege.manage.title"/></h2>	

<spring:hasBindErrors name="privilege">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Privilege.privilege"/></td>
		<td>
			<spring:bind path="privilege.privilege">
				<c:if test="${status.value == null || status.value == \"\"}"><input type="text" name="${status.expression}" id="priv" value="${status.value}"></c:if>
				<c:if test="${!(status.value == null)}">${status.value}</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="privilege.description">
				<textarea name="description" rows="3" cols="50">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</table>

<input type="submit" value="<spring:message code="Privilege.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>