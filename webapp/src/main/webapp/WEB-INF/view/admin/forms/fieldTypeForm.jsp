<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Field Types" otherwise="/login.htm" redirect="/admin/forms/fieldType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="FieldType.edit" /></h2>

<spring:hasBindErrors name="fieldType">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form method="post" onSubmit="return validateForm()">
	<table>
		<tr>
			<td><openmrs:message code="general.name" /><span class="required">*</span></td>
			<td>
                <spring:bind path="fieldType.name">
				<input type="text" name="name" id="fieldTypeName" value="${fieldType.name}" size="35" onKeyUp="hideError('nameError');"/>
                <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
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
		<tr>
          <c:if test="${fieldType.fieldTypeId != null}">
             <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
             <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${fieldType.uuid}</sub></font></td>
         </c:if>
       </tr>
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