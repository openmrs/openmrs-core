<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Field Types" otherwise="/login.htm" redirect="/admin/forms/fieldType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="FieldType.edit" /></h2>

<form method="post">
	<table>
		<tr>
			<td><spring:message code="general.name" /></td>
			<td><input type="text" name="name" value="${fieldType.name}" size="35" /></td>
		</tr>
		<tr>
			<td><spring:message code="FieldType.isSet" /></td>
			<input type="hidden" name="_isSet" value="" />
			<td><input type="checkbox" name="isSet" value="true" <c:if test="${fieldType.isSet == true}">checked</c:if> /></td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.description" /></td>
			<td><textarea name="description" rows="3" cols="40">${fieldType.description}</textarea></td>
		</tr>
		<c:if test="${!(orderType.creator == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${orderType.creator.personName} -
					<openmrs:formatDate date="${orderType.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
	<br />
	<input type="submit" value="<spring:message code="FieldType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>