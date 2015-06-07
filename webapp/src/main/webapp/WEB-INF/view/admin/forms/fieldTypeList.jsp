<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Field Types" otherwise="/login.htm" redirect="/admin/forms/fieldType.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="FieldType.manage" /></h2>	

<a href="fieldType.form"><openmrs:message code="FieldType.add" /></a> <br />

<br />

<b class="boxHeader">
	<openmrs:message code="FieldType.list.title" />
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <openmrs:message code="general.name" /> </th>
			<th> <openmrs:message code="FieldType.isSet" /> </th>
			<th> <openmrs:message code="general.description" /></th>
		</tr>
		<c:forEach var="fieldType" items="${fieldTypeList}">
			<tr>
				<td valign="top"><input type="checkbox" name="fieldTypeId" value="${fieldType.fieldTypeId}"></td>
				<td valign="top">
					<a href="fieldType.form?fieldTypeId=${fieldType.fieldTypeId}">
						<c:out value="${fieldType.name}"/>
					</a>
				</td>
				<td valign="top" align="center"><c:if test="${fieldType.isSet == true}"><openmrs:message code="general.yes"/></c:if></td>
				<td valign="top"><c:out value="${fieldType.description}"/></td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<openmrs:message code="FieldType.delete"/>" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>