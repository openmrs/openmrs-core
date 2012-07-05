<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	function changeStatusClicked(button) {
		alert(button.id);
	}

</script>

<div>
	<b class="boxHeader"><spring:message code="PatientDashboard.box.header"/></b>
	<div class="box">
		<table id="manageDashboardTable" cellpadding="2" cellspacing="0" style="width: 50%">
			<thead id="manageDashboardTableHeader">
				<tr>
					<th><span><spring:message code="PatientDashboard.tab.header"/></span></th>
					<th><span><spring:message code="PatientDashboard.current.header"/></span></th>
					<th><span><spring:message code="PatientDashboard.change.header"/></span></th>
				</tr>
			</thead>
			<tbody>
				<tr class="odd">
					<td><spring:message code="patientDashboard.overview"/></td>
					<td>${overviewStatus}</td>
					<td><button id="changeOverviewButton" onclick="return changeStatusClicked(this);">${overviewButtonLabel}</button></td>
				</tr>
				<tr class="even">
					<td><spring:message code="patientDashboard.regimens"/></td>
					<td>${regimensStatus}</td>
					<td><button id="changeRegimensButton" onclick="return changeStatusClicked(this);">${regimensButtonLabel}</button></td>
				</tr>
				<tr class="odd">
					<td><spring:message code="patientDashboard.visits"/>/<spring:message code="patientDashboard.encounters"/></td>
					<td>${visitsEncountersStatus}</td>
					<td><button id="changeVisitsEncountersButton" onclick="return changeStatusClicked(this);">${visitsEncountersButtonLabel}</button></td>
				</tr>
				<tr class="even">
					<td><spring:message code="patientDashboard.demographics"/></td>
					<td>${demographicsStatus}</td>
					<td><button id="changeDemographicsButton" onclick="return changeStatusClicked(this);">${demographicsButtonLabel}</button></td>
				</tr>
				<tr class="odd">
					<td><spring:message code="patientDashboard.graphs"/></td>
					<td>${graphsStatus}</td>
					<td><button id="changeGraphsButton" onclick="return changeStatusClicked(this);">${graphsButtonLabel}</button></td>
				</tr>
				<tr class="even">
					<td><spring:message code="patientDashboard.formEntry"/></td>
					<td>${formentryStatus}</td>
					<td><button id="changeFormEntryButton" onclick="return changeStatusClicked(this);">${formentryButtonLabel}</button></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>