<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Data Exports" otherwise="/login.htm" redirect="/admin/reports/rowPerObsDataExport.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="RowPerObsDataExport.manage.title"/></h2>	

<spring:message code="RowPerObsDataExport.help" /><br/><br/>

<a href="rowPerObsDataExport.form"><spring:message code="RowPerObsDataExport.add"/></a> <br />

<br />

<b class="boxHeader"><spring:message code="RowPerObsDataExport.list.title"/></b>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0">
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
			<th> </th>
		</tr>
		<c:forEach var="dataExport" items="${dataExportList}" varStatus="varStatus">
			<tr class="<c:choose><c:when test="${varStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
				<td valign="top"><input type="checkbox" name="dataExportId" value="${dataExport.reportObjectId}"></td>
				<td valign="top">
					<a href="dataExport.form?dataExportId=${dataExport.reportObjectId}">${dataExport.name}</a>
				</td>
				<td valign="top">${dataExport.description}</td>
				<td>
					<c:if test="${generatedDates[dataExport] != null}">
						<a href="${pageContext.request.contextPath}/dataExportServlet?dataExportId=${dataExport.reportObjectId}"><spring:message code="general.download"/></a>
						<span class="smallMessage">(${generatedSizes[dataExport]} <spring:message code="DataExport.generatedOn"/> <openmrs:formatDate date="${generatedDates[dataExport]}" type="long" />)</span>
					</c:if>
				</td>
			</tr>
		</c:forEach>
	</table>
	
	<input type="submit" value='<spring:message code="DataExport.generate"/>' name="action">
	&nbsp;
	<input type="submit" value='<spring:message code="DataExport.delete"/>' name="action" onclick="return confirm('Are you sure you want to DELETE these Exports?')">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>