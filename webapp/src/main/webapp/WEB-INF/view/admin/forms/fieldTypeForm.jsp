<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Field Types" otherwise="/login.htm" redirect="/admin/forms/fieldType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="FieldType.edit" /></h2>

<form method="post" onSubmit="return validateForm()">
	<table>
		<tr>
			<td><openmrs:message code="general.name" /></td>
			<td>
				<input type="text" name="name" id="fieldTypeName" value="${fieldType.name}" size="35" onKeyUp="hideError('nameError');"/>
				<span class="error" id="nameError"><openmrs:message code="error.name"/></span>
			</td>
		</tr>
		<tr>
			<td><openmrs:message code="FieldType.isSet" /></td>
			<input type="hidden" name="_isSet" value="" />
			<td><input type="checkbox" name="isSet" value="true" <c:if test="${fieldType.isSet == true}">checked</c:if> /></td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.description" /></td>
			<td><textarea name="description" rows="3" cols="40">${fieldType.description}</textarea></td>
		</tr>
		<c:if test="${!(orderType.creator == null)}">
			<tr>
				<td><openmrs:message code="general.createdBy" /></td>
				<td>
					<c:out value="${orderType.creator.personName}" /> -
					<openmrs:formatDate date="${orderType.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
	<br />
	<input type="submit" value="<openmrs:message code="FieldType.save"/>">
</form>

		<script type="text/javascript"><!--
			hideError("nameError");
			function validateForm() {
				var name = document.getElementById("fieldTypeName");
				var result = true;
				if (name.value.trim() == "") {
					showError("nameError"); 
					result = false;
				}
				return result;
			}
		--></script>

<%@ include file="/WEB-INF/template/footer.jsp" %>