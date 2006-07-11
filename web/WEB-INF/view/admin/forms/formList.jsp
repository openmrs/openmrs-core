<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/form.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Form.manage" /></h2>	

<script type="text/javascript">
	function duplicate() {
		var id = document.getElementById('duplicateFormId').value;
		if (id == '')
			return false;
		window.location = "formEdit.form?duplicate=true&formId=" + id;
	}
</script>

<a href="formEdit.form"><spring:message code="Form.add" /></a>

<form style="padding: 0px; margin: 0px; display: inline;">
<spring:message code="Form.duplicate.or"/>:
	<select name="duplicateFormId" id="duplicateFormId">
		<option value=""><spring:message code="general.choose"/>...</option>
		<c:forEach var="form" items="${formList}">
			<option value="${form.formId}">${form.name} (v. ${form.version}.${form.build})</option>
		</c:forEach>
	</select>
	<input type="button" value="<spring:message code="Form.duplicate"/>" onclick="return duplicate()" />
</form>

<br /><br />

<b class="boxHeader">
	<spring:message code="Form.list.title" />
</b>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0">
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="Form.version" /> </th>
			<th> <spring:message code="Form.build" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th> <spring:message code="Form.published" /> </th>

		</tr>
		<c:forEach var="form" items="${formList}">
			<tr>
				<td valign="top" style="white-space: nowrap">
					<a href="formEdit.form?formId=${form.formId}"><spring:message code="Form.editProperties"/></a> | 
					<a href="formSchemaDesign.form?formId=${form.formId}"><spring:message code="Form.designSchema"/></a>
				</td>
				<td valign="top" style="white-space: nowrap">${form.name}</td>
				<td valign="top">${form.version}</td>
				<td valign="top">${form.build}</td>
				<td valign="top">${form.description}</td>
				<td valign="top"><c:if test="${form.published == true}"><spring:message code="general.yes"/></c:if></td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>