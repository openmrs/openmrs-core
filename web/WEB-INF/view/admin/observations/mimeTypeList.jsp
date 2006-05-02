<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Mime Types" otherwise="/login.htm" redirect="/admin/observations/mimeType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="MimeType.manage.title"/></h2>	

<a href="mimeType.form"><spring:message code="MimeType.add"/></a> <br />

<br />

<b class="boxHeader"><spring:message code="MimeType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="MimeType.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
		</tr>
		<c:forEach var="mimeType" items="${mimeTypeList}">
			<tr>
				<td valign="top"><input type="checkbox" name="mimeTypeId" value="${mimeType.mimeTypeId}"></td>
				<td valign="top">
					<a href="mimeType.form?mimeTypeId=${mimeType.mimeTypeId}">
					   ${mimeType.mimeType}
					</a>
				</td>
				<td valign="top">${mimeType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="MimeType.delete"/>" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>