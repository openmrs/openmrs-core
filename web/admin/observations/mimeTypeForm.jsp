<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Observations" otherwise="/login.jsp" redirect="/admin/observations/mimeType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="MimeType.title"/></h2>

<spring:hasBindErrors name="mimeType">
	<spring:message code="error.fix"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><spring:message code="MimeType.name"/></td>
		<td>
			<spring:bind path="mimeType.mimeType">
				<input type="text" name="mimeType" value="${status.value}" size="35" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="mimeType.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
</table>
<c:if test="${mimeType.mimeTypeId != null}">
<input type="hidden" name="mimeTypeId:int" value="<c:out value="${mimeType.mimeTypeId}"/>">
</c:if>
<br />
<input type="submit" value="<spring:message code="MimeType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>