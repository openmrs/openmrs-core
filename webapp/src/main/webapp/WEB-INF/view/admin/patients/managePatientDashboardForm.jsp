<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	$j(document).ready(function() { 
		var overviewId = "overview" + "${ajaxOverview}" + "Radio";
		var regimensId = "regimens" + "${ajaxRegimens}" + "Radio";
		var visitsId = "encountersvisits" + "${ajaxVisitsEncounters}" + "Radio";
		var demographicsId = "demographics" + "${ajaxDemographics}" + "Radio";
		var graphsId = "graphs" + "${ajaxGraphs}" + "Radio";
		var formentryId = "formentry" + "${ajaxFormEntry}" + "Radio";
		document.getElementById(overviewId).checked = true;
		document.getElementById(regimensId).checked = true;
		document.getElementById(visitsId).checked = true;
		document.getElementById(demographicsId).checked = true;
		document.getElementById(graphsId).checked = true;
		document.getElementById(formentryId).checked = true;	
	})


	function changeStatusClicked(button) {
	    var form = document.createElement("form");
	    form.setAttribute("method", "post");
	    form.setAttribute("action", "savePatientDashboard.form");
		var params = {};
		
		var tabId = button.id.replace("Button","");
		var onclickRadio = tabId + "OnclickRadio";
		var backgroundRadio = tabId + "BackgroundRadio";
		var preloadRadio = tabId + "PreloadRadio";

		params["tabId"] = tabId;
		if(document.getElementById(onclickRadio).checked) {
			params["status"] = "Onclick"
		} else if(document.getElementById(backgroundRadio).checked) {
			params["status"] = "Background";
		} else if(document.getElementById(preloadRadio).checked) {
			params["status"] = "Preload";
		}
	    for(var key in params) {
	        if(params.hasOwnProperty(key)) {
	            var hiddenField = document.createElement("input");
	            hiddenField.setAttribute("type", "hidden");
	            hiddenField.setAttribute("name", key);
	            hiddenField.setAttribute("value", params[key]);

	            form.appendChild(hiddenField);
	         }
	    }

	    document.body.appendChild(form);
	    form.submit();
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
					<td>${overviewStatusLabel}</td>
					<td>
						<input id="overviewOnclickRadio" type="radio" name="overview" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
					    <input id="overviewBackgroundRadio" type="radio" name="overview" value="background"/><spring:message code="PatientDashboard.status.background"/>
					    <input id="overviewPreloadRadio" type="radio" name="overview" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
						<button id="overviewButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
					</td>
				</tr>
				<tr class="even">
					<td><spring:message code="patientDashboard.regimens"/></td>
					<td>${regimensStatusLabel}</td>
					<td>
						<input id="regimensOnclickRadio" type="radio" name="regimens" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
					    <input id="regimensBackgroundRadio" type="radio" name="regimens" value="background"/><spring:message code="PatientDashboard.status.background"/>
					    <input id="regimensPreloadRadio" type="radio" name="regimens" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
						<button id="regimensButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
					</td>
				</tr>
				<tr class="odd">
					<td><spring:message code="patientDashboard.visits"/>/<spring:message code="patientDashboard.encounters"/></td>
					<td>${visitsEncountersStatusLabel}</td>
					<td>
						<input id="encountersvisitsOnclickRadio" type="radio" name="encountersvisits" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
					    <input id="encountersvisitsBackgroundRadio" type="radio" name="encountersvisits" value="background"/><spring:message code="PatientDashboard.status.background"/>
					    <input id="encountersvisitsPreloadRadio" type="radio" name="encountersvisits" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
						<button id="encountersvisitsButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
					</td>
				</tr>
				<tr class="even">
					<td><spring:message code="patientDashboard.demographics"/></td>
					<td>${demographicsStatusLabel}</td>
					<td>
						<input id="demographicsOnclickRadio" type="radio" name="demographics" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
					    <input id="demographicsBackgroundRadio" type="radio" name="demographics" value="background"/><spring:message code="PatientDashboard.status.background"/>
					    <input id="demographicsPreloadRadio" type="radio" name="demographics" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
						<button id="demographicsButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
					</td>
				</tr>
				<tr class="odd">
					<td><spring:message code="patientDashboard.graphs"/></td>
					<td>${graphsStatusLabel}</td>
					<td>
						<input id="graphsOnclickRadio" type="radio" name="graphs" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
					    <input id="graphsBackgroundRadio" type="radio" name="graphs" value="background"/><spring:message code="PatientDashboard.status.background"/>
					    <input id="graphsPreloadRadio" type="radio" name="graphs" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
						<button id="graphsButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
					</td>
				</tr>
				<tr class="even">
					<td><spring:message code="patientDashboard.formEntry"/></td>
					<td>${formentryStatusLabel}</td>
					<td>
						<input id="formentryOnclickRadio" type="radio" name="formentry" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
					    <input id="formentryBackgroundRadio" type="radio" name="formentry" value="background"/><spring:message code="PatientDashboard.status.background"/>
					    <input id="formentryPreloadRadio" type="radio" name="formentry" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
						<button id="formentryButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>