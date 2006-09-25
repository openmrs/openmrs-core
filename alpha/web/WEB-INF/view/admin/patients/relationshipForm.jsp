<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Relationships" otherwise="/login.htm" redirect="/admin/patients/relationship.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>

<script type="text/javascript">
var mySearch = null;
var searchType = "";
var changeButton = null;

var init = function() {
	addRow('relationships', 'newRelationship');
	mySearch = new fx.Resize("searchForm", {duration: 100});
	mySearch.hide();
};

function showSearch(btn, type) {
	mySearch.hide();
	if (searchType != type) {
		//if they've changed which form they want
		setPosition(btn, $("searchForm"), 465, 515);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		searchType = type;
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	else {
		searchType = "";
		changeButton.focus();
	}
}

var findObjects = function(txt) {
	if (searchType == "relation" || searchType == "patient") {
		DWRPatientService.findPatients(fillTable, txt, 0);
	}
	else if (searchType == "user") {
		DWRUserService.findUsers(fillTable, txt, [], 0);
	}
}

var onSelect = function(objs) {
	var obj = objs[0];
	if (searchType == "relation") {
		var parent = changeButton.parentNode.parentNode;	// parent = tbody containing button 
		var cells = parent.childNodes;
		for (var x = 0; x<cells.length; x++) {
			if (cells[x].id == 'patientName')
				cells[x].innerHTML = getName(obj);
			else if (cells[x].id == 'patient') {
				var inputs = cells[x].childNodes;
				for (var i = 0; i<inputs.length; i++)
					if (inputs[i].id == 'patient')
						inputs[i].value = obj.patientId;
			}
		}
	}
	else if (searchType == "patient") {
		//var parent = changeButton.parentNode;
		$('patient').value = obj.patientId;
		$('patientName').innerHTML = getName(obj);
	}
	else if (searchType == "user") {
		$('user').value = obj.userId;
		$('userName').innerHTML = getName(obj);
	}
	mySearch.hide();
	changeButton.focus();
	searchType = "";
	changeButton = null;
	return false;
	
}

var getName = function(obj) {
	if (typeof obj == 'string') return obj;
	str = '';
		
	if (searchType == 'user') {
		str += obj.firstName;
		str += ' ';
		str += obj.lastName;
	}
	else {
		str += obj.givenName;
		str += ' ';
		str += obj.middleName;
		str += ' ';
		str += obj.familyName;
	}
	
	return str;
}

function addRow(tableId, newRowId) {
	var table = document.getElementById(tableId);
	var row = document.getElementById(newRowId);
	var newrow = row.cloneNode(true);
	newrow.style.display = "";
	newrow.id = table.childNodes.length;
	table.appendChild(newrow);
}

function clearRow(btn) {
	var row = btn.parentNode.parentNode;
	var table = row.parentNode;
	table.removeChild(row);
	if (table.getElementsByTagName("tr").length == 1)
		addRow("relationships", "newRelationship");
}

function closeBox() {
	searchType = "";
	mySearch.hide();
	return false;
}

var customCellFunctions = [getNumber, getName];

var oldonload = window.onload;
if (typeof window.onload != 'function') {
	window.onload = init;
} else {
	window.onload = function() {
		oldonload();
		init();
	}
}

function removeHiddenRows() {
	var rows = document.getElementsByTagName("TR");
	var i = 0;
	while (i < rows.length) {
		if (rows[i].style.display == "none") {
			rows[i].parentNode.removeChild(rows[i]);
		}
		else {
			i = i + 1;
		}
	}
}

</script>

<style>
	#newRelationship {
		display: none;
	}
	.searchForm {
		width: 450px;
		position: absolute;
		z-index: 10;
		margin: 5px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 500px;
	}
	.searchResults {
		height: 415px;
		overflow: auto;
		width: 440px;
	}
	.smallButton {
		margin: 2px;
	}
</style>

<br />
<h2><spring:message code="Relationship.title"/></h2>

