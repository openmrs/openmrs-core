<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Observations" otherwise="/login.htm" redirect="/admin/observations/obs.form" />

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
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWREncounterService.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>

<script type="text/javascript">

var mySearch = null;
var searchType = "";
var changeButton = null;

var init = function() {
	mySearch = new fx.Resize("searchForm", {duration: 100});
	mySearch.hide();
	<c:if test="${obs.concept.conceptId != null}">
		DWRConceptService.getConcept(updateObsValues, '${obs.concept.conceptId}');
	</c:if>
	<c:if test="${obs.concept.conceptId == null}">
		updateObsValues();
	</c:if>
};

var findObjects = function(txt) {
	if (searchType == 'patient') {
		DWRPatientService.findPatients(fillTable, txt, 0);
	}
	else if (searchType == 'concept' || searchType == 'valueCoded') {
		DWRConceptService.findConcepts(fillTable, txt, [], 0);
	}
	else if (searchType == 'encounter') {
		DWREncounterService.findEncounters(fillTable, txt, 0);
	}
	return false;
}

var onSelect = function(objs) {
	var obj = objs[0];
	if (searchType == 'patient') {
		$("patient").value = obj.patientId;
		$("patientName").innerHTML = getName(obj);
	}
	else if (searchType == 'concept') {
		$("concept").value = obj.conceptId;
		$("conceptName").innerHTML = obj.name;
		$("conceptDescription").innerHTML = obj.description;
		updateObsValues(obj);
	}
	else if (searchType == 'valueCoded') {
		$("valueCoded").value = obj.conceptId;
		$("valueCodedName").innerHTML = obj.name;
		$("valueCodedDescription").innerHTML = obj.description;
	}
	else if (searchType == 'encounter') {
		$("encounter").value = obj.encounterId;
		$("encounterName").innerHTML = getName(obj);
	}
	mySearch.hide();
	searchType = "";
	changeButton.focus();
	return false;
}

function updateObsValues(concept) {
	var values = ['valueBoolean', 'valueCoded', 'valueDatetime', 'valueModifier', 'valueText', 'valueNumeric'];
	for (var i=0; i<values.length; i++)
		$(values[i]).style.display = "none";
	
	if (concept != null) {
		var datatype = concept.datatype;
		if (typeof datatype != 'string')
			datatype = concept.datatype.name;
			
		if (datatype == 'Boolean')
			$('valueBoolean').style.display = "";
		if (datatype == 'Numeric' || datatype == 'Structured Numeric')
			$('valueNumeric').style.display = "";
		if (datatype == 'Coded')
			$('valueCoded').style.display = "";
		if (datatype == 'Text')
			$('valueText').style.display = "";
		if (datatype == 'Date' || datatype == 'Datetime' || datatype == 'Time')
			$('valueDatetime').style.display = "";
	}
}

