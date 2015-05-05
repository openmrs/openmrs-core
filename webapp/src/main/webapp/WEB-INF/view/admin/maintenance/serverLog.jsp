<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Server Log" otherwise="/login.htm" redirect="/admin/maintenance/serverLog.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<style>
 .descriptionBox {
 	border-color: transparent;
 	border-width: 1px;
 	overflow-y: auto;
 	background-color: transparent;
 	padding: 1px;
 	height: 2.7em;
 }
 td.description {
 	padding-top: 0px;
 }
 #buttonsAtBottom {
 	padding: 5px;
 }
</style>

<h2><openmrs:message code="ServerLog.view.title"/></h2>	

<c:choose>
	<c:when test="${empty logLines}">
		<table cellpadding="4" cellspacing="0">
			<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow" }'>
				<td>No logs to display</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
	
		<table cellpadding="4" cellspacing="0">
			<c:forEach var="logLine" varStatus="status" items="${logLines}">
				<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow" }'>
					<td>${fn:escapeXml(logLine)}</td>
				</tr>
			</c:forEach>
		</table>
		
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>
