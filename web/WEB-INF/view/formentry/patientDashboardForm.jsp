<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/formentry/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var timeOut = null;

	function startDownloading() {
		timeOut = setTimeout("goBack()", 30000);
	}
	
	function goBack() {
		document.location='index.htm';
	}
	
	function switchPatient() {
		document.location='index.htm?phrase=${param.phrase}&autoJump=false';
	}
	
	function cancelTimeout() {
		if (timeOut != null)
			clearTimeout(timeOut);
	}
	
	function changeTab(selectedElement, tabType) {
		if (!document.getElementById || !document.createTextNode) {return;}
		var tabs = document.getElementById('patientTabs').getElementsByTagName('a');
		for (var i=0; i<tabs.length; i++) {
			if (tabs[i].className == 'current') {
				tabs[i].className = '';
			}
		}
		selectedElement.className = 'current';
		
		if (tabType == 'overview') {
			document.getElementById('patientOverview').style.display = '';
			document.getElementById('patientRegimen').style.display = 'none';
			document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'regimen') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = '';
			document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'encounters') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = '';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'demographics') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = '';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'graphs') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = '';
		}
		else if (tabType == 'forms') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			document.getElementById('patientForms').style.display = '';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}

    }
</script>

<openmrs:portlet url="patientSet" id="patientSetHeader" size="compact" parameters="linkUrl=patientDashboard.form|allowRemove=true|allowClear=true|selectedPatientId=${patient.patientId}|mutable=true|droppable=true"/>

&nbsp;

<div id="patientTabs">
	<ul>
		<li><a href="#" onclick="changeTab(this, 'overview');" class="current">Overview</a></li>
		<li><a href="#" onclick="changeTab(this, 'regimen');">Regimens</a></li>
		<li><a href="#" onclick="changeTab(this, 'forms');">Forms</a></li>
		<li><a href="#" onclick="changeTab(this, 'encounters');">Encounters</a></li>
		<li><a href="#" onclick="changeTab(this, 'demographics');">Demographics</a></li>
		<li><a href="#" onclick="changeTab(this, 'graphs');">Graphs</a></li>
	</ul>
</div>

<openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}"/>
<br>
<div id="patientSections">
	<div id="patientOverview">
		<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
	</div>
	<div id="patientRegimen" style="display:none;">
		<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${patient.patientId}"/>
	</div>
	<div id="patientForms" style="display:none;">
		<openmrs:portlet url="patientForms" id="patientDashboardForms" patientId="${patient.patientId}"/>
	</div>
	<div id="patientEncounters" style="display:none;">
		<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}"/>
	</div>
	<div id="patientDemographics" style="display:none;">
		<openmrs:portlet url="patientDemographics" id="patientDashboardDemographics" patientId="${patient.patientId}"/>
	</div>
	<div id="patientGraphs" style="display:none;">
		<openmrs:portlet url="patientGraphs" id="patientGraphsPortlet" patientId="${patient.patientId}"/>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
