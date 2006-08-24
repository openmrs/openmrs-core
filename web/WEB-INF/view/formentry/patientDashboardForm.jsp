<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/formentry/patientDashboard.form" />

<c:set var="OPENMRS_VIEWING_PATIENT_ID" scope="request" value="${patient.patientId}"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var timeOut = null;
	addEvent(window, 'load', initTabs);

	function initTabs() {
		var c = getTabCookie();
		if (c == null) {
			c = 'overview';
		}
		changeTab(c);
	}
	
	function setTabCookie(tabType) {
		document.cookie = "dashboardTab="+escape(tabType);
	}
	
	function getTabCookie() {
		var cookies = document.cookie.match('dashboardTab=(.*?)(;|$)');
		if (cookies) {
			return unescape(cookies[1]);
		}
		return null;
	}
	
	function changeTab(tabType) {
		if (!document.getElementById || !document.createTextNode) {return;}
		var tabs = document.getElementById('patientTabs').getElementsByTagName('a');
		for (var i=0; i<tabs.length; i++) {
			if (tabs[i].className.indexOf('current') != -1) {
				manipulateClass('remove', tabs[i], 'current');
			}
		}
		addClass(document.getElementById(tabType+'Tab'), 'current');
		
		if (tabType == 'overview') {
			document.getElementById('patientOverview').style.display = '';
			document.getElementById('patientRegimen').style.display = 'none';
			//document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'regimen') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = '';
			//document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'encounters') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			//document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = '';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'demographics') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			//document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = '';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		else if (tabType == 'graphs') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			//document.getElementById('patientForms').style.display = 'none';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = '';
		}
		else if (tabType == 'forms') {
			document.getElementById('patientOverview').style.display = 'none';
			document.getElementById('patientRegimen').style.display = 'none';
			//document.getElementById('patientForms').style.display = '';
			document.getElementById('patientEncounters').style.display = 'none';
			document.getElementById('patientDemographics').style.display = 'none';
			document.getElementById('patientGraphs').style.display = 'none';
		}
		setTabCookie(tabType);
    }
</script>

<%--
<div id="breadcrumbHeader" class="breadcrumb">
	<a href="/openmrs">Home</a> >
	<a href="#switch" onClick="switchPatient()">Find Patient</a> >
	<spring:message code="Patient.dashboard.title"/>
</div>
--%>

<openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}"/>

<div id="patientTabs">
	<ul>
		<li><a id="overviewTab" href="#" onclick="changeTab('overview');">Overview</a></li>
		<li><a id="regimenTab" href="#" onclick="changeTab('regimen');">Regimens</a></li>
		<!--<li><a id="formsTab" href="#" onclick="changeTab('forms');">Forms</a></li>-->
		<li><a id="encountersTab" href="#" onclick="changeTab('encounters');">Encounters</a></li>
		<li><a id="demographicsTab" href="#" onclick="changeTab('demographics');">Demographics</a></li>
		<li><a id="graphsTab" href="#" onclick="changeTab('graphs');">Graphs</a></li>
	</ul>
</div>
<br/>
<div id="patientSections">
	<div id="patientOverview" style="display:none;">
		<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
	</div>
	<div id="patientRegimen" style="display:none;">
		<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${patient.patientId}" parameters="displayDrugSetIds=ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
	</div>
	<!--
	<div id="patientForms" style="display:none;">
		<openmrs:portlet url="patientForms" id="patientDashboardForms" patientId="${patient.patientId}"/>
	</div>
	-->
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
