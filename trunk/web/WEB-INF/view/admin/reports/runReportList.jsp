<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<openmrs:require privilege="Run Reports" otherwise="/login.htm" redirect="/admin/reports/runReport.list" />

<h2><spring:message code="Report.list.title" /></h2>

<br/>

<c:if test="${fn:length(reports) == 0}">
	<spring:message code="Report.noReports" />
</c:if>

<c:if test="${fn:length(reports) != 0}">
    <div class="boxHeader">
        <b><spring:message code="Report.list.pick" /></b>
    </div>
    <div class="box">
	<table cellpadding="2" cellspacing="0">
		<tr>
			<th></th>
			<th style="padding-left: 3em"><spring:message code="Report.description"/></th>
			<th style="padding-left: 3em"><spring:message code="Report.list.lastRun"/></th>
		</tr>
		<c:forEach var="report" items="${reports}" varStatus="status">
			<tr valign="baseline" class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
				<td>
					<a href="runReport.form?reportId=${report.reportSchemaId}">${report.name}</a></div>
				</td>
				<td style="padding-left: 3em">
					<small><i>${report.description}</i></small>
				</td>
				<td style="padding-left: 3em">
					(cached last run report should go here)
				</td>
			</tr>
		</c:forEach>
	</table>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>