<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Database Changes" otherwise="/login.htm"
	redirect="/admin/maintenance/databaseChangesInfo.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2><openmrs:message code="DatabaseChangesInfo.title" /></h2>

<openmrs:message code="DatabaseChangesInfo.help" />
<br/>
<br/>
<b><openmrs:message code="DatabaseChangesInfo.filename.title" /></b>
<div style="padding-left: 20px; padding-top: 5px">${updateLogFile}</div>

<br/>
<br/>

<b class="boxHeader"><openmrs:message code="DatabaseChangesInfo.header"/></b>
<table cellpadding="4" cellspacing="0" border="0" class="box">
	<tr>
		<th style="white-space: nowrap"><openmrs:message code="DatabaseChangesInfo.runDate" /></th>
		<th><openmrs:message code="DatabaseChangesInfo.comments" /></th>
		<th><openmrs:message code="general.description" /></th>
		<th><openmrs:message code="DatabaseChangesInfo.runStatus" /></th>
		<th><openmrs:message code="general.author" /></th>
	</tr>
	<c:forEach items="${databaseChanges}" var="databaseChange" varStatus="varStatus">
		<tr class="${varStatus.index % 2 == 0 ? 'evenRow' : 'oddRow'}">
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