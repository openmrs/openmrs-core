<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require
	privilege="Add Patient Searches,Edit Patient Searches,Delete Patient Searches,View Patient Searches"
	otherwise="/login.htm" redirect="/admin/reports/patientSearch.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2><spring:message code="PatientSearch.manage.title" /></h2>

<br />

<b class="boxHeader"><spring:message
	code="PatientSearch.manage.list.title" /></b>
<form method="post" class="box">
<table cellpadding="2" cellspacing="0">
	<tr>
		<th></th>
		<th><spring:message code="general.name" /></th>
		<th><spring:message code="general.description" /></th>
		<th><spring:message code="general.parameters" /></th>
	</tr>
	<c:forEach var="patientSearch" items="${patientSearchList}"
		varStatus="varStatus">
		<tr
			class="<c:choose><c:when test="${varStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td valign="top"><input type="checkbox" name="patientSearchId"
				value="${patientSearch.reportObjectId}"></td>
			<td valign="top"><a
				href="patientSearch.form?patientSearchIdLookup=${patientSearch.reportObjectId}">${patientSearch.name}</a>
			</td>
			<td valign="top">${patientSearch.description}</td>
			<td>
				<c:forEach var="par" items="${patientSearch.patientSearch.parameters}">
					${par.name}: ${par.defaultValue}
					(${par.clazz})
					<br/>
				</c:forEach>
			</td>
		</tr>
	</c:forEach>
</table>

<input type="submit"
	value='<spring:message code="PatientSearch.delete"/>' name="action"
	onclick="return confirm('Are you sure you want to DELETE these Patient Searches?')">
<input type="hidden" name="hiddenDelete" value="no"></form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
