<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Encounters" otherwise="/login.htm" redirect="/admin/encounters/encounterType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="EncounterType.title"/></h2>

<spring:hasBindErrors name="encounterType">
	<spring:message code="error.fix"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td>
			<spring:bind path="encounterType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top">
			<spring:bind path="encounterType.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${encounterType.encounterTypeId != null}">
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>${encounterType.creator}</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>${encounterType.dateCreated}</td>
		</tr>
	<input type="hidden" name="encounterTypeId:int" value="<c:out value="${encounterType.encounterTypeId}"/>">
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="EncounterType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>