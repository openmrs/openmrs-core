<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptProposal.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="ConceptProposal.manage.title"/></h2>

<br />

<b class="boxHeader">
<a style="display: block; float: right" href="?includeCompleted=${!param.includeCompleted}"><spring:message code="ConceptProposal.includeCompleted"/></a>
<spring:message code="ConceptProposal.list.title"/>
</b>
<form method="post" class="box">
	<table width="100%">
		<tr>
			<th></th>
			<th> <spring:message code="ConceptProposal.encounter"/> </th>
			<th> <spring:message code="ConceptProposal.originalText"/> </th>
			<th> <spring:message code="general.creator"/> </th>
			<th> <spring:message code="general.dateCreated"/> </th>
		</tr>
		<c:forEach var="conceptProposal" items="${conceptProposalList}">
			<tr <c:if test="${conceptProposal.state != unmapped}">class="voided"</c:if>>
				<td valign="top"><a href="conceptProposal.form?conceptProposalId=${conceptProposal.conceptProposalId}">edit</a></td>
				<td valign="top">${conceptProposal.encounter.encounterId}</td>
				<td valign="top">${conceptProposal.originalText}</td>
				<td valign="top">${conceptProposal.creator}</td>
				<td valign="top">${conceptProposal.dateCreated}</td>
			</tr>
		</c:forEach>
	</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>