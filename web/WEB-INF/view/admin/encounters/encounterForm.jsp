<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" redirect="/admin/encounters/encounter.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>

<script type="text/javascript">

var myPatientSearch = null;
var myUserSearch = null;
var findObjects = null;
var searchType = "";
var changeButton = null;

var init = function() {
	myPatientSearch = new fx.Resize("patientSearchForm", {duration: 100});
	myPatientSearch.hide();
	myUserSearch = new fx.Resize("userSearchForm", {duration: 100});
	myUserSearch.hide();
};

var findObjects = function(txt) {
	if (searchType == 'patient') {
		DWRPatientService.findPatients(fillTable, txt, 0);
	}
	else if (searchType == 'user') {
		DWRUserService.findUsers(fillTable, txt, [], false);
	}
	return false;
}

var onSelect = function(objs) {
	var obj = objs[0];
	if (searchType == 'patient') {
		$("patient").value = obj.patientId;
		$("patientName").innerHTML = getName(obj);
		myPatientSearch.hide();
		changeButton.focus();
	}
	else if (searchType == 'user') {
		$("provider").value = obj.userId;
		$("providerName").innerHTML = getName(obj);
		myUserSearch.hide();
		changeButton.focus();
	}
	return false;
}

function showPatientSearch(btn) {
	setPosition(btn, $("patientSearchForm"));
	resetForm();
	DWRUtil.removeAllRows("patientSearchBody");
	searchType = "patient";
	myPatientSearch.toggle();
	myUserSearch.hide();
	$("patientSearchText").value = '';
	$("patientSearchText").select();
	changeButton = btn;
}

function showUserSearch(btn) {
	setPosition(btn, $("userSearchForm"));
	resetForm();
	DWRUtil.removeAllRows("userSearchBody");
	searchType = "user";
	myUserSearch.toggle();
	myPatientSearch.hide();
	$("userSearchText").value = '';
	$("userSearchText").select();
	changeButton = btn;
}

var getName = function(obj) {
	if (typeof obj == 'string') return obj;
	str = '';
	if (searchType == 'patient') {
		str += obj.familyName;
		str += ', ';
		str += obj.givenName;
	}
	else if (searchType == 'user') {
		str += obj.lastName;
		str += ', ';
		str += obj.firstName;
		return str;
	}
	return str;
}

function setPosition(btn, form) {
	var left  = getElementLeft(btn) + btn.offsetWidth + 20;
	var top   = getElementTop(btn)-50;
	var formWidth  = 520;
	var formHeight = 280;
	var windowWidth = window.innerWidth + getScrollOffsetX();
	var windowHeight = window.innerHeight + getScrollOffsetY();
	if (left + formWidth > windowWidth)
		left = windowWidth - formWidth - 10;
	if (top + formHeight > windowHeight)
		top = windowHeight - formHeight - 10;
	form.style.left = left + "px";
	form.style.top = top + "px";
}

function getElementLeft(elm) {
	var x = 0;
	while (elm != null) {
		x+= elm.offsetLeft;
		elm = elm.offsetParent;
	}
	return parseInt(x);
}

function getElementTop(elm) {
	var y = 0;
	while (elm != null) {
		y+= elm.offsetTop;
		elm = elm.offsetParent;
	}
	return parseInt(y);
}

function getScrollOffsetY() {
	if (window.innerHeight) {
		return window.pageYOffset;
	}
	else {
		return document.documentElement.scrollTop;
	}
}

function getScrollOffsetX() {
	if (window.innerWidth) {
		return window.pageXOffset;
	}
	else {
		return document.documentElement.scrollLeft;
	}
}

function closePatientBox() {
	myPatientSearch.toggle();
	return false;
}

function closeUserBox() {
	myUserSearch.toggle();
	return false;
}

function hideBoxes() {
	myUserSearch.hide();
	myPatientSearch.hide();
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

</script>

<style>
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
		height: 275px;
	}
	.searchResults {
		height: 220px;
		overflow: auto;
	}
</style>

<h2><spring:message code="Encounter.title"/></h2>

<spring:hasBindErrors name="encounter">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><spring:message code="general.id"/></td>
		<td>
			<spring:bind path="encounter.encounterId">
				${status.value}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.type"/></td>
		<td>
			<spring:bind path="encounter.encounterType">
				<select name="encounterType">
					<c:forEach items="${encounterTypes}" var="type">
						<option value="${type.encounterTypeId}" <c:if test="${type.encounterTypeId == status.value}">selected</c:if>>${type.name}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.patient"/></td>
		<td>
			<spring:bind path="encounter.patient">
				<div style="width:100px; float: left;" id="patientName">${status.value.patientName.familyName}, ${status.value.patientName.givenName}</div>
				<input type="hidden" id="patient" value="${status.value.patientId}" name="patientId"/>
				<input type="button" id="patientButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showPatientSearch(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.provider"/></td>
		<td>
			<spring:bind path="encounter.provider">
				<div style="width:100px; float:left" id="providerName">${status.value.lastName}, ${status.value.firstName}</div>
				<input type="hidden" id="provider" value="${status.value.userId}" name="providerId" />
				<input type="button" id="userButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showUserSearch(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.location"/></td>
		<td>
			<spring:bind path="encounter.location">
				<select name="location">
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}" <c:if test="${status.value == record.locationId}">selected</c:if>>${record.name}</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.form"/></td>
		<td>
			<spring:bind path="encounter.form">
				<select name="form">
					<option value=""></option>
					<c:forEach items="${forms}" var="form">
						<option value="${form.formId}" <c:if test="${form.formId == status.value}">selected</c:if>>${form.name}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.datetime"/></td>
		<td>
			<spring:bind path="encounter.encounterDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(encounter.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${encounter.creator.firstName} ${encounter.creator.lastName} -
				<openmrs:formatDate date="${encounter.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="hidden" name="phrase" value="<request:parameter name="phrase" />"/>
<input type="submit" value="<spring:message code="Encounter.save"/>">
</form>

<div id="patientSearchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closePatientBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('patientSearchBody', patientSearchText, null, false, 0); return false;">
			<h3><spring:message code="Patient.find"/></h3>
			<input type="text" id="patientSearchText" size="35" onkeyup="return searchBoxChange('patientSearchBody', this, event, false, 400);">
		</form>
		<div id="patientSearchResults" class="searchResults">
			<table>
				<tbody id="patientSearchBody">
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<div id="userSearchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeUserBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('userSearchBody', userSearchText, null, false, 0); return false;">
			<h3><spring:message code="User.find"/></h3>
			<input type="text" id="userSearchText" size="35" onkeyup="return searchBoxChange('userSearchBody', this, event, false, 400);">
		</form>
		<div id="userSearchResults" class="searchResults">
			<table>
				<tbody id="userSearchBody">
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