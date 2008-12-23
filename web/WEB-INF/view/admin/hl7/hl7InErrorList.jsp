<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View HL7 Inbound Queue" otherwise="/login.htm" redirect="/admin/hl7/hl7InError.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Hl7inError.header" /></h2>	

<div class="hl7inErrorList" style="overflow:xscroll">
	<b class="boxHeader"><spring:message code="Hl7inError.errorList.title" /></b>
	<div class="box">
		<form id="hl7inErrorListForm" method="post">
			<div id="hl7ErrorListing">
				<c:if test="${fn:length(errorList) == 0}">
					<i> &nbsp; There are no erred messages</i><br/>
				</c:if>
				<c:if test="${fn:length(errorList) > 0}">
				<table cellpadding="5" cellspacing="0">
					<tr>
						<th></th>
						<th><spring:message code="Hl7inError.errorList.source.header" /></th>
						<th><spring:message code="Hl7inError.errorList.data.header" /></th>
						<th><spring:message code="Hl7inError.errorList.error.header" /></th>
						<th><spring:message code="Hl7inError.errorList.errorDetails.header" /></th>
						<th><spring:message code="Hl7inError.errorList.dateCreated.header" /></th>
					</tr>
					<c:forEach var="error" items="${errorList}">
						<tr>
							<td valign="top"><input type="checkbox" name="errorId" value="${error.HL7InErrorId}"></td>
							<td valign="top">${error.HL7Source.name}</td>
							<td valign="top">
								<div style="overflow:auto">
									<pre>${error.HL7Data}</pre>
								</div>							
							</td>
							<td valign="top">${error.error}</td>
							<td valign="top">${error.errorDetails}</td>	
							<td valign="top">${error.dateCreated }</td>	
						</tr>
					</c:forEach>
					<tr>
						<td colspan="6">
							<input type="submit" value="<spring:message code="Hl7inError.errorList.restore"/>" name="restore">
						</td>
					</tr>
				</table>
				</c:if>
			</div>
		</form>
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>