<spring:hasBindErrors name="patient">
	<div class="error">Please fix all errors</div>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post">

	<table>
		<tr>
			<td valign="top"><b><spring:message code="Relationship.patient"/></b>:</td>
			<td valign="top">
				<div id="patientName">
					${person.patient.patientName.givenName} ${person.patient.patientName.middleName} ${person.patient.patientName.familyName}
				</div>
			</td>
			<c:if test="${person.patient == null}">
				<td valign="top">
					<input type="hidden" id="patient" value="" name="patientId"/>
					<input type="button" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearch(this, 'patient')" />
				</td>
			</c:if>
		</tr>
		<tr>
			<td valign="top"><b><spring:message code="Relationship.user"/></b>:</td>
			<td valign="top">
				<input type="hidden" id="user" value="" name="userId"/>
				<div id="userName">${person.user.firstName} ${person.user.lastName}</div>
			</td>
			<td valign="top"><input type="button" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearch(this, 'user')" /></td>
		</tr>
	</table>

	<br/><br/>
	
	<h4><spring:message code="Relationship.relationships"/></h4>
	<table id="relationships" cellspacing="0" cellpadding="2">
		<tr>
			<th> </th>
			<th> <spring:message code="Relationship.person"/> </th>
			<th> <spring:message code="Relationship.relationshipType"/> </th>
			<th> <spring:message code="Relationship.relative"/> </th>
		</tr>
		<tbody>
			<c:forEach items="${relationships}" var="relation">
				<tr <c:if test="${relation.voided}">class="voided"</c:if>>
					<td><input type="checkbox" name="relationshipId" value="${relation.relationshipId}"/></td>
					<td>
						${relation.person.patient.patientName.givenName} ${relation.person.patient.patientName.middleName} ${relation.person.patient.patientName.familyName}<spring:message code="Relationship.possessive"/>
					</td>
					<td>${relation.relationship.name}:</td>
					<td>${relation.relative.patient.patientName.givenName} ${relation.relative.patient.patientName.middleName} ${relation.relative.patient.patientName.familyName}</td>
					<c:if test="${relation.voided}">
						<td>
							<spring:message code="general.voidedBy"/>
							${relation.voidedBy.firstName} ${relation.voidedBy.lastName} -
							<openmrs:formatDate date="${relation.dateVoided}" type="medium" />
						</td>
					</c:if>
				</tr>
			</c:forEach>
			<tr id="newRelationship" style="display: none">
				<td></td>
				<td>
					${person.patient.patientName.givenName} ${person.patient.patientName.middleName} ${person.patient.patientName.familyName}<spring:message code="Relationship.possessive"/>
				</td>
				<td>
					<select name="relationshipType">
						<option value=""></option>
						<c:forEach items="${relationshipTypes}" var="type">
							<option value="${type.relationshipTypeId}">${type.name}</option>
						</c:forEach>
					</select>:
				</td>
				<td id="patientName">
					_______________
				</td>
				<td id="patient">
					<input type="hidden" id="patient" value="" name="patientId"/>
					<input type="button" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearch(this, 'relation')" />
					<input type="button" class="closeButton" style="float: none" value='<spring:message code="general.clear"/>' onclick="clearRow(this)" />
				</td>
			</tr>
		</tbody>
	</table>
	<input type="button" class="smallButton" style="width: 115px" value='<spring:message code="Relationship.add"/>' onclick="addRow('relationships', 'newRelationship')"/>
	<br /><br />
	<input type="submit" name="action" value='<spring:message code="Relationship.save"/>' onclick="removeHiddenRows()" />
	<input type="submit" name="action" value='<spring:message code="Relationship.void"/>' />!
	<input type="submit" name="action" value='<spring:message code="Relationship.unvoid"/>' />!
</form>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, event, false, 0); return false;">
			<h3><spring:message code="general.search"/></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);"> &nbsp;
			<!-- <input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label> -->
		</form>
		<div id="searchResults" class="searchResults">
			<table width="100%">
				<tbody id="searchBody">
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>