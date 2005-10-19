<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.PatientService" %>
<%@ page import="org.openmrs.Patient" %>
<%@ page import="org.openmrs.PatientName" %>
<%@ page import="org.openmrs.PatientIdentifier" %>
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
	var lastTab = new Array();
	lastTab["identifier"] = null;
	lastTab["name"]		  = null;
	lastTab["address"]	  = null;
	
	// Number of objects stored.  Needed for 'add new' purposes.
	// starts at -1 due to the extra 'blank' data div in the *Boxes dib
	var numObjs = new Array();
	numObjs["identifier"]	= -1;
	numObjs["name"]			= -1;
	numObjs["address"]		= -1;
	
	function initializeChildren(obj, type) {
		if (obj.hasChildNodes()) {
			var child = obj.firstChild;
			while (child != null) {
				if (child.nodeName == "DIV") {
					child.style.display = "none";
					numObjs[type] = numObjs[type] + 1;
				}
				child = child.nextSibling;
			}
		}
	}
	
	function selectTab(tab, type) {
		var data = document.getElementById(tab.id + "Data");
		if (data != null) {
			tab.className = "selected";                             //set the tab as selected
			data.style.display = "";                                //show the data box
			if (lastTab[type] != null && lastTab[type] != tab) {    //if there was a last tab
				lastTab[type].className = "";                       //set the last tab as unselected
				var lastData = document.getElementById(lastTab[type].id + "Data");
				if (lastData != null) 
					lastData.style.display = "none";                //hide last data tab
			}
			lastTab[type] = tab;             //new tab is now the last tab
			tab.blur();                      //get rid of the ugly dotted border the browser creates.
		}

		return false;
	}
	
	function addNew(obj, type) {
		var newData = document.getElementById(obj.id + "Data");
		if (newData != null) {
			var tabToClone = obj.previousSibling;
			if (tabToClone.id == null) tabToClone = tabToClone.previousSibling;
			var tabClone = tabToClone.cloneNode(true);
			tabClone.id = type + numObjs[type];
			var parent = obj.parentNode;
			parent.insertBefore(tabClone, obj);

			var dataClone = newData.cloneNode(true);
			dataClone.id = type + numObjs[type] + "Data";
			parent = newData.parentNode;
			parent.insertBefore(dataClone, newData);

			numObjs[type] = numObjs[type] + 1;
		}

		return selectTab(tabClone, type);
	}
	
	function removeTab(obj, type) {
	
		//TODO don't remove if this is the last name/id/address, etc
	
		var data = obj.parentNode;
		var tabId = data.id.substring(0, data.id.lastIndexOf("Data"));
		var tab = document.getElementById(tabId);
		var tabToSelect = null;			
		
		if (data != null && tab != null) {
			var tabparent = tab.parentNode;
			tabparent.removeChild(tab);
			var dataparent = data.parentNode;
			dataparent.removeChild(data);

			var child = tabparent.firstChild;
			while (tabToSelect == null && child != tabparent.lastChild) {
				if (child.id == null || child.className == "addNew")
					child = child.nextSibling;
				else
					tabToSelect = child;
			}
		}
		
		return selectTab(tabToSelect, type);
	}
	
	function modifyTab(obj, value, child) {
		var parent = obj.parentNode;
		while (parent.nodeName != "DIV") {
			parent = parent.parentNode;
		}
		var tabId = parent.id.substring(0, parent.id.lastIndexOf("Data"));  //strip 'Data' from div id
		var tab = document.getElementById(tabId);
		tab.childNodes[child].innerHTML = value;
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
		min-height: 150px;
	}
	.tabBox {
		height: 100%;
	}
	.addNew, .removeTab {
		font-size: 10px;
		text-align: right;
		vertical-align: bottom;
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
			<a href="javascript:null;" onClick="return addNew(this, 'identifier');" class="addNew" id="addNewIdentifier">
				Add New Identifier
				<img src="/openmrs/images/add.gif" border=0>
			</a>
		</div>
		<div class="tabBoxes" id="pIdBoxes">
			<c:forEach var="identifier" items="${patient.identifiers}" varStatus="status">
				<jsp:useBean id="identifier" type="org.openmrs.PatientIdentifier" />
				<div id="identifier${status.index}Data" class="tabBox">
					<%@ include file="/WEB-INF/include/patientIdentifier.jsp" %>
				</div>
			</c:forEach>
			<div id="addNewIdentifierData" class="tabBox">
				<%	PatientIdentifier identifier = new PatientIdentifier(); 
					pageContext.setAttribute("identifier", identifier); %>
				<%@ include file="/WEB-INF/include/patientIdentifier.jsp" %>
			</div>
		</div>
	</div>
	
	<br style="clear: both" />
	
	<h3>Patient Names</h3>
	<div id="pNames">
		<div class="tabBar" id="pNameTabBar">
			<c:forEach var="name" items="${patient.names}" varStatus="status">
				<a href="javascript:null;" onClick="return selectTab(this, 'name');" id="name${status.index}"><span>${name.givenName}</span> <span>${name.familyName}</span></a>
			</c:forEach>
			<a href="javascript:null;" onClick="return addNew(this, 'name');" class="addNew" id="addNewName">
				Add New Name
				<img src="/openmrs/images/add.gif" border="0" title="Add New Name">
			</a>
		</div>
		<div class="tabBoxes" id="pNameBoxes">
			<c:forEach var="name" items="${patient.names}" varStatus="status">
				<jsp:useBean id="name" type="org.openmrs.PatientName" />
				<div id="name${status.index}Data" class="tabBox">
					<%@ include file="/WEB-INF/include/patientName.jsp" %>
					<a href="javascript:null;" onClick="return removeTab(this, 'name');" class="removeTab">Remove this name <img src="/openmrs/images/delete.gif" border="0" title="Remove this name" /></span>
				</div>
			</c:forEach>
			<div id="addNewNameData" class="tabBox">
				<%  PatientName name = new PatientName();
					pageContext.setAttribute("name", name); %>
				<%@ include file="/WEB-INF/include/patientName.jsp" %>
				<a href="javascript:null;" onClick="return removeTab(this, 'name');" class="removeTab">Remove this name <img src="/openmrs/images/delete.gif" border="0" title="Remove this name" /></span>
			</div>
		</div>
	</div>
	
	<br style="clear: both" />
	
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
	var pNameTabBar = document.getElementById("pNameTabBar");
	var pNameBoxes  = document.getElementById("pNameBoxes");
	
	initializeChildren(pIdBoxes, "identifier");
	selectTab(document.getElementById("identifier0"), "identifier");
	
	initializeChildren(pNameBoxes, "name");
	selectTab(document.getElementById("name0"), "name");
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>