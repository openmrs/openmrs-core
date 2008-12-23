<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage HL7Messages" otherwise="/login.htm" redirect="/admin/hl7/hl7Deleted.form" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<script type="text/javascript">

</script>

<h2><spring:message code="Hl7inQueue.header" /></h2>	

<div class="hl7DeletedForm" style="overflow:xscroll">
	<b class="boxHeader"><spring:message code="Hl7inQueue.queueForm.title" /></b>
	<div class="box">
		<form id="hl7DeletedForm" method="post">
			<div id="hl7DeletedListing">
				<c:if test="${fn:length(queueForm) == 0}">
					<i> &nbsp; There are no deleted messages</i><br/>
				</c:if>
				<c:if test="${fn:length(queueForm) > 0}">
				<table cellpadding="5" cellspacing="0">
					<tr>
						<th></th>
						<th><spring:message code="Hl7inQueue.queueForm.source.header" /></th>
						<th><spring:message code="Hl7inQueue.queueForm.data.header" /></th>
						<th><spring:message code="Hl7inQueue.queueForm.dateDeleted" /></th>
					</tr>
					<c:forEach var="queue" items="${queueForm}">
						<tr>
							<td valign="top"><input type="checkbox" name="queueId" value="${queue.HL7InArchiveId}"></td>	
							<td valign="top">${queue.HL7Source.name}</td>
							<td valign="top">
								<div style="overflow:auto">
									<pre>${queue.HL7Data}</pre>
								</div>							
							</td>
							<td valign="top">${queue.dateCreated }</td>	
						</tr>
					</c:forEach>
					<tr>
						<td colspan="6">
							<input type="submit" value="<spring:message code="Hl7inQueue.queueForm.restore"/>" name="restore">
						</td>
					</tr>
				</table>
				</c:if>
			</div>
		</form>
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>