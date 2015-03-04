<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/patientDashboard.form" />

<c:set var="OPENMRS_VIEWING_PATIENT_ID" scope="request" value="${patient.patientId}"/>
<openmrs:globalProperty var="enablePatientName" key="dashboard.showPatientName" defaultValue="false"/>

<c:if test="${enablePatientName}">
	<c:set var="patientName" value="${patient.personName.fullName} (${patient.patientIdentifier})"/>
	<openmrs:message var="pageTitle" text="${patientName}" scope="page"/>
</c:if>
<c:if test="${!enablePatientName}">
	<openmrs:message var="pageTitle" code="patientDashboard.title" scope="page"/>
</c:if>
<%@ include file="/WEB-INF/template/header.jsp" %>

<%-- Files from encounter and graph portlets being included near header to improve page loading speed
     If those tabs/portlets are no longer using them, they should be removed from here --%>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />

<openmrs:htmlInclude file="/scripts/flot/jquery.flot.js" />
<openmrs:htmlInclude file="/scripts/flot/jquery.flot.multiple.threshold.js"/> 
<%-- /end file imports for portlets --%>

<script type="text/javascript">
	var timeOut = null;

	<openmrs:authentication>var userId = "${authenticatedUser.userId}";</openmrs:authentication>

	//initTabs
	$j(document).ready(function() {
		var c = getTabCookie();
		if (c == null || (!document.getElementById(c))) {
			var tabs = document.getElementById("patientTabs").getElementsByTagName("a");
			if (tabs.length && tabs[0].id)
				c = tabs[0].id;
		}
		changeTab(c);
	});
	
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
		<div><openmrs:message code="Patient.voidedMessage"/></div>
	</div>
</c:if>

<c:if test="${patient.dead}">
	<div id="patientDashboardDeceased" class="retiredMessage">
		<div>
			<openmrs:message code="Patient.patientDeceased"/>
			<c:if test="${not empty patient.deathDate}">
				&nbsp;&nbsp;&nbsp;&nbsp;
				<openmrs:message code="Person.deathDate"/>: <openmrs:formatDate date="${patient.deathDate}"/>
			</c:if>
			<c:if test="${not empty patient.causeOfDeath}">
				&nbsp;&nbsp;&nbsp;&nbsp;
				<openmrs:message code="Person.causeOfDeath"/>: <openmrs:format concept="${patient.causeOfDeath}"/>
				<c:if test="${not empty causeOfDeathOther}"> 
					  &nbsp;:&nbsp;<c:out value="${causeOfDeathOther}"></c:out>
				</c:if>
			</c:if>
		</div>
	</div>
</c:if>

<openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}"/>

<openmrs:globalProperty var="enableFormEntryTab" key="FormEntry.enableDashboardTab" defaultValue="true"/>

<div id="patientTabs" class="${patientVariation}">
	<ul>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
			<li><a id="patientOverviewTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="patientDashboard.overview"/></a></li>
		</openmrs:hasPrivilege>
		
		<openmrs:globalProperty key="visits.enabled" defaultValue="true" var="visitsEnabled"/>
		<c:choose>		
			<c:when test='${visitsEnabled}'>
				<openmrs:hasPrivilege privilege="Patient Dashboard - View Visits Section">
					<li><a id="patientVisitsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="patientDashboard.visits"/></a></li>
				</openmrs:hasPrivilege>
			</c:when>
			<c:otherwise>
				<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
					<li><a id="patientEncountersTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="patientDashboard.encounters"/></a></li>
				</openmrs:hasPrivilege>
			</c:otherwise>
			
		</c:choose>
		
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
			<li><a id="patientDemographicsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="patientDashboard.demographics"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
			<li><a id="patientGraphsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="patientDashboard.graphs"/></a></li>
		</openmrs:hasPrivilege>
		<c:if test="${enableFormEntryTab}">
			<openmrs:hasPrivilege privilege="Form Entry">
				<li><a id="formEntryTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="patientDashboard.formEntry"/></a></li>
			</openmrs:hasPrivilege>
		</c:if>
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
			<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
				<li>
					<a id="${extension.tabId}Tab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><openmrs:message code="${extension.tabName}"/></a>
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
	
	<openmrs:globalProperty key="visits.enabled" defaultValue="true" var="visitsEnabled"/>
	<c:choose>		
			<c:when test='${visitsEnabled}'>
				<openmrs:hasPrivilege privilege="Patient Dashboard - View Visits Section">
					<div id="patientVisits" style="display:none;">
						
						<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.VisitsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
						<openmrs:portlet url="patientVisits" id="patientDashboardVisits" patientId="${patient.patientId}" />
						
					</div>
				</openmrs:hasPrivilege>
			</c:when>
			<c:otherwise>
				<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
					<div id="patientEncounters" style="display:none;">
						
						<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.EncountersTabHeader" type="html" parameters="patientId=${patient.patientId}" />
						<openmrs:globalProperty var="maxEncs" key="dashboard.encounters.maximumNumberToShow" defaultValue="" />
						<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=${maxEncs}|showPagination=true|formEntryReturnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
						
					</div>
				</openmrs:hasPrivilege>
			</c:otherwise>
			
	</c:choose>
	
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
				<openmrs:portlet url="personFormEntry" id="formEntryPortlet" personId="${patient.personId}" parameters="showDecoration=true|showDefinedNumberOfEncounters=true|returnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
				
			</div>
		</openmrs:hasPrivilege>
	</c:if>		
	<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<div id="${extension.tabId}" style="display:none;">
				<c:catch var="ex">
					<c:choose>
						<c:when test="${extension.portletUrl == '' || extension.portletUrl == null}">
							portletId is null: '${extension.extensionId}'
						</c:when>
						<c:otherwise>
						
							<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.${extension.tabId}TabHeader" type="html" parameters="patientId=${patient.patientId}" />
							<openmrs:portlet url="${extension.portletUrl}" id="${extension.tabId}" moduleId="${extension.moduleId}"/>
							
						</c:otherwise>
					</c:choose>
				</c:catch>
				<c:if test="${not empty ex}">
					<div class="error">
						<openmrs:message code="fix.error.plain"/> <br/>
						<b>${ex}</b>
						<div style="height: 200px; width: 800px; overflow: scroll">
							<c:forEach var="row" items="${ex.cause.stackTrace}">
								${row}<br/>
							</c:forEach>
						</div>
					</div>
				</c:if>
			</div>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>
	
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>