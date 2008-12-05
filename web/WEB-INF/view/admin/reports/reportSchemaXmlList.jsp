<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Run Reports" otherwise="/login.htm" redirect="/admin/reports/reportSchemaXml.list" />

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.manage.title" /></h2>
<br/>
<spring:message code="Report.manage.new" />:&nbsp;
<a href="cohortReport.form"><spring:message code="Report.cohortReport.enter" /></a>
&nbsp;&nbsp;|&nbsp;&nbsp;
<a href="reportSchemaXml.form"><spring:message code="Report.schema.enter" /></a>
<br/>
<br/>

<div class="boxHeader">
	<b><spring:message code="Report.manage.choose" /></b>
</div>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0" width="98%">
		<tr>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th>&nbsp;</th>
		</tr>
		<c:forEach var="reportSchemaXml" items="${reportSchemaXmlList}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
				<td valign="top">
					<a href="cohortReport.form?reportId=${reportSchemaXml.reportSchemaId}">${reportSchemaXml.name}</a></td>
				<td valign="top">${reportSchemaXml.description}</td>
				<td valign="top" align="right">
					<a href="reportSchemaXml.form?reportSchemaId=${reportSchemaXml.reportSchemaId}"><spring:message code="Report.schema.editXml" /></a>
				</td>
			</tr>
		</c:forEach>
	</table>
</form>
