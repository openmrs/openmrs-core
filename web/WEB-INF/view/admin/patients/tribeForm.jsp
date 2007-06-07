<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Tribes" otherwise="/login.htm" redirect="/admin/patients/tribe.form"/>

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
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.retired"/></td>
		<td>
			<spring:bind path="tribe.retired">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<spring:bind path="tribe.tribeId">
	<input type="hidden" name="tribeId:int" value="<c:out value="${status.value}"/>">
	<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>
<br />
<input type="submit" value="<spring:message code="Tribe.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>