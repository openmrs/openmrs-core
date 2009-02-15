<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/form.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Form.manage" /></h2>	

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

<openmrs:extensionPoint pointId="org.openmrs.admin.forms.formListHeader" type="html">
	<c:forEach items="${extension.links}" var="link">
		| <a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
	</c:forEach>
</openmrs:extensionPoint>

<br /><br />

<div class="boxHeader">
	<span style="float: right">
		<a href="#" id="showRetired" onClick="return toggleRowVisibilityForClass('formTable', 'voided');"><spring:message code="general.toggle.retired"/></a>
	</span>
	<b><spring:message code="Form.list.title" /></b>
</div>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0" id="formTable" width="98%">
		<tr>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="Form.version" /> </th>
			<th> <spring:message code="Form.build" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th> <spring:message code="Form.published" /> </th>

		</tr>
		<c:forEach var="form" items="${formList}" varStatus="status">
			<tr class="<c:if test="${form.retired}">voided </c:if><c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
				<td valign="top" style="white-space: nowrap"><a href="formEdit.form?formId=${form.formId}">${form.name}</a></td>
				<td valign="top">${form.version}</td>
				<td valign="top">${form.build}</td>
				<td valign="top">${form.description}</td>
				<td valign="top"><c:if test="${form.published == true}"><spring:message code="general.yes"/></c:if></td>
			</tr>
		</c:forEach>
	</table>
</form>

<script type="text/javascript">
	function duplicate() {
		var id = document.getElementById('duplicateFormId').value;
		if (id == '')
			return false;
		window.location = "formEdit.form?duplicate=true&formId=" + id;
	}
	
	toggleRowVisibilityForClass("formTable", "voided");
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>