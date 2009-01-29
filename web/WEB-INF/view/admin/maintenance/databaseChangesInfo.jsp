<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Database Changes" otherwise="/login.htm"
	redirect="/admin/maintenance/databaseChangesInfo.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2><spring:message code="DatabaseChangesInfo.title" /></h2>

<spring:message code="DatabaseChangesInfo.help" />
<br/>
<br/>

<b class="boxHeader"><spring:message code="DatabaseChangesInfo.header"/></b>
<table cellpadding="4" cellspacing="0" border="0" class="box">
	<tr>
		<th style="white-space: nowrap"><spring:message code="DatabaseChangesInfo.runDate" /></th>
		<th><spring:message code="DatabaseChangesInfo.comments" /></th>
		<th><spring:message code="general.description" /></th>
		<th><spring:message code="DatabaseChangesInfo.runStatus" /></th>
		<th><spring:message code="general.author" /></th>
	</tr>
	<c:forEach items="${databaseChanges}" var="databaseChange" varStatus="varStatus">
		<tr class="<c:choose><c:when test="${varStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td style="white-space: nowrap"><openmrs:formatDate date="${databaseChange.ranDate}" type="medium" /></td>
			<td>${databaseChange.comments}</td>
			<td>${databaseChange.description}</td>
			<td>${databaseChange.runStatus}</td>
			<td>${databaseChange.author}</td>
		</tr>
	</c:forEach>
</table>

<br />
<br />
<%@ include file="/WEB-INF/template/footer.jsp"%>