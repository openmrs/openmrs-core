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

var mySearch = null;
var findObjects = null;
var searchType = "";
var changeButton = null;

var init = function() {
	mySearch = new fx.Resize("searchForm", {duration: 100});
	mySearch.hide();
};

var findObjects = function(txt) {
	if (searchType == 'patient') {
		DWRPatientService.findPatients(fillTable, txt, 0);
	}
	else if (searchType == 'user') {
		DWRUserService.findUsers(fillTable, txt, ['Clinician'], false);
	}
	return false;
}

var onSelect = function(objs) {
	var obj = objs[0];
	if (searchType == 'patient') {
		$("patient").value = obj.patientId;
		$("patientName").innerHTML = getName(obj);
		changeButton.focus();
	}
	else if (searchType == 'user') {
		$("provider").value = obj.userId;
		$("providerName").innerHTML = getName(obj);
		changeButton.focus();
	}
	mySearch.hide();
	return false;
}

function showSearch(btn) {
	mySearch.hide();
	setPosition(btn, $("searchForm"));
	resetForm();
	DWRUtil.removeAllRows("searchBody");
	if (btn.id == "userButton") {
		$('searchTitle').innerHTML = '<spring:message code="Encounter.provider.find"/>';
		searchType = 'user';
	}
	else {
		$('searchTitle').innerHTML = '<spring:message code="Patient.find"/>';
		searchType = 'patient'
	}
	mySearch.toggle();
	$("searchText").value = '';
	$("searchText").select();
	changeButton = btn;
}

var getIdentifier = function(obj) {
	if (typeof obj == 'string')  return obj;
	if (searchType == 'patient') return obj.identifier;
	return '';
}

var getName = function(obj) {
	if (typeof obj == 'string') return '';
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

function closeBox() {
	mySearch.toggle();
	return false;
}

var customCellFunctions = [getNumber, getIdentifier, getName];

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
		height: 330px;
	}
	.searchResults {
		height: 270px;
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
				<div style="width:200px; float: left;" id="patientName">${status.value.patientName.familyName}, ${status.value.patientName.givenName}</div>
				<input type="hidden" id="patient" value="${status.value.patientId}" name="patientId"/>
				<input type="button" id="patientButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Encounter.provider"/></td>
		<td>
			<spring:bind path="encounter.provider">
				<div style="width:200px; float:left" id="providerName">${status.value.lastName}, ${status.value.firstName}</div>
				<input type="hidden" id="provider" value="${status.value.userId}" name="providerId" />
				<input type="button" id="userButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this)" />
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
	<c:if test="${encounter.encounterId != null}">
		<tr>
			<td><spring:message code="Encounter.observations" /></td>
			<td>
				<a href="${pageContext.request.contextPath}/admin/observations/obsSummary.form?encounterId=${encounter.encounterId}"><spring:message code="Encounter.observations.view"/></a>
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="hidden" name="phrase" value='<request:parameter name="phrase" />'/>
<input type="submit" value='<spring:message code="Encounter.save"/>'>
&nbsp;
<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1); return; document.location='index.htm?autoJump=false&phrase=<request:parameter name="phrase"/>'">
</form>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;">
			<h3 id="searchTitle"></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
		</form>
		<div id="searchResults" class="searchResults">
			<table cellpadding="2" cellspacing="0">
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