<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/form.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Form.manage" /></h2>	

<openmrs:globalProperty key="forms.locked" var="formsLocked"/>
<c:if test="${formsLocked != 'true'}"> <a href="formEdit.form"> <openmrs:message code="Form.add" /> </a> </c:if>

<form style="padding: 0px; margin: 0px; display: inline;">
<c:if test="${formsLocked != 'true'}"> <openmrs:message code="Form.duplicate.or"/>:</c:if>
	<select name="duplicateFormId" id="duplicateFormId" <c:if test="${formsLocked == 'true'}"> disabled </c:if>>
		<option value=""><openmrs:message code="general.choose"/>...</option>
		<c:forEach var="form" items="${formList}">
			<option value="${form.formId}"><c:out value="${form.name}"/> (v. ${form.version}.${form.build})</option>
		</c:forEach>
	</select>
	<input type="button" value="<openmrs:message code="Form.duplicate"/>" onclick="return duplicate()" <c:if test="${formsLocked == 'true'}"> disabled </c:if>/>
</form>

<openmrs:extensionPoint pointId="org.openmrs.admin.forms.formListHeader" type="html">
	<c:forEach items="${extension.links}" var="link">
		| <a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
	</c:forEach>
</openmrs:extensionPoint>

<br /><br />

<div class="boxHeader">
	<span style="float: right">
		<a href="#" id="showRetired" onClick="return toggleRowVisibilityForClass('formTable', 'voided');"><openmrs:message code="general.toggle.retired"/></a>
	</span>
	<b><openmrs:message code="Form.list.title" /></b>
</div>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0" id="formTable" width="98%">
		<tr>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="Form.version" /> </th>
			<th> <openmrs:message code="Form.build" /> </th>
			<th> <openmrs:message code="general.description" /> </th>
			<th> <openmrs:message code="Form.published" /> </th>

		</tr>
		<c:forEach var="form" items="${formList}" varStatus="status">
			<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"} ${form.retired ? "voided" : ""}'>
				<td valign="top" style="white-space: nowrap"><a href="formEdit.form?formId=${form.formId}"><c:out value="${form.name}"/></a></td>
				<td valign="top">${form.version}</td>
				<td valign="top">${form.build}</td>
				<td valign="top"><c:out value="${form.description}"/></td>
				<td valign="top"><c:if test="${form.published == true}"><openmrs:message code="general.yes"/></c:if></td>
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