<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/admin/reports/report.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.title"/></h2>

<spring:hasBindErrors name="report">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post" class="box">
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td colspan="5">
			<spring:bind path="report.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top" colspan="5">
			<spring:bind path="report.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(report.reportId == null)}" >
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>
				<spring:bind path="report.creator">
					${report.creator.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>
				<spring:bind path="report.dateCreated">
					<openmrs:formatDate date="${report.dateCreated}" type="long"/>
				</spring:bind>
			</td>
		</tr>
		<input type="hidden" name="reportId:int" value="${report.reportId}">
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="Report.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>