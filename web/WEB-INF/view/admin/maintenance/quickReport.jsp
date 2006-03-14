<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/maintenance/quickReport.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>

<br />
<h2><spring:message code="QuickReport.manage"/></h2>
<br />

<form method="get" action="${pageContext.request.contextPath}/quickReportServlet">
	<table border="0" cellspacing="2" cellpadding="2">
		<tr>
			<td><spring:message code="QuickReport.type"/></td>
			<td>
				<select name="reportType">
					<option value="RETURN VISIT DATE THIS WEEK">Return Visit Date This Week</option>
				</select>
			</td>
		</tr>
		<tr>
			<td><spring:message code="QuickReport.date"/></td>
			<td><input type="text" name="reportDate" onClick="showCalendar(this)" /></td>
		</tr>
	</table>
	<br/>
	<input type="submit" value='<spring:message code="QuickReport.view" />' />
</form>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>