<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Observations" otherwise="/login.htm" redirect="/admin/observations/obs.form" />

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
var concept = null;
var pageLoad = true;

var init = function() {
	mySearch = new fx.Resize("searchForm", {duration: 100});
	mySearch.hide();
	<c:if test="${obs.concept.conceptId != null}">
		DWRConceptService.getConcept(updateObsValues, '${obs.concept.conceptId}');
	</c:if>
	<c:if test="${obs.concept.conceptId == null}">
		updateObsValues();
	</c:if>
	<c:if test="${obs.valueCoded != null && obs.valueDrug == null}">
		searchType = "valueCoded";
		DWRConceptService.getConcept(onSelect, '${obs.valueCoded.conceptId}');
	</c:if>
	<c:if test="${obs.valueDrug != null}">
		searchType = "valueDrug";
		DWRConceptService.getDrug(onSelect, '${obs.valueDrug.drugId}');
	</c:if>
};

var findObjects = function(txt) {
	if (searchType == 'patient') {
		DWRPatientService.findPatients(fillTable, txt, 0);
	}
	else if (searchType == 'concept') {
		DWRConceptService.findConcepts(fillTable, txt, [], 0, ['N/A']);
	}
	else if (searchType == 'valueCoded') {
		DWRConceptService.findConceptAnswers(preFillTable, txt, concept, 0);
	}
	else if (searchType == 'valueDrug') {
		DWRConceptService.findDrugs(fillTable, txt);
	}
	else if (searchType == 'encounter') {
		DWREncounterService.findEncounters(fillTable, txt, 0);
	}
	return false;
}

function preFillTable(concepts) {
	// append "Propose Concept" box
	concepts.push("<a href='#proposeConcept' onclick='javascript:return showProposeConceptForm();'><spring:message code="ConceptProposal.propose.new"/></a>");
	fillTable(concepts);
}

function showProposeConceptForm() {
	var qs = "?";
	var encounterId = $("encounter").value;
	if (encounterId != "")
		qs += "&encounterId=" + encounterId;
	var obsConceptId = $("concept").value
	if (obsConceptId != "")
		qs += "&obsConceptId=" + obsConceptId;
	document.location = "${pageContext.request.contextPath}/admin/concepts/proposeConcept.form" + qs;
}

var onSelect = function(objs) {
	var obj;
	if (objs instanceof Array)
		obj = objs[0];
	else
		obj = objs;
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
		$("valueCodedId").value = obj.conceptId;
		$("valueCodedName").innerHTML = obj.name;
		$("valueCodedDescription").innerHTML = obj.description;
	}
	else if (searchType == 'valueDrug') {
		$("valueCodedId").value = obj.conceptId;
		$("valueDrugId").value = obj.drugId;
		$("valueCodedName").innerHTML = obj.fullName;
	}
	else if (searchType == 'encounter') {
		$("encounter").value = obj.encounterId;
		$("encounterName").innerHTML = getName(obj);
	}
	
	if (searchType == 'valueCoded' && obj.className == "Drug" && pageLoad == false) {
		resetForm();
		searchType = "valueDrug";
		DWRConceptService.getDrugs(fillTable, obj.conceptId, true);
	}
	else {
		mySearch.hide();
		searchType = "";
		if (changeButton != null)
			changeButton.focus();
	}
	pageLoad = false;
	
	return false;
}

function updateObsValues(tmpConcept) {
	concept = tmpConcept;
	var values = ['valueBoolean', 'valueCoded', 'valueDatetime', 'valueModifier', 'valueText', 'valueNumeric', 'valueInvalid'];
	for (var i=0; i<values.length; i++)
		$(values[i]).style.display = "none";
	
	if (tmpConcept != null) {
		var datatype = tmpConcept.hl7Abbreviation;
		if (typeof datatype != 'string')
			datatype = tmpConcept.datatype.hl7Abbreviation;
			
		if (datatype == 'BIT')
			$('valueBoolean').style.display = "";
		else if (datatype == 'NM' || datatype == 'SN') {
			$('valueNumeric').style.display = "";
			DWRConceptService.getConceptNumericUnits(fillNumericUnits, tmpConcept.conceptId);
		}
		else if (datatype == 'CWE')
			$('valueCoded').style.display = "";
		else if (datatype == 'ST')
			$('valueText').style.display = "";
		else if (datatype == 'DT' || datatype == 'TS' || datatype == 'TM')
			$('valueDatetime').style.display = "";
		// TODO move datatype 'TM' to own time box.  How to have them select?
		else {
			$('valueInvalid').style.display = "";
			//DWRConceptService.getQuestionsForAnswer(fillValueInvalidPossible, tmpConcept.conceptId);
		}
	}
}

