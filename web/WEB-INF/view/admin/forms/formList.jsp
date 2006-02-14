<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Forms" otherwise="/login.htm" redirect="/admin/forms/form.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Form.manage" /></h2>	

<a href="form.form"><spring:message code="Form.add" /></a> <br />

<br />

<b class="boxHeader">
	<spring:message code="Form.list.title" />
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="Form.version" /> </th>
			<th> <spring:message code="Form.build" /> </th>
			<th> <spring:message code="general.description" /></th>

		</tr>
		<c:forEach var="form" items="${formList}">
			<tr>
				<td valign="top">
					<a href="formEdit.form?formId=${form.formId}"><spring:message code="Form.editProperties"/></a> | 
					<a href="formDesign.form?formId=${form.formId}"><spring:message code="Form.designSchema"/></a>
				</td>
				<td valign="top">${form.name}</td>
				<td valign="top">${form.version}</td>
				<td valign="top">${form.build}</td>
				<td valign="top">${form.description}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>