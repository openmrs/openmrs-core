<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View FormEntry Queue" otherwise="/login.htm" redirect="/admin/formentry/formEntryQueue.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2>
	<spring:message code="FormEntryQueue.title" />
</h2>

<form method="post" action="${pageContext.request.contextPath}/formEntryQueueDownload">
	<b class="boxHeader"><spring:message code="FormEntryQueue.multiple" />:</b>
	<div class="box">
		<table>
			<tr>
				<td><spring:message code="general.start" /></td>
				<td>
					<select name="startId">
						<c:forEach items="${formEntryQueueList}" var="entry">
							<option value="${entry.formEntryQueueId}">${entry.formEntryQueueId}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.end" /></td>
				<td>
					<select name="endId">
						<c:forEach items="${formEntryQueueList}" var="entry">
							<option value="${entry.formEntryQueueId}">${entry.formEntryQueueId}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
		<input type="submit" value='<spring:message code="general.download" />' />
	</div>
</form>

<br/>

<div>
	<b class="boxHeader">
		<spring:message code="FormEntryQueue.select" />
	</b>
	<div class="box">
		<table cellspacing="0" cellpadding="1" width="100%">
			<thead>
				<tr>
					<th><spring:message code="general.id" /></th>
					<th></th>
					<th><spring:message code="FormEntryQueue.status" /></th>
					<th><spring:message code="FormEntryQueue.date" /></th>
					<th><spring:message code="FormEntryQueue.errorMsg" /></th>
					<th><spring:message code="general.createdBy" /></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${formEntryQueueList}" var="entry">
					<tr>
						<td>${entry.formEntryQueueId}</td>
						<td><a href="formEntryQueue.form?formEntryQueueId=${entry.formEntryQueueId}"><spring:message code="general.download" /></a></td>
						<td><spring:message code="FormEntryQueue.status.${entry.status}"/></td>
						<td>${entry.dateProcessed}</td>
						<td>${entry.errorMsg}</td>
						<td>
							${entry.creator.firstName} ${entry.creator.lastName} -
							<openmrs:formatDate date="${entry.dateCreated}" type="long" />
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<script type="text/javascript">

	var fieldListing	= document.getElementById("fieldListing");
	var searchBox		= document.getElementById("searchBox");
	
	showSearch();
	
	<request:existsParameter name="fieldId">
		var fields = new Array();
		var fields[0] = new Object();
		fields[0].fieldId = request.getAttribute("fieldId");
		onSelect(fields);
	</request:existsParameter>
	
	<request:existsParameter name="phrase">
		searchBox.value = '<request:parameter name="phrase" />';
	</request:existsParameter>
	
	// creates back button functionality
	if (searchBox.value != "")
		searchBoxChange("fieldTableBody", searchBox, null, 0, 0);
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
