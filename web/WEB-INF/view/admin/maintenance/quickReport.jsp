<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/maintenance/quickReport.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>

<br />
<h2><spring:message code="QuickReport.manage"/></h2>
<br />

<script type="text/javascript">
	function show(ids) {
		var rows = document.getElementById("reportOptions").rows;
		for (var i=0; i < rows.length; i++) {
			var id = rows[i].id;
			var showRow = false;
			for (var x=0; x < ids.length; x++) {
				if (ids[x] == id) {
					showRow = true;
					break;
				}
			}
			if (showRow == true)
				rows[i].style.display = "";
			else
				rows[i].style.display = "none";
		}
	}

</script>

<form method="get" action="${pageContext.request.contextPath}/quickReportServlet">
	<table border="0" cellspacing="2" cellpadding="2" id="reportOptions">
		<tr id="reportType">
			<td>Report Type</td>
			<td>
				<select name="reportType">
					<option value="RETURN VISIT DATE THIS WEEK" onclick="show(['reportType', 'startDate', 'endDate', 'location'])">Return Visit Date This Week</option>
					<option value="ATTENDED CLINIC THIS WEEK" onclick="show(['reportType', 'startDate', 'endDate', 'location'])">Attended Clinic This Week</option>
					<option value="VOIDED OBS" onclick="show(['reportType'])">Voided Obs</option>
				</select>
			</td>
		</tr>
		<tr id="startDate">
			<td>Start Date</td>
			<td><input type="text" name="startDate" onClick="showCalendar(this)" /></td>
		</tr>
		<tr id="endDate">
			<td>End Date</td>
			<td><input type="text" name="endDate" onClick="showCalendar(this)" /></td>
		</tr>
		<tr id="location">
			<td>Location</td>
			<td>
				<select name="location">
					<option value="">All Locations</option>
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}" <c:if test="${status.value == record.locationId}">selected</c:if>>${record.name}</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
		
		
	</table>
	<br/>
	<input type="submit" value='<spring:message code="QuickReport.view" />' />
</form>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>