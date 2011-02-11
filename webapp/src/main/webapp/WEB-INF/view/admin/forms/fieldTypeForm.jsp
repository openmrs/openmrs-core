<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Field Types" otherwise="/login.htm" redirect="/admin/forms/fieldType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="FieldType.edit" /></h2>

<form method="post">
	<table>
		<spring:nestedPath path="fieldType">
			<openmrs:portlet url="localizedName" id="localizedNameLayout" /> 
		</spring:nestedPath>
		<tr>
			<td><spring:message code="FieldType.isSet" /></td>
			<input type="hidden" name="_isSet" value="" />
			<td><input type="checkbox" name="isSet" value="true" <c:if test="${fieldType.isSet == true}">checked</c:if> /></td>
		</tr>
		<spring:nestedPath path="fieldType">
			<openmrs:portlet url="localizedDescription" id="localizedDescriptionLayout" /> 
		</spring:nestedPath>
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