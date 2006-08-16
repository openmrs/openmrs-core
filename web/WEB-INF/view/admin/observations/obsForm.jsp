<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Observations" otherwise="/login.htm" redirect="/admin/observations/obs.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.EncounterSearch");
	dojo.require("dojo.widget.openmrs.PatientSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

	var patientSelection;
	var encounterSearch;
	var encounterSelection;
	var conceptSearch;
	var conceptSelection;
	var codedSearch;
	var codedSelection;
	
	dojo.addOnLoad( function() {
		var patientSelection = dojo.widget.manager.getWidgetById("patientSelection");
		var encounterSelection = dojo.widget.manager.getWidgetById("encounterSelection");
		var encounterSearch = dojo.widget.manager.getWidgetById("eSearch");
		var conceptSelection = dojo.widget.manager.getWidgetById("conceptSelection");
		var conceptSearch = dojo.widget.manager.getWidgetById("cSearch");
		var codedSelection = dojo.widget.manager.getWidgetById("codedSelection");
		var codedSearch = dojo.widget.manager.getWidgetById("codedSearch");
		
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				var patient = msg.objs[0];
				patientSelection.hiddenInputNode.value = patient.patientId;
				patientSelection.displayNode.innerHTML = "";
				if (patient.givenName) patientSelection.displayNode.innerHTML += patient.givenName;
				if (patient.middleName) patientSelection.displayNode.innerHTML += " " + patient.middleName;
				if (patient.familyName) patientSelection.displayNode.innerHTML += " " + patient.familyName;
			}
		);
		
		dojo.event.topic.subscribe("eSearch/select", 
			function(msg) {
				encounterSelection.hiddenInputNode.value = msg.objs[0].encounterId;
				encounterSelection.displayNode.innerHTML = msg.objs[0].location + " - " + encounterSearch.getDateString(msg.objs[0].encounterDateTime);
			}
		);
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				conceptSelection.hiddenInputNode.value = msg.objs[0].conceptId;
				conceptSelection.displayNode.innerHTML = msg.objs[0].name;
				conceptSelection.descriptionDisplayNode.innerHTML = msg.objs[0].description;
				updateObsValues(msg.objs[0]);
			}
		);
		
		dojo.event.connect(codedSelection, "onChangeButtonClick", 
			function() {
				var conceptId = conceptSelection.hiddenInputNode.value;
				DWRConceptService.findConceptAnswers(codedSearch.simpleClosure(codedSearch, 'doObjectsFound'), '', conceptId, false, true);
			}
		);
		
		dojo.event.topic.subscribe("codedSearch/select", 
			function(msg) {
				var obj = msg.objs[0];
				if (obj.drugId) {
					codedSelection.displayNode.innerHTML = obj.fullName;
					codedSelection.descriptionDisplayNode.innerHTML = "";
					codedSelection.hiddenInputNode.value = obj.conceptId;
					$('valueDrugId').value = obj.drugId;
				}
				else if (obj.conceptId) {
					codedSelection.displayNode.innerHTML = obj.name;
					codedSelection.descriptionDisplayNode.innerHTML = obj.description;
					codedSelection.hiddenInputNode.value = obj.conceptId;
				}
				
			}
		);

		codedSearch.doFindObjects = function(txt) {
			var conceptId = conceptSelection.hiddenInputNode.value;
			DWRConceptService.findConceptAnswers(codedSearch.simpleClosure(codedSearch, 'doObjectsFound'), txt, conceptId, false, true);
		}
		
		dojo.event.topic.subscribe("codedSearch/objectsFound", 
			function(msg) {
				msg.objs.push('<a href="#proposeConcept" onclick="javascript:return showProposeConceptForm();"><spring:message code="ConceptProposal.propose.new"/></a>');
			}
		);
		
		<c:if test="${obs.concept.conceptId == null}">
			updateObsValues();
		</c:if>
		
	});
	
	function showProposeConceptForm() {
		var qs = "?";
		var encounterId = "${obs.encounter.encounterId}" || $("encounterId").value;
		if (encounterId != "")
			qs += "&encounterId=" + encounterId;
		var obsConceptId = "${obs.concept.conceptId}" || $("conceptId").value
		if (obsConceptId != "")
			qs += "&obsConceptId=" + obsConceptId;
		document.location = "${pageContext.request.contextPath}/admin/concepts/proposeConcept.form" + qs;
	}
	
	function updateObsValues(tmpConcept) {
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
	
	function fillNumericUnits(units) {
		$('numericUnits').innerHTML = units;
	}
	
	function validateNumericRange(value) {
		if (!isNaN(value)) {
			var conceptId = conceptSelection.hiddenInputNode.value;
			var numericErrorMessage = function(validValue) {
				var errorTag = $('numericRangeError');
				errorTag.className = "error";
				if (validValue == false)
					errorTag.innerHTML = '<spring:message code="ConceptNumeric.invalid.msg"/>';
				else
					errorTag.innerHTML = errorTag.className = "";
			}
			DWRConceptService.isValidNumericValue(numericErrorMessage, value, conceptId);
		}
	}
	
	function removeHiddenRows() {
		var rows = document.getElementsByTagName("TR");
		var i = 0;
		while (i < rows.length) {
			if (rows[i].style.display == "none")
				rows[i].parentNode.removeChild(rows[i]);
			else
				i = i + 1;
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
	
</script>

<style>
	th {
		text-align: left;
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
	#encounterSelection .popupSearchForm {
		width: 700px;
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
				<c:choose>
					<c:when test="${obs.obsId != null}">
						<div id="patientName">${status.value.patientName.givenName} ${status.value.patientName.middleName} ${status.value.patientName.familyName}</div>
					</c:when>
					<c:otherwise>
						<div dojoType="PatientSearch" widgetId="pSearch" patientId="${status.value.patientId}" showVerboseListing="true"></div>
						<div dojoType="OpenmrsPopup" widgetId="patientSelection" hiddenInputName="patientId" searchWidget="pSearch" searchTitle='<spring:message code="Patient.findBy" />'></div>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</c:otherwise>
				</c:choose>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.encounter"/></th>
		<td>
			<spring:bind path="obs.encounter">
				<c:choose>
					<c:when test="${obs.encounter == null}">
						<div dojoType="EncounterSearch" widgetId="eSearch"></div>
						<div dojoType="OpenmrsPopup" widgetId="encounterSelection" hiddenInputName="encounterId" hiddenInputId="encounterId" searchWidget="eSearch" searchTitle='<spring:message code="Encounter.find" />'></div>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</c:when>
					<c:otherwise>
						${status.value.location.name} - <openmrs:formatDate date="${status.value.encounterDatetime}" type="medium" />
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
		<th><spring:message code="Obs.concept"/></th>
		<td>
			<spring:bind path="obs.concept">
				<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="${status.value.conceptId}" showVerboseListing="true" ignoreClasses="N/A"></div>
				<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="conceptId" hiddenInputId="conceptId" searchWidget="cSearch" searchTitle='<spring:message code="Concept.find" />' <c:if test="${obs.obsId != null}">showChangeButton="false"</c:if> ></div>
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
				<div dojoType="ConceptSearch" widgetId="codedSearch" conceptId="${status.value.conceptId}" drugId="${obs.valueDrug.drugId}" showVerboseListing="true" includeDrugConcepts="true"></div>
				<div dojoType="OpenmrsPopup" widgetId="codedSelection" hiddenInputName="valueCodedId" searchWidget="codedSearch" searchTitle='<spring:message code="Concept.find" />'></div>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
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
	
	<c:if test="${1 == 2}">
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
	</c:if>
	
	<tr>
		<th><spring:message code="Obs.comment"/></th>
		<spring:bind path="obs.comment">
			<td>
				<textarea name="${status.expression}" rows="2" cols="45">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<c:if test="${obs.creator != null}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${obs.creator.firstName} ${obs.creator.lastName} -
				<openmrs:formatDate date="${obs.dateCreated}" type="medium" />
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
				<openmrs:formatDate date="${obs.dateVoided}" type="medium" />
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

<input type="submit" value='<spring:message code="Obs.save"/>' >
&nbsp; 
<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1);">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>