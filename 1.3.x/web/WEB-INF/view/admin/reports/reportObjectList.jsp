<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Report Objects" otherwise="/login.htm" redirect="/admin/reports/reportObject.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ReportObject.manage.title"/></h2>	

<a href="reportObject.form"><spring:message code="ReportObject.add"/></a> <br />

<br />

<b class="boxHeader"><spring:message code="ReportObject.list.title"/></b>
<c:if test="${reportObjectList.size > 0}">
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.type" /> </th>
			<th> <spring:message code="general.subType" /> </th>
			<th> <spring:message code="general.description" /> </th>
		</tr>
		<c:forEach var="reportObject" items="${reportObjectList.reportObjects}">
			<tr>
				<td valign="top"><input type="checkbox" name="reportObjectId" value="${reportObject.reportObjectId}"></td>
				<td valign="top">
					<a href="reportObject.form?reportObjectId=${reportObject.reportObjectId}">
					   ${reportObject.name}
					</a>
				</td>
				<td valign="top">${reportObject.type}</td>
				<td valign="top">${reportObject.subType}</td>
				<td valign="top">
				<% try { %>
					${reportObject.description}</td>
				<% } catch (Exception ex) { %>
					<font color="red">EXCEPTION!</font>
				<% } %>
				</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="ReportObject.delete"/>" name="action">
</form>
</c:if>
<c:if test="${reportObjectList.size == 0}">
	&nbsp;&nbsp;<spring:message code="ReportObject.list.nonefound"/>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp" %>