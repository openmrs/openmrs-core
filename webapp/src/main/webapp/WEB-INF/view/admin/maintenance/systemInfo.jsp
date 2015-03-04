<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Administration Functions" otherwise="/login.htm"
	redirect="/admin/maintenance/systemInfo.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<table cellpadding="4" cellspacing="0" border="0" width="50%">
<c:forEach items="${systemInfo}" var="var" varStatus="status">
		<tr>
			<td colspan="2">
				<br/>
				<h3><openmrs:message code="${ var.key }" /></h3>
			</td>
		</tr>
		<tr>
			<th align="left"><openmrs:message code="SystemInfo.name" /></th>
			<th align="left"><openmrs:message code="SystemInfo.value" /></th>
		</tr>
		<c:forEach items="${var.value}" var="info" varStatus="status">
			<tr class='${status.index % 2 == 0 ? "evenRow" : "oddRow"}'>
				<td nowrap><openmrs:message code="${ info.key }" /></td>
				<td>${ info.value }</td>
			</tr>
		</c:forEach>
</c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>