function showSearch(btn, type) {
	mySearch.hide();
	if (searchType != type) {
		//if they've changed which form they want
		setPosition(btn, $("searchForm"), 465, 515);
		resetForm();
		mySearch.toggle();
		DWRUtil.removeAllRows("searchBody");
		searchType = type;
		var searchText = $("searchText");
		searchText.value = '';
		searchText.select();
		changeButton = btn;
		if (searchType == 'valueCoded') {
			searchBoxChange('searchBody', searchText, null, false, 0);	//set up textbox, event, etc
			// wait to call dwr until our box has finished expanding
			setTimeout("DWRConceptService.findConceptAnswers(fillTable, '', concept, 0)", 150); //force lookup on blank string
		}
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
	else if (searchType == 'valueDrug') {
		return obj.fullName;
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

function fillNumericUnits(units) {
	$('numericUnits').innerHTML = units;
}

function validateNumericRange(value) {
	if (!isNaN(value)) {
		DWRConceptService.isValidNumericValue(numericErrorMessage, value, concept.conceptId);
	}
}

function numericErrorMessage(validValue) {
	var errorTag = $('numericRangeError');

	if (validValue == false) {
		errorTag.innerHTML = "<spring:message code="ConceptNumeric.invalid.msg"/>";
		errorTag.className = "error";
	}
	else {
		errorTag.innerHTML = "";
		errorTag.className = "";
	}
}

function fillValueInvalidPossible(questions) {
	var div = $('valueInvalidPossibleConcepts');
	var txt = documentCreateTextNode('<spring:message code="Obs.valueInvalid.didYouMean"/> ');
	for (var i=0; i<questions.length && i < 5; i++) {
		if (i == 0)
			div.appendChild(txt);
		var concept = questions[i];
		var link = document.createElement("a");
		link.href = "#selectAsQuestion";
		link.onclick = function() { selectNewQuestion(concept.conceptId); return false; };
		link.title = concept.description;
		link.innerHTML = concept.name;
		if (i == (questions.length - 1))
			link.innerHTML += "?";
		else
			link.innerHTML += ", ";
		div.appendChild(link);
	}
}

function selectNewQuestion(conceptId) {
	searchType = "concept";
	DWRConceptService.getConcept(onSelect, conceptId);
}

function allowAutoListWithNumber() {
	return true;
}

</script>

<style>
	th {
		text-align: left;
	}
	.searchForm {
		width: 450px;
		position: absolute;
		z-index: 10;
		left: -1000px;
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
	#numericRangeError {
		font-weight: bold;
		padding: 2px 4px 2px 4px;
	}
	.numericRangeErrorNormal {
		background-color: green;
		color: white;
	}
	.numericRangeErrorCritical {
		background-color: yellow;
		color: black;
	}
	.numericRangeErrorAbsolute {
		background-color: orange;
		color: white;
	}
	.numericRangeErrorInvalid {
		background-color: red;
		color: white;
	}
</style>

<h2><spring:message code="Obs.title"/></h2>

<spring:hasBindErrors name="obs">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.globalErrors}" var="error">
			<spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br/>
</spring:hasBindErrors>
<form method="post" onSubmit="removeHiddenRows()">
<table>
	<c:if test="${obs.obsId != null}">
		<tr>
			<th><spring:message code="general.id"/></th>
			<td>
				<spring:bind path="obs.obsId">
					${status.value}
				</spring:bind>
			</td>
		</tr>
	</c:if>
	<tr>
		<th><spring:message code="Obs.patient"/></th>
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
		<th><spring:message code="Obs.encounter"/></th>
		<td>
			<spring:bind path="obs.encounter">
				<input type="text" size="7" id="encounter" value="${status.value.encounterId}" name="encounterId" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:choose>
					<c:when test="${obs.encounter == null}">
						<input type="button" id="encounterButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'encounter')" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</c:when>
					<c:otherwise>
						<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${status.value.encounterId}"><spring:message code="general.view"/>/<spring:message code="general.edit"/></a>
					</c:otherwise>
				</c:choose>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.order"/></th>
		<td>
			<spring:bind path="obs.order">
				<input type="text" name="orderId" id="order" value="${status.value.orderId}" size="7" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.concept"/></th>
		<td>
			<spring:bind path="obs.concept">
				<c:choose>
					<c:when test="${obs.obsId != null}">
						<div id="conceptName">${conceptName}</div>
					</c:when>
					<c:otherwise>
						<div style="width:200px; float:left;" id="conceptName">${conceptName}</div>
						<input type="hidden" id="concept" value="${status.value.conceptId}" name="conceptId" />
						<input type="button" id="conceptButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'concept')" />
					</c:otherwise>
				</c:choose>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				<div class="description" style="clear: left;" id="conceptDescription">${conceptName.description}</div>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.datetime"/></th>
		<td>
			<spring:bind path="obs.obsDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" id="${status.expression}" />
				(<spring:message code="general.format"/>: ${datePattern})
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.location"/></th>
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
	<c:if test="${1 == 2}">
		<tr>
			<th><spring:message code="Obs.accessionNumber"/></th>
			<td>
				<spring:bind path="obs.accessionNumber">
					<input type="text" name="${status.expression}" id="accessionNumber" value="${status.value}" size="10" <c:if test="${obs.obsId != null}">disabled</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Obs.valueGroupId"/></th>
			<spring:bind path="obs.valueGroupId">
				<td>
					<input type="text" name="${status.expression}" id="valueGroupId" value="${status.value}" size="10" <c:if test="${obs.obsId != null}">disabled</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</td>
			</spring:bind>
		</tr>
	</c:if>
	<tr id="valueBoolean">
		<th><spring:message code="general.value"/></th>
		<spring:bind path="obs.valueNumeric">
			<td>
				<select name="${status.expression}">
					<option value="" <c:if test="${status.value == null}">selected</c:if>></option>
					<option value="1" <c:if test="${status.value != 0}">selected</c:if>><spring:message code="general.true"/></option>
					<option value="0" <c:if test="${status.value == 0}">selected</c:if>><spring:message code="general.false"/></option>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueCoded">
		<th><spring:message code="general.value"/></th>
		<td>
			<spring:bind path="obs.valueCoded">
				<table>
					<tr>
						<td>
							<input type="hidden" id="valueCodedId" value="${status.value.conceptId}" name="valueCodedId" />
							<div id="valueCodedName">${status.value.conceptId}&nbsp;</div>
						</td>
						<td >
							<input type="button" id="valueCodedButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'valueCoded')" />
						</td>
						<td><c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if></td>
					</tr>
				</table>
				<div class="description" id="valueCodedDescription"></div>
			</spring:bind>
			<spring:bind path="obs.valueDrug">
				<input type="hidden" id="valueDrugId" value="${status.value.drugId}" name="valueDrugId" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="valueDatetime">
		<th><spring:message code="general.value"/></th>
		<td>
			<spring:bind path="obs.valueDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				  (<spring:message code="general.format"/>: ${datePattern})
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr id="valueNumeric">
		<th><spring:message code="general.value"/></th>
		<spring:bind path="obs.valueNumeric">
			<td>
				<input type="text" name="${status.expression}" value="${status.value}" size="10" onKeyUp="validateNumericRange(this.value)"/>
				<span id="numericUnits"></span>
				<span id="numericRangeError"></span>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueModifier">
		<th><spring:message code="Obs.valueModifier"/></th>
		<spring:bind path="obs.valueModifier">
			<td>
				<input type="text" name="${status.expression}" id="valueModifier" value="${status.value}" size="3" maxlength="2"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueText">
		<th><spring:message code="general.value"/></th>
		<spring:bind path="obs.valueText">
			<td>
				<textarea name="${status.expression}" rows="3" cols="35">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueInvalid">
		<th><spring:message code="general.value"/></th>
		<td>
			<div class="error"><spring:message code="Obs.valueInvalid.description"/></div>
			<div id="valueInvalidPossibleConcepts"></div>
		</td>
	</tr>
	<!-- 
	<tr>
		<th><spring:message code="Obs.dateStarted"/></th>
		<td>
			<spring:bind path="obs.dateStarted">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.dateStopped"/></th>
		<td>
			<spring:bind path="obs.dateStopped">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	-->
	<tr>
		<th><spring:message code="Obs.comment"/></th>
		<spring:bind path="obs.comment">
			<td>
				<textarea name="${status.expression}" rows="2" cols="45">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${!(obs.creator == null)}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${obs.creator.firstName} ${obs.creator.lastName} -
				<openmrs:formatDate date="${obs.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${obs.obsId != null}">
		<tr>
			<th><spring:message code="general.voided"/></th>
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
	</c:if>
	<c:if test="${fn:length(obs.voidReason) > 0}">
		<tr>
			<th><spring:message code="general.voidReason"/></th>
			<td>${obs.voidReason}</td>
		</tr>
	</c:if>
	<c:if test="${obs.voided == true && obs.voidedBy != null}" >
		<tr>
			<th><spring:message code="general.voidedBy"/></th>
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
		<b><spring:message code="Obs.edit.reason"/></b> <input type="text" value="${editReason}" size="40" name="editReason"/>
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