function showSearch(btn, type) {
	mySearch.hide();
	if (searchType != type) {
		//if they've changed which form they want
		setPosition(btn, $("searchForm"));
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

var getName = function(obj) {
	if (typeof obj == 'string') return obj;
	str = '';
	if (searchType == 'patient') {
		str += obj.familyName;
		str += ', ';
		str += obj.givenName;
	}
	else if (searchType == 'concept' || searchType == 'valueCoded') {
		return getCellContent(obj);
	}
	else if (searchType == 'encounter') {
		str += obj.patientName;
		str += ' - ';
		str += obj.location;
		str += ' - ';
		str += getDateString(obj.encounterDateTime);
	}	
	return str;
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
</style>

<h2><spring:message code="Obs.title"/></h2>

<spring:hasBindErrors name="obs">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br/>
</spring:hasBindErrors>
<form method="post" onSubmit="removeHiddenRows()">
<table>
	<tr>
		<td><spring:message code="general.id"/></td>
		<td>
			<spring:bind path="obs.obsId">
				${status.value}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.patient"/></td>
		<td>
			<spring:bind path="obs.patient">
				<div style="width:200px; float: left;" id="patientName">${status.value.patientName.givenName} ${status.value.patientName.middleName} ${status.value.patientName.familyName}</div>
				<c:if test="${obs.obsId == null}">
					<input type="hidden" id="patient" value="${status.value.patientId}" name="patientId"/>
					<input type="button" id="patientButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'patient')" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.concept"/></td>
		<td>
			<spring:bind path="obs.concept">
				<div style="width:200px; float:left;" id="conceptName">${conceptName}</div>
				<c:if test="${obs.obsId == null}">
					<input type="hidden" id="concept" value="${status.value.conceptId}" name="conceptId" />
					<input type="button" id="conceptButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'concept')" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</c:if>
				<div class="description" style="clear: left;" id="conceptDescription">${conceptName.description}</div>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.encounter"/></td>
		<td>
			<spring:bind path="obs.encounter">
				<input type="text" size="7" id="encounter" value="${status.value.encounterId}" name="encounterId" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:if test="${obs.obsId == null}">
					<input type="button" id="encounterButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'encounter')" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.order"/></td>
		<td>
			<spring:bind path="obs.order">
				<input type="text" name="orderId" id="order" value="${status.value.orderId}" size="7" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.datetime"/></td>
		<td>
			<spring:bind path="obs.obsDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				(<spring:message code="general.format"/>: ${datePattern})
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.location"/></td>
		<td>
			<spring:bind path="obs.location">
				<select name="location" <c:if test="${obs.obsId != null}">disabled</c:if>>
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}" <c:if test="${status.value == record.locationId}">selected</c:if>>${record.name}</option>
					</openmrs:forEachRecord>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.accessionNumber"/></td>
		<td>
			<spring:bind path="obs.accessionNumber">
				<input type="text" name="${status.expression}" id="accessionNumber" value="${status.value}" size="10" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.valueGroupId"/></td>
		<spring:bind path="obs.valueGroupId">
			<td>
				<input type="text" name="${status.expression}" id="valueGroupId" value="${status.value}" size="10" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueBoolean">
		<td><spring:message code="Obs.valueBoolean"/></td>
		<spring:bind path="obs.valueBoolean">
			<td>
				<select name="${status.expression}">
					<option value=""></option>
					<option value="true" <c:if test="${status.value == true}">selected</c:if>><spring:message code="general.true"/></option>
					<option value="false" <c:if test="${status.value != '' && status.value == false}">selected</c:if>><spring:message code="general.false"/></option>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueCoded">
		<td><spring:message code="Obs.valueCoded"/></td>
		<td>
			<spring:bind path="obs.valueCoded">
				<input type="text" id="valueCoded" value="${status.value.conceptId}" name="valueCodedId" size="7"/>
				<div style="width:200px; float:left; display: none;" id="valueCodedName">${status.value.conceptId}&nbsp;</div>
				<input type="button" id="valueCodedButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'valueCoded')" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				<div class="description" id="valueCodedDescription"></div>
			</spring:bind>
		</td>
	</tr>
	<tr id="valueDatetime">
		<td><spring:message code="Obs.valueDatetime"/></td>
		<td>
			<spring:bind path="obs.valueDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr id="valueNumeric">
		<td><spring:message code="Obs.valueNumeric"/></td>
		<spring:bind path="obs.valueNumeric">
			<td>
				<input type="text" name="${status.expression}" id="valueNumeric" value="${status.value}" size="10"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueModifier">
		<td><spring:message code="Obs.valueModifier"/></td>
		<spring:bind path="obs.valueModifier">
			<td>
				<input type="text" name="${status.expression}" id="valueModifier" value="${status.value}" size="3" maxlength="2"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueText">
		<td><spring:message code="Obs.valueText"/></td>
		<spring:bind path="obs.valueText">
			<td>
				<textarea name="${status.expression}" rows="3" cols="35">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr>
		<td><spring:message code="Obs.dateStarted"/></td>
		<td>
			<spring:bind path="obs.dateStarted">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.dateStopped"/></td>
		<td>
			<spring:bind path="obs.dateStopped">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Obs.comment"/></td>
		<spring:bind path="obs.comment">
			<td>
				<textarea name="${status.expression}" rows="2" cols="45">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${!(obs.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${obs.creator.firstName} ${obs.creator.lastName} -
				<openmrs:formatDate date="${obs.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
		<td><spring:message code="general.voided"/></td>
		<td>
			<spring:bind path="obs.voided">
				<input type="hidden" name="_${status.expression}">
				<input type="checkbox" name="${status.expression}" 
					   id="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> 
				/>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.voidReason"/></td>
		<spring:bind path="obs.voidReason">
			<td>
				<input type="text" name="${status.expression}" id="voidReason" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${obs.voided}" >
		<tr>
			<td><spring:message code="general.voidedBy"/></td>
			<td>
				${obs.voidedBy.firstName} ${obs.voidedBy.lastName} -
				<openmrs:formatDate date="${obs.dateVoided}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<input type="hidden" name="phrase" value="<request:parameter name="phrase" />"/>
<br /><br />

<c:if test="${obs.obsId != null}">
		<spring:message code="Obs.edit.reason"/> <input type="text" value="${editReason}" size="40" name="editReason"/>
		<spring:hasBindErrors name="obs">
			<c:forEach items="${errors.allErrors}" var="error">
				<c:if test="${error.code == 'editReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
			</c:forEach>
		</spring:hasBindErrors>
	<br/><br/>
</c:if>

<input type="submit" value="<spring:message code="Obs.save"/>" >
&nbsp; 
<input type="button" value="<spring:message code="general.cancel"/>" onclick="history.go(-1);">
</form>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, event, false, 0); return false;">
			<h3><spring:message code="general.search"/></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);"> &nbsp;
			<input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
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
