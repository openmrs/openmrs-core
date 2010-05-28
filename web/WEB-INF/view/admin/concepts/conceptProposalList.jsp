<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Concept Proposals" otherwise="/login.htm" redirect="/admin/concepts/conceptProposal.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	function selectProposal(pid) {
		document.location="conceptProposal.form?conceptProposalId=" + pid;
	}
	function mouseOver(row) {
		if (row.className.indexOf("searchHighlight") == -1)
			row.className = "searchHighlight " + row.className;
	}
	function mouseOut(row) {
		var c = row.className;
		row.className = c.substring(c.indexOf(" ") + 1, c.length);
	}
	function updateList() {
		var url = "conceptProposal.list?";
		url += "includeCompleted=" + document.getElementById('includeCompleted').checked;
		
		if (document.getElementById('orderAsc').checked)
			url += "&sortOrder=asc";
		else
			url += "&sortOrder=desc";
		
		if (document.getElementById('sortText').checked)
			url += "&sortOn=text";
		else if (document.getElementById('sortOccurences').checked)
			url += "&sortOn=occurences";
			
		document.location = url;
	}
</script>

<style>
	th { text-align: left; }
</style>

<h2><spring:message code="ConceptProposal.manage.title"/></h2>

<a href="proposeConcept.form"><spring:message code="ConceptProposal.proposeNewConcept"/></a>

<br/><br/>

<openmrs:hasPrivilege privilege="Edit Concepts">
	<table>
		<tr>
			<th><spring:message code="ConceptProposal.includeCompleted"/></th>
			<td><input type="checkbox" <c:if test="${param.includeCompleted}">checked</c:if> id="includeCompleted" onclick="updateList()" /></td>
		</tr>
		<tr>
			<th><spring:message code="ConceptProposal.sortOn"/></th>
			<td>
				<input type="radio" name="sortOn" id="sortText" value="text" <c:if test="${param.sortOn == 'text'}">checked</c:if> onclick="updateList()" /><label for="sortText"><spring:message code="ConceptProposal.originalText"/></label>
				<input type="radio" name="sortOn" id="sortOccurences" value="text" <c:if test="${param.sortOn == null || param.sortOn == 'occurences'}">checked</c:if> onclick="updateList()" /><label for="sortOccurences"><spring:message code="ConceptProposal.occurences"/></label>
			</td>
		</tr>
		<tr>
			<th><spring:message code="ConceptProposal.sortOrder"/></th>
			<td>
				<input type="radio" name="sortOrder" id="orderAsc" value="asc" <c:if test="${param.sortOrder == 'asc'}">checked</c:if> onclick="updateList()" /><label for="orderAsc"><spring:message code="ConceptProposal.sortOrder.asc"/></label>
				<input type="radio" name="sortOrder" id="orderDesc" value="desc" <c:if test="${param.sortOrder == null || param.sortOrder == 'desc'}">checked</c:if> onclick="updateList()" /><label for="orderDesc"><spring:message code="ConceptProposal.sortOrder.desc"/></label>
			</td>
		</tr>
	</table>
	
	<br/>
	
	<b class="boxHeader"><spring:message code="ConceptProposal.list.title"/></b>
	
	<form method="post" class="box">
		<table width="100%" cellspacing="0" cellpadding="2">
			<tr>
				<th> <spring:message code="ConceptProposal.encounter"/> </th>
				<th> <spring:message code="ConceptProposal.originalText"/> </th>
				<th> <spring:message code="general.creator"/> </th>
				<th> <spring:message code="general.dateCreated"/> </th>
				<th> <spring:message code="ConceptProposal.occurences"/> </th>
			</tr>
			<c:forEach var="map" items="${conceptProposalMap}" varStatus="rowStatus">
				<c:forEach items="${map.key}" var="conceptProposal" varStatus="varStatus">
					<c:if test="${varStatus.first}">
						<tr class='<c:choose><c:when test="${rowStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose><c:if test="${conceptProposal.state != unmapped}"> voided</c:if>'
							onclick="selectProposal('${conceptProposal.conceptProposalId}')"
							onmouseover="mouseOver(this)" onmouseout="mouseOut(this)">
							<td valign="top">${conceptProposal.encounter.encounterId}</td>
							<td valign="top">${conceptProposal.originalText}</td>
							<td valign="top"><openmrs:format user="${conceptProposal.creator}"/></td>
							<td valign="top">${conceptProposal.dateCreated}</td>
							<td valign="top">${map.value}</td>
						</tr>
					</c:if>
				</c:forEach>
			</c:forEach>
		</table>
	</form>
</openmrs:hasPrivilege>

<%@ include file="/WEB-INF/template/footer.jsp" %>