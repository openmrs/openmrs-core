<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/patientDashboard.form" />

<c:set var="OPENMRS_VIEWING_PATIENT_ID" scope="request" value="${patient.patientId}"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var timeOut = null;
	addEvent(window, 'load', initTabs);

	function initTabs() {
		var c = getTabCookie();
		if (c == null) {
			var tabs = document.getElementById("patientTabs").getElementsByTagName("a");
			if (tabs.length && tabs[0].id)
				c = tabs[0].id;
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
	
	function changeTab(tabObj) {
		if (!document.getElementById || !document.createTextNode) {return;}
		if (typeof tabObj == "string")
			tabObj = document.getElementById(tabObj);
		
		if (tabObj) {
			var tabs = tabObj.parentNode.parentNode.getElementsByTagName('a');
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].className.indexOf('current') != -1) {
					manipulateClass('remove', tabs[i], 'current');
				}
				var divId = tabs[i].id.substring(0, tabs[i].id.indexOf("Tab"));
				var divObj = document.getElementById(divId);
				if (divObj) {
					if (tabs[i].id == tabObj.id)
						divObj.style.display = "";
					else
						divObj.style.display = "none";
				}
			}
			addClass(tabObj, 'current');
			
			setTabCookie(tabObj.id);
		}
		return false;
    }
</script>

<openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}"/>

<div id="patientTabs">
	<ul>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
			<li><a id="patientOverviewTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.overview"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Regimen Section">
			<li><a id="patientRegimenTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.regimens"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
			<li><a id="patientEncountersTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.encounters"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
			<li><a id="patientDemographicsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.demographics"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
			<li><a id="patientGraphsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.graphs"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Forms Section">
			<li><a id="patientFormsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.forms"/></a></li>
		</openmrs:hasPrivilege>
	</ul>
</div>
<br/>
<div id="patientSections">
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
		<div id="patientOverview" style="display:none;">
			<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Regimen Section">	
		<div id="patientRegimen" style="display:none;">
			<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${patient.patientId}" parameters="displayDrugSetIds=ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
		<div id="patientEncounters" style="display:none;">
			<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}"/>
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
		<div id="patientDemographics" style="display:none;">
			<openmrs:portlet url="patientDemographics" id="patientDashboardDemographics" patientId="${patient.patientId}"/>
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
		<div id="patientGraphs" style="display:none;">
			<openmrs:portlet url="patientGraphs" id="patientGraphsPortlet" patientId="${patient.patientId}"/>
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Forms Section">
		<div id="patientForms" style="display:none;">
			<openmrs:portlet url="patientForms" id="patientDashboardForms" patientId="${patient.patientId}" parameters="goBackOnEntry=false|showUnpublishedForms=true" />
		</div>
	</openmrs:hasPrivilege>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>