<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View HL7 Inbound Queue" otherwise="/login.htm" redirect="/admin/hl7/hl7InQueue.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Hl7inQueue.header" /></h2>	

<div class="hl7inQueueList" style="overflow:xscroll">
	<b class="boxHeader"><spring:message code="Hl7inQueue.queueList.title" /></b>
	<div class="box">
		<form id="hl7inQueueListForm" method="post">
			<div id="hl7QueueListing">
				<c:if test="${fn:length(queueList) == 0}">
					<i> &nbsp; There are no queued messages</i><br/>
				</c:if>
				<c:if test="${fn:length(queueList) > 0}">
				<table cellpadding="5" cellspacing="0">
					<tr>
						<th></th>
						<th><spring:message code="Hl7inQueue.queueList.source.header" /></th>
						<th><spring:message code="Hl7inQueue.queueList.data.header" /></th>
						<th><spring:message code="Hl7inQueue.queueList.state.header" /></th>
						<th><spring:message code="Hl7inQueue.queueList.errorMessage.header" /></th>
						<th><spring:message code="Hl7inQueue.queueList.dateCreated" /></th>
					</tr>
					<c:forEach var="queue" items="${queueList}">
						<tr>
							<td valign="top"><input type="checkbox" name="queueId" value="${queue.HL7InQueueId}"></td>	
							<td valign="top">${queue.HL7Source.name}</td>
							<td valign="top">
								<div style="overflow:auto">
									<pre>${queue.HL7Data}</pre>
								</div>							
							</td>
							<td valign="top"><spring:message code="Hl7inQueue.status.${queue.messageState}" /></td>
							<td valign="top">${queue.errorMessage}</td>
							<td valign="top">${queue.dateCreated }</td>	
						</tr>
					</c:forEach>
					<tr>
						<td colspan="6">
							<input type="submit" value="<spring:message code="Hl7inQueue.queueList.delete"/>" name="delete">
						</td>
					</tr>
				</table>
				</c:if>
			</div>
		</form>
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>