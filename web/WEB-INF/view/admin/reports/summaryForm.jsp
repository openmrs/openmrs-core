<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Summaries" otherwise="/login.htm" redirect="/admin/reports/summaryForm.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<br />
<h2><spring:message code="PatientSummary.manage"/></h2>
<br />

<script type="text/javascript">
	function show(ids) {
		var rows = document.getElementById("summaryOptions").rows;
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
	
	function clearAutoComplete() {
		if (document.getElementsByTagName) {
		var inputs = document.getElementsByTagName("input");
		for (var i=0;i<inputs.length; i++) {
			if (inputs[i].onclick &&
				inputs[i].onclick.toString().indexOf("showCalendar") != -1) {
					inputs[i].setAttribute("autocomplete", "on");
			}
		}
	}
	}

</script>

<form method="post" action="${pageContext.request.contextPath}/summaryServlet">
	<table border="0" cellspacing="2" cellpadding="2" id="summaryOptions">
		<tr>
			<td colspan="2"><b>Return Visit Date:<b></td>
		</tr>
		<tr id="startDate">
			<td> &nbsp; Start</td>
			<td><input type="text" name="startDate" onClick="showCalendar(this)" /></td>
		</tr>
		<tr id="endDate">
			<td> &nbsp; End</td>
			<td><input type="text" name="endDate" onClick="showCalendar(this)" /></td>
		</tr>
		<tr>
			<td colspan="2">and/or</td>
		</tr>
		<tr id="location">
			<td><b>Last Location</b></td>
			<td>
				<select name="location">
					<option value=""> </option>
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}">${record.name}</option>
					</openmrs:forEachRecord>
				</select>
			</td>
			<td class="desc">Patient's last encounter location</td>
		</tr>
		<tr>
			<td colspan="2">and/or</td>
		</tr>
		<tr>
			<td valign="top"><b>Identifiers</b></td>
			<td><textarea name="patientIdentifiers" rows="10" cols="30"></textarea></td>
			<td class="desc">One patient identifier per line</td>
		</tr>
		<tr>
			<td colspan="2">and/or</td>
		</tr>
	</table>
	
	<br/>
	
	<input type="submit" value="Generate" onClick="clearAutoComplete()" />
	
</form>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>