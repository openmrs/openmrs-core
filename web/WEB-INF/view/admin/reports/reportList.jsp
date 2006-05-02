<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/admin/reports/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.manage.title"/></h2>	

<a href="report.form"><spring:message code="Report.add"/></a> <br />

<br />

<b class="boxHeader"><spring:message code="Report.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
		</tr>
		<c:forEach var="report" items="${reportList}">
			<tr>
				<td valign="top"><input type="checkbox" name="reportId" value="${report.reportId}"></td>
				<td valign="top">
					<a href="report.form?reportId=${report.reportId}">
					   ${report.name}
					</a>
				</td>
				<td valign="top">${report.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Report.delete"/>" name="action">
</form>
<%@ include file="/WEB-INF/template/footer.jsp" %>