<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Synchronization Status" otherwise="/login.htm" redirect="/admin/synchronization/synchronizationStatus.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRSynchronizationService.js" />

<h2><spring:message code="Synchronization.history.title"/></h2>

<script language="JavaScript">
	<!--

		function showHideDiv(id) {
			var div = document.getElementById(id);
			if ( div ) {
				if ( div.style.display != "none" ) {
					div.style.display = "none";
				} else { 
					div.style.display = "";
				}
			}
		}
			
	-->
</script>

<b class="boxHeader"><spring:message code="Synchronization.changes.all"/></b>
<div class="box">
	<table id="syncChangesTable" cellpadding="5" cellspacing="0">
		<thead>
			<tr>
				<th><spring:message code="SynchronizationStatus.itemTypeAndGuid" /></th>
				<th colspan="2" style="text-align: center;"><spring:message code="SynchronizationStatus.timestamp" /></th>
				<%--<th nowrap style="text-align: center;"><spring:message code="SynchronizationStatus.itemState" /></th>--%>
				<th nowrap style="text-align: center;"><spring:message code="SynchronizationStatus.recordState" /></th>
				<th nowrap style="text-align: center;"><spring:message code="SynchronizationStatus.retryCount" /></th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:if test="${not empty synchronizationHistoryList}">
				<c:set var="bgStyle" value="eee" />
				<c:forEach var="syncRecord" items="${synchronizationHistoryList}" varStatus="status">
					<c:forEach var="syncItem" items="${syncRecord.items}" varStatus="itemStatus">
						<tr>
							<td valign="middle" nowrap style="background-color: #${bgStyle};">
								<b>${itemTypes[syncItem.key.keyValue]}</b>
								<%--<c:if test="${not empty itemInfo[syncItem.key.keyValue]}">(${itemInfo[syncItem.key.keyValue]})</c:if></b>--%>
								<br>
								(${itemGuids[syncItem.key.keyValue]})
							</td>
							<td valign="middle" nowrap style="background-color: #${bgStyle};" align="right">
								<spring:message code="Synchronization.item.state_${syncItem.state}" /> -</td>
							<td valign="middle" nowrap style="background-color: #${bgStyle};" align="left"><openmrs:formatDate date="${syncRecord.timestamp}" format="${syncDateDisplayFormat}" /></td>
							<td valign="middle" nowrap style="background-color: #${bgStyle};" align="center">
								<span class="sync${syncRecord.state}"><spring:message code="Synchronization.record.state_${syncRecord.state}" /></span></td>
							<td valign="middle" nowrap style="background-color: #${bgStyle};" align="center">${syncRecord.retryCount}</td>
							<td valign="middle" style="background-color: #${bgStyle};"><span id="message_${syncItem.key.keyValue}"></span></td>
						</tr>
						<c:choose>
	
							<c:when test="${bgStyle == 'eee'}"><c:set var="bgStyle" value="fff" /></c:when>
							<c:otherwise><c:set var="bgStyle" value="eee" /></c:otherwise>
	
						</c:choose>
					</c:forEach>
				</c:forEach>
			</c:if>
			<c:if test="${empty synchronizationHistoryList}">
				<td colspan="5" align="left">
					<i><spring:message code="SynchronizationHistory.noItems" /></i>
				</td>
			</c:if>
		</tbody>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
