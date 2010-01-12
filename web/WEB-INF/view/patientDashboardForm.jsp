<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/patientDashboard.form" />

<c:set var="OPENMRS_VIEWING_PATIENT_ID" scope="request" value="${patient.patientId}"/>
<spring:message var="pageTitle" code="patientDashboard.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var timeOut = null;
	addEvent(window, 'load', initTabs);

	<openmrs:authentication>var userId = "${authenticatedUser.userId}";</openmrs:authentication>

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
		document.cookie = "dashboardTab-" + userId + "="+escape(tabType);
	}
	
	function getTabCookie() {
		var cookies = document.cookie.match('dashboardTab-' + userId + '=(.*?)(;|$)');
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
				var divId = tabs[i].id.substring(0, tabs[i].id.lastIndexOf("Tab"));
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

<c:if test="${patient.voided}">
	<div id="patientDashboardVoided" class="retiredMessage">
		<div><spring:message code="Patient.voidedMessage"/></div>
	</div>
</c:if>

<c:if test="${patient.dead}">
	<div id="patientDashboardDeceased" class="retiredMessage">
		<div>
			<spring:message code="Patient.patientDeceased"/>
			<c:if test="${not empty patient.deathDate}">
				&nbsp;&nbsp;&nbsp;&nbsp;
				<spring:message code="Person.deathDate"/>: <openmrs:formatDate date="${patient.deathDate}"/>
			</c:if>
			<c:if test="${not empty patient.causeOfDeath}">
				&nbsp;&nbsp;&nbsp;&nbsp;
				<spring:message code="Person.causeOfDeath"/>: <openmrs:format concept="${patient.causeOfDeath}"/>
				<c:if test="${not empty causeOfDeathOther}"> 
					  &nbsp;:&nbsp;<c:out value="${causeOfDeathOther}"></c:out>
				</c:if>
			</c:if>
		</div>
	</div>
</c:if>

<openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}"/>

<openmrs:globalProperty var="enableFormEntryTab" key="FormEntry.enableDashboardTab" defaultValue="true"/>

<div id="patientTabs${patientVariation}">
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
		<c:if test="${enableFormEntryTab}">
			<openmrs:hasPrivilege privilege="Form Entry">
				<li><a id="formEntryTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.formEntry"/></a></li>
			</openmrs:hasPrivilege>
		</c:if>
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
			<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
				<li>
					<a id="${extension.tabId}Tab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="${extension.tabName}"/></a>
				</li>
			</openmrs:hasPrivilege>
		</openmrs:extensionPoint>
	</ul>
</div>

<div id="patientSections">
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
		<div id="patientOverview" style="display:none;">
			
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.OverviewTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
			
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Regimen Section">	
		<div id="patientRegimen" style="display:none;">

			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.RegimenTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:globalProperty var="displayDrugSetIds" key="dashboard.regimen.displayDrugSetIds" defaultValue="ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
			<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${patient.patientId}" parameters="displayDrugSetIds=${displayDrugSetIds}" />
			
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
		<div id="patientEncounters" style="display:none;">
			
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.EncountersTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=100|showPagination=true|formEntryReturnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
			
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
		<div id="patientDemographics" style="display:none;">
			
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.DemographicsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="patientDemographics" id="patientDashboardDemographics" patientId="${patient.patientId}"/>
			
		</div>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
		<div id="patientGraphs" style="display:none;">
		
			<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.GraphsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
			<openmrs:portlet url="patientGraphs" id="patientGraphsPortlet" patientId="${patient.patientId}"/>
			
		</div>
	</openmrs:hasPrivilege>
	<c:if test="${enableFormEntryTab}">
		<openmrs:hasPrivilege privilege="Form Entry">
			<div id="formEntry" style="display:none;">
			
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.FormEntryTabHeader" type="html" parameters="patientId=${patient.patientId}" />
				<openmrs:portlet url="personFormEntry" id="formEntryPortlet" personId="${patient.personId}" parameters="showDecoration=true|showLastThreeEncounters=true|returnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
				
			</div>
		</openmrs:hasPrivilege>
	</c:if>		
	<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<div id="${extension.tabId}" style="display:none;">
				<c:choose>
					<c:when test="${extension.portletUrl == '' || extension.portletUrl == null}">
						portletId is null: '${extension.extensionId}'
					</c:when>
					<c:otherwise>
					
						<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.${extension.tabId}TabHeader" type="html" parameters="patientId=${patient.patientId}" />
						<openmrs:portlet url="${extension.portletUrl}" id="${extension.tabId}" moduleId="${extension.moduleId}"/>
						
					</c:otherwise>
				</c:choose>
			</div>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>
	
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>