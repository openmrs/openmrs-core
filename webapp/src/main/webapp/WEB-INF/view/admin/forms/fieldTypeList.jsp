<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Field Types" otherwise="/login.htm" redirect="/admin/forms/fieldType.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="FieldType.manage" /></h2>	

<a href="fieldType.form"><spring:message code="FieldType.add" /></a> <br />

<br />

<b class="boxHeader">
	<spring:message code="FieldType.list.title" />
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="FieldType.isSet" /> </th>
			<th> <spring:message code="general.description" /></th>
		</tr>
		<c:forEach var="fieldType" items="${fieldTypeList}">
			<tr>
				<td valign="top"><input type="checkbox" name="fieldTypeId" value="${fieldType.fieldTypeId}"></td>
				<td valign="top">
					<a href="fieldType.form?fieldTypeId=${fieldType.fieldTypeId}">
					   ${fieldType.name}
					</a>
				</td>
				<td valign="top" align="center"><c:if test="${fieldType.isSet == true}"><spring:message code="general.yes"/></c:if></td>
				<td valign="top">${fieldType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="FieldType.delete"/>" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>