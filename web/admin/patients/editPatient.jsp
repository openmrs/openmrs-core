<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.PatientService" %>
<%@ page import="org.openmrs.Patient" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Patients" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	PatientService patientService = context.getPatientService();
	Patient patient = patientService.getPatient(Integer.valueOf(request.getParameter("patientId")));
	pageContext.setAttribute("patient", patient);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<script>
	function hideDIVChildren(obj) {
		if (obj.hasChildNodes()) {
			var child = obj.firstChild;
			while (child != null) {
				if (child.nodeName == "DIV")
					child.style.display = "none";
				child = child.nextSibling;
			}
		}
	}
	
	var lastTab = new Array();
	lastTab["identifier"] = null;
	lastTab["name"]		  = null;
	lastTab["address"]	  = null;
	
	function selectTab(tab, type) {
		var data = document.getElementById(tab.id + "Data");
		if (data != null) {
			tab.className = "selected";		//set the tab as selected
			data.style.display = "";			//show the data box
			if (lastTab[type] != null && lastTab[type] != tab) {		//if there was a last tab
				lastTab[type].className = "";	//set the last tab as unselected
				document.getElementById(lastTab[type].id + "Data").style.display = "none"; //hide last data tab
			}
			lastTab[type] = tab;				//new tab is now the last tab
			tab.blur();							//get rid of the ugly dotted line the browser creates.
		}
		return false;
	}
	
</script>

<style>
	.tabBar {
		clear: left;
		float: left;
		font-size: 11px;
		width: 150px;
	}
	.tabBar a {
		display: block;
		border-width: 2px;
		border-style: none solid none none;
		border-color: dodgerblue;
		background-color: WhiteSmoke;
		padding: 4px;
	}
	.tabBar .selected {
		border-width: 2px;
		border-style: solid none solid solid;
		border-color: dodgerblue;
	}
	.tabBoxes {
		margin-left: 148px;
		border: 2px solid DodgerBlue;
		padding: 3px;
		height: 100%;
	}
	.tabBox {
		height: 100%;
	}
	
</style>

<br />
<h2>Editing Patient</h2>

<form method="post" action="editPatientServlet">

	<h3>Patient Identifiers</h3>
	<div id="pIds">
		<div class="tabBar" id="pIdTabBar">
			<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
				<a href="javascript:null;" onClick="return selectTab(this, 'identifier');" id="identifier${status.index}">${identifier.identifierType.name}</a>
			</c:forEach>
		</div>
		<div class="tabBoxes" id="pIdBoxes">
			<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
				<div id="identifier${status.index}Data" class="tabBox">
					<%@ include file="/WEB-INF/include/patientIdentifier.jsp" %>
				</div>
			</c:forEach>
		</div>
	</div>
	
	<br style="clear: both" />
	
	<h3>Patient Names</h3>
	<div class="tabBar" id="patientNamesTabBar">
	</div>
	<div class="tabBoxes" id="patientNamesBoxes">
	</div>
	
	<h3>Patient Addresses</h3>
	<div class="tabBar" id="patientAddressesTabBar">
	</div>
	<div class="tabBoxes" id="patientAddressesBoxes">
	</div>
	
	<h3>Other Patient Information</h3>
	<div class="tabBox" id="patientInformationBox">
		<%@ include file="/WEB-INF/include/patientInfo.jsp" %>
	</div>	

	<br />
	<input type="hidden" name="patientId" value="${patient.patientId}"/>
	<input type="submit" value="Save Patient">
</form>

<script>
	var pIdTabBar = document.getElementById("pIdTabBar");
	var pIdBoxes  = document.getElementById("pIdBoxes");
	
	hideDIVChildren(pIdBoxes);
	selectTab(document.getElementById("identifier0"), "identifier");
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>