<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/patientDashboard.form" />

<c:set var="OPENMRS_VIEWING_PATIENT_ID" scope="request" value="${patient.patientId}"/>
<openmrs:globalProperty var="enablePatientName" key="dashboard.showPatientName" defaultValue="false"/>

<c:if test="${enablePatientName}">
	<c:set var="patientName" value="${patient.personName.fullName} (${patient.patientIdentifier})"/>
	<spring:message var="pageTitle" text="${patientName}" scope="page"/>
</c:if>
<c:if test="${!enablePatientName}">
	<spring:message var="pageTitle" code="patientDashboard.title" scope="page"/>
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
	var ajaxProperties = {};

	<openmrs:authentication>var userId = "${authenticatedUser.userId}";</openmrs:authentication>

	//initTabs
	$j(document).ready(function() {
		<c:forEach var="entry" items="${ajaxProperties}">
			ajaxProperties["${entry.key}"] = "${entry.value}";
		</c:forEach>
		var c = getTabCookie();
		if (c == null || (!document.getElementById(c))) {
			var tabs = document.getElementById("patientTabs").getElementsByTagName("a");
			if (tabs.length && tabs[0].id)
				c = tabs[0].id;
		}
		
		changeTab(c);
		startBackgroundLoading();
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
	
	function startBackgroundLoading() {
		for(var key in ajaxProperties) {
			if (ajaxProperties[key] === 'Background') {
				if (document.getElementById(key + 'Tab').innerHTML === "") {
					var divId = '#' + key + 'Tab';
					$j(divId).html('<p><img src="<openmrs:contextPath/>/images/loading.gif"/><spring:message code="general.loading"/></p>');
					var keySuffix = key.substring(key.length - 9,key.length);
					var url = "";
					if (keySuffix === 'Extension') {
						var params = document.getElementById(key + 'Url').value;
						url = "patientDashboardModuleExtension.form?" + params;
					} else {
						url = "patientDashboard" + key.replace("ajax", "") + ".form?patientId=" + ${patient.patientId};
					}
					$j(divId).load(url);
					while (document.getElementById(key + 'Tab').innerHTML === "") {}
				}
			}
		}
	}
	
	function changeTab(tabObj) {
		if (!document.getElementById || !document.createTextNode) {return;}
		if (typeof tabObj == "string")
			tabObj = document.getElementById(tabObj);
		
		if (tabObj) {
			var tabs = tabObj.parentNode.parentNode.getElementsByTagName('a');
			var divId = "";
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].className.indexOf('current') != -1) {
					manipulateClass('remove', tabs[i], 'current');
				}
				var key = 'ajax' + tabs[i].id.replace("link", "").replace("Tab", "");
				if (ajaxProperties[key] == null || ajaxProperties[key] === 'Preload') {
					divId = tabs[i].id.replace("link", "content");
				} else {
					divId = tabs[i].id.replace("link", "ajax");
				}
				var divObj = document.getElementById(divId);
				if (divObj) {
					if (tabs[i].id == tabObj.id)
						divObj.style.display = "";
					else
						divObj.style.display = "none";
				}
			}
			
			var key = 'ajax' + tabObj.id.replace("link", "").replace("Tab", "");
			if (ajaxProperties[key] === 'Onclick') {
				var divId = '#' + key + 'Tab';
				var div = document.getElementById(key + 'Tab');
				div.style.display = "";
				if (div.innerHTML === "") {
					$j(divId).html('<p><img src="<openmrs:contextPath/>/images/loading.gif"/><spring:message code="general.loading"/></p>');
					var keySuffix = key.substring(key.length - 9,key.length);
					var url = "";
					if (keySuffix === 'Extension') {
						var params = document.getElementById(key + 'Url').value;
						url = "patientDashboardModuleExtension.form?" + params;
					} else {
						url = "patientDashboard" + key.replace("ajax", "") + ".form?patientId=" + ${patient.patientId};
					}
					$j(divId).load(url);
				}
			} else if (ajaxProperties[key] === 'Background') {
				var div = document.getElementById(key + 'Tab');
				div.style.display = "";
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

<div id="patientTabs" class="${patientVariation}">
	<ul>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
			<li><a id="linkOverviewTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.overview"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Regimen Section">
			<li><a id="linkRegimensTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.regimens"/></a></li>
		</openmrs:hasPrivilege>
		
		<openmrs:globalProperty key="visits.enabled" defaultValue="true" var="visitsEnabled"/>
		<c:choose>		
			<c:when test='${visitsEnabled}'>
				<openmrs:hasPrivilege privilege="Patient Dashboard - View Visits Section">
					<li><a id="linkVisitsEncountersTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.visits"/></a></li>
				</openmrs:hasPrivilege>
			</c:when>
			<c:otherwise>
				<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
					<li><a id="linkVisitsEncountersTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.encounters"/></a></li>
				</openmrs:hasPrivilege>
			</c:otherwise>
			
		</c:choose>
		
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
			<li><a id="linkDemographicsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.demographics"/></a></li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
			<li><a id="linkGraphsTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.graphs"/></a></li>
		</openmrs:hasPrivilege>
		<c:if test="${enableFormEntryTab}">
			<openmrs:hasPrivilege privilege="Form Entry">
				<li><a id="linkFormEntryTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="patientDashboard.formEntry"/></a></li>
			</openmrs:hasPrivilege>
		</c:if>
		<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
			<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
				<li>
					<a id="link${extension.tabId}ExtensionTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus"><spring:message code="${extension.tabName}"/></a>
				</li>
			</openmrs:hasPrivilege>
		</openmrs:extensionPoint>
	</ul>
</div>

<div id="patientSections">
	<c:choose>
		<c:when test="${ajaxProperties['ajaxOverview'] == 'Preload'}">
			<openmrs:hasPrivilege privilege="Patient Dashboard - View Overview Section">
				<div id="contentOverviewTab" style="display:none;">
					
					<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.OverviewTabHeader" type="html" parameters="patientId=${patient.patientId}" />
					<openmrs:portlet url="patientOverview" id="patientDashboardOverview" patientId="${patient.patientId}"/>
					
				</div>
			</openmrs:hasPrivilege>
		</c:when>
		<c:otherwise>
			<div id="ajaxOverviewTab" style="display:none;"></div>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test="${ajaxProperties['ajaxRegimens'] == 'Preload'}">
			<openmrs:hasPrivilege privilege="Patient Dashboard - View Regimen Section">	
				<div id="contentRegimensTab" style="display:none;">
		
					<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.RegimenTabHeader" type="html" parameters="patientId=${patient.patientId}" />
					<openmrs:globalProperty var="displayDrugSetIds" key="dashboard.regimen.displayDrugSetIds" defaultValue="ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS" />
					<openmrs:portlet url="patientRegimen" id="patientDashboardRegimen" patientId="${patient.patientId}" parameters="displayDrugSetIds=${displayDrugSetIds}" />
					
				</div>
			</openmrs:hasPrivilege>
		</c:when>
		<c:otherwise>
			<div id="ajaxRegimensTab" style="display:none;"></div>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test="${ajaxProperties['ajaxVisitsEncounters'] == 'Preload'}">
			<openmrs:globalProperty key="visits.enabled" defaultValue="true" var="visitsEnabled"/>
			<c:choose>		
				<c:when test='${visitsEnabled}'>
					<openmrs:hasPrivilege privilege="Patient Dashboard - View Visits Section">
						<div id="contentVisitsEncountersTab" style="display:none;">
							
							<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.VisitsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
							<openmrs:portlet url="patientVisits" id="patientDashboardVisits" patientId="${patient.patientId}" />
							
						</div>
					</openmrs:hasPrivilege>
				</c:when>
				<c:otherwise>
					<openmrs:hasPrivilege privilege="Patient Dashboard - View Encounters Section">
						<div id="contentVisitsEncountersTab" style="display:none;">
							
							<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.EncountersTabHeader" type="html" parameters="patientId=${patient.patientId}" />
							<openmrs:globalProperty var="maxEncs" key="dashboard.maximumNumberOfEncountersToShow" defaultValue="" />
							<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=${maxEncs}|showPagination=true|formEntryReturnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
							
						</div>
					</openmrs:hasPrivilege>
				</c:otherwise>	
			</c:choose>
		</c:when>
		<c:otherwise>
			<div id="ajaxVisitsEncountersTab" style="display:none;"></div>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test="${ajaxProperties['ajaxDemographics'] == 'Preload'}">
			<openmrs:hasPrivilege privilege="Patient Dashboard - View Demographics Section">
				<div id="contentDemographicsTab" style="display:none;">
					
					<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.DemographicsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
					<openmrs:portlet url="patientDemographics" id="patientDashboardDemographics" patientId="${patient.patientId}"/>
					
				</div>
			</openmrs:hasPrivilege>
		</c:when>
		<c:otherwise>
			<div id="ajaxDemographicsTab" style="display:none;"></div>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test="${ajaxProperties['ajaxGraphs'] == 'Preload'}">
			<openmrs:hasPrivilege privilege="Patient Dashboard - View Graphs Section">
				<div id="contentGraphsTab" style="display:none;">
				
					<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.GraphsTabHeader" type="html" parameters="patientId=${patient.patientId}" />
					<openmrs:portlet url="patientGraphs" id="patientGraphsPortlet" patientId="${patient.patientId}"/>
					
				</div>
			</openmrs:hasPrivilege>
		</c:when>
		<c:otherwise>
			<div id="ajaxGraphsTab" style="display:none;"></div>
		</c:otherwise>
	</c:choose>
	
	<c:choose>
		<c:when test="${ajaxProperties['ajaxFormEntry'] == 'Preload'}">
			<c:if test="${enableFormEntryTab}">
				<openmrs:hasPrivilege privilege="Form Entry">
					<div id="contentFormEntryTab" style="display:none;">
					
						<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.FormEntryTabHeader" type="html" parameters="patientId=${patient.patientId}" />
						<openmrs:portlet url="personFormEntry" id="formEntryPortlet" personId="${patient.personId}" parameters="showDecoration=true|showLastThreeEncounters=true|returnUrl=${pageContext.request.contextPath}/patientDashboard.form"/>
						
					</div>
				</openmrs:hasPrivilege>
			</c:if>
		</c:when>
		<c:otherwise>
			<div id="ajaxFormEntryTab" style="display:none;"></div>
		</c:otherwise>
	</c:choose>		
		
	<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
		<c:set var="keyValue" value="ajax${extension.tabId}Extension" scope="page"/>
		<c:choose>
			<c:when test="${ajaxProperties[keyValue] == null || ajaxProperties[keyValue] == 'Preload'}">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<div id="content${extension.tabId}ExtensionTab" style="display:none;">
						<c:catch var="ex">
							<c:choose>
								<c:when test="${extension.portletUrl == '' || extension.portletUrl == null}">
									portletId is null: '${extension.extensionId}'
								</c:when>
								<c:otherwise>
									hooo
									<openmrs:extensionPoint pointId="org.openmrs.patientDashboard.${extension.tabId}TabHeader" type="html" parameters="patientId=${patient.patientId}" />
									<openmrs:portlet url="${extension.portletUrl}" id="${extension.tabId}" moduleId="${extension.moduleId}"/>
									
								</c:otherwise>
							</c:choose>
						</c:catch>
						<c:if test="${not empty ex}">
							<div class="error">
								<spring:message code="fix.error.plain"/> <br/>
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
			</c:when>
			<c:otherwise>
				<div id="ajax${extension.tabId}ExtensionTab" style="display:none;"></div>
				<c:set var="privilege" value="${fn:replace(extension.requiredPrivilege, ' ', '@@')}" scope="page"/>
				<input type="hidden" id="ajax${extension.tabId}ExtensionUrl" value="extensionUrl=${extension.portletUrl}&extensionTabId=${extension.tabId}&extensionModuleId=${extension.moduleId}&requiredPrivilege=${privilege}&extensionId=${extension.extensionId}&patientId=${patient.patientId}" />
			</c:otherwise>
		</c:choose>
	</openmrs:extensionPoint>
	
</div>
<%@ include file="/WEB-INF/template/footer.jsp" %>