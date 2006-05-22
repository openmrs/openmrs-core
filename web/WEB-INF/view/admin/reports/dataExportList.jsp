<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Data Exports" otherwise="/login.htm" redirect="/admin/dataExports/dataExport.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="DataExport.manage.title"/></h2>	

<a href="dataExport.form"><spring:message code="DataExport.add"/></a> <br />

<br />

<b class="boxHeader"><spring:message code="DataExport.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th> </th>
		</tr>
		<c:forEach var="dataExport" items="${dataExportList}">
			<tr>
				<td valign="top"><input type="checkbox" name="dataExportId" value="${dataExport.reportObjectId}"></td>
				<td valign="top">
					<a href="dataExport.form?dataExportId=${dataExport.reportObjectId}">
					   ${dataExport.name}
					</a>
				</td>
				<td valign="top">${dataExport.description}</td>
				<td>
					<a href="${pageContext.request.contextPath}/dataExportServlet?dataExportId=${dataExport.reportObjectId}">
						<spring:message code="DataExport.viewGenerate"/>
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="DataExport.delete"/>" name="action">
</form>
<%@ include file="/WEB-INF/template/footer.jsp" %>