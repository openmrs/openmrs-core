<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Cohorts" otherwise="/login.htm" redirect="/admin/reports/cohorts.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	function maybeEnableDeleteButton(val) {
		if (val == '')
			$('deleteButton').disabled = true;
		else
			$('deleteButton').disabled = false;
	}
</script>

<h2><spring:message code="Cohort.manage.title"/></h2>	

<br/>

<form method="post">
	<input type="hidden" name="method" value="delete"/>
	<table>
		<tr>
			<th></th>
			<th align="left"><spring:message code="general.id"/></th>
			<th align="left"><spring:message code="general.name"/></th>
			<th align="left"><spring:message code="general.description"/></th>
		</tr>
		<c:forEach var="cohort" items="${cohortList}">
			<tr>
				<td>
					<c:if test="${!cohort.voided}">
						<input type="checkbox" name="cohortId" value="${cohort.cohortId}"/>
					</c:if>
				</td>
				<td align="right">${cohort.cohortId}</td>
				<td>
					<c:if test="${cohort.voided}">
						<strike>
					</c:if>
					${cohort.name}
					<c:if test="${cohort.voided}">
						</strike>
					</c:if>
				</td>
				<td>
					<c:if test="${cohort.voided}">
						<strike>
					</c:if>
					${cohort.description}
					<c:if test="${cohort.voided}">
						</strike>
					</c:if>
				</td>
			</tr>
		</c:forEach>
	</table>
	<br/>
	<br/>
	<spring:message code="Cohort.manage.deleteInstructions"/><input type="text" size="40" name="voidReason" onKeyUp="maybeEnableDeleteButton(this.value)" />
	<input type="submit" id="deleteButton" value="<spring:message code="general.delete"/>" disabled="true" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>