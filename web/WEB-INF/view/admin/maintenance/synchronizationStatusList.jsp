<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/maintenance/synchronizationStatus.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="SynchronizationStatus.title"/></h2>

<form id="syncCreateTx" action="synchronizationStatus.list" method="post">
	<input type="submit" value='<spring:message code="SynchronizationStatus.createTx"/>'/>
	<input type="hidden" name="action" value="createTx"/>
</form>

<br/>

<table>
	<thead>
		<tr>
			<th><spring:message code="SynchronizationStatus.guid" /></th>
			<th><spring:message code="SynchronizationStatus.timestamp" /></th>
			<th><spring:message code="SynchronizationStatus.retryCount" /></th>
			<th><spring:message code="SynchronizationStatus.state" /></th>
		</tr>
	</thead>
	<tbody id="globalPropsList">
		<c:forEach var="synchronizationStatusList" items="${synchronizationStatusList}" varStatus="status">
			<tr>
				<td>${synchronizationStatusList.guid}</td>
				<td>${synchronizationStatusList.timestamp}</td>
				<td>${synchronizationStatusList.retryCount}</td>
				<td>${synchronizationStatusList.state}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %>
