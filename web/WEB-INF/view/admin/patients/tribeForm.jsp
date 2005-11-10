<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.htm" redirect="/admin/patients/tribe.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<br />
<h2><spring:message code="Tribe.title"/></h2>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Tribe.name"/></td>
		<td>
			<spring:bind path="tribe.name">
				<input type="text" name="name" value="${status.value}" />
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Tribe.retired"/></td>
		<td>
			<spring:bind path="tribe.retired">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value == true}">checked</c:if> />
				<span class="error">${status.errorMessage}</span>
			</spring:bind>
		</td>
	</tr>
</table>
<spring:bind path="tribe.tribeId">
	<input type="hidden" name="tribeId:int" value="<c:out value="${status.value}"/>">
	<span class="error">${status.errorMessage}</span>
</spring:bind>
<br />
<input type="submit" value="<spring:message code="Tribe.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>