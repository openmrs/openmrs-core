<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Observations" otherwise="/login.htm" redirect="/admin/observations/obs.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<script type="text/javascript">

	// on concept select:
	function onQuestionSelect(concept) {
		$j("#conceptDescription").show();
		$j("#conceptDescription").html(concept.description);
		updateObsValues(concept);
	}

	// on answer select:
	function onAnswerSelect(concept) {
		$j("#codedDescription").show();
		$j("#codedDescription").html(concept.description);
	}
	
	function showProposeConceptForm() {
		var qs = "?";
		var encounterId = "${obs.encounter.encounterId}" || $j("#encounterId").val();
		if (encounterId != "")
			qs += "&encounterId=" + encounterId;
		var obsConceptId = "${obs.concept.conceptId}" || $j("#conceptId").val();
		if (obsConceptId != "")
			qs += "&obsConceptId=" + obsConceptId;
		document.location = "${pageContext.request.contextPath}/admin/concepts/proposeConcept.form" + qs;
	}
	
	function updateObsValues(tmpConcept) {
		var values = ['valueBooleanRow', 'valueCodedRow', 'valueDatetimeRow', 'valueModifierRow', 'valueTextRow', 'valueNumericRow', 'valueInvalidRow', 'valueComplex'];
		$j.each(values, function(x, val) { $j("#" + val).hide() });
		
		if (tmpConcept != null) {
			var datatype = tmpConcept.hl7Abbreviation;
			if (typeof datatype != 'string')
				datatype = tmpConcept.datatype.hl7Abbreviation;
			
			if (datatype == 'BIT') {
				$j('#valueBooleanRow').show();
			}
			else if (datatype == 'NM' || datatype == 'SN') {
				$j('#valueNumericRow').show();
				DWRConceptService.getConceptNumericUnits(tmpConcept.conceptId, fillNumericUnits);
			}
			else if (datatype == 'CWE') {
				$j('#valueCodedRow').show();
				
				// clear any old values:
				$j("#valueCoded").val("");
				$j("#valueCoded_selection").val("");
				$j("#codedDescription").html("");
				
				// set up the autocomplete for the answers
				var conceptId = $j("#conceptId").val();
				new AutoComplete("valueCoded_selection", new CreateCallback({showAnswersFor: conceptId}).conceptAnswersCallback(), {'minLength':'0'});
				$j("#valueCoded_selection").autocomplete().focus(function(event, ui) {
					if (event.target.value == "")
						$j("#valueCoded_selection").trigger('keydown.autocomplete');
				}); // trigger the drop down on focus

				$j("#valueCoded_selection").focus();
			}
			else if (datatype == 'ST') {
				$j('#valueTextRow').show();
			}
			else if (datatype == 'DT' || datatype == 'TS' || datatype == 'TM') {
				$j('#valueDatetimeRow').show();
			}
			// TODO move datatype 'TM' to own time box.  How to have them select?
			else if (datatype == 'ED') {
				$j('#valueComplex').show();
			}
			else {
				$j('#valueInvalidRow').show();
				DWRConceptService.getQuestionsForAnswer(tmpConcept.conceptId, fillValueInvalidPossible(tmpConcept));
			}
		}
	}
	
	function fillNumericUnits(units) {
		$j('#numericUnits').html(units);
	}
	
	function validateNumericRange(value) {
		if (!isNaN(value) && value != '') {
			var conceptId = $j("#conceptId").val();
			var numericErrorMessage = function(validValue) {
				var errorTag = document.getElementById('numericRangeError');
				errorTag.className = "error";
				if (validValue == false)
					errorTag.innerHTML = '<spring:message code="ConceptNumeric.invalid.msg"/>';
				else
					errorTag.innerHTML = errorTag.className = "";
			}
			DWRConceptService.isValidNumericValue(value, conceptId, numericErrorMessage);
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
	
	var fillValueInvalidPossible = function(invalidConcept) {
		return function(questions) {
			var div = document.getElementById('valueInvalidPossibleConcepts');
			div.innerHTML = "";
			var txt = document.createTextNode('<spring:message code="Obs.valueInvalid.didYouMean"/> ');
			for (var i=0; i<questions.length && i < 10; i++) {
				if (i == 0)
					div.appendChild(txt);
				var concept = questions[i];
				var link = document.createElement("a");
				link.href = "#selectAsQuestion";
				link.onclick = selectNewQuestion(concept, invalidConcept);
				link.title = concept.description;
				link.innerHTML = concept.name;
				if (i == (questions.length - 1) || i == 9)
					link.innerHTML += "?";
				else
					link.innerHTML += ", ";
				div.appendChild(link);
			}
		}
	}
	
	var selectNewQuestion = function (question, answer) {
		return function() {
				var msg = new Object();
				msg.objs = [question];
				dojo.event.topic.publish(conceptSearch.eventNames.select, msg);
				msg.objs = [answer];
				dojo.event.topic.publish(codedSearch.eventNames.select, msg);
				return false;
		};
	}
	
</script>

<style>
	th {
		text-align: left;
	}
	*>#numericRangeError, *>.obsValue {
		visibility: visible;
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
	.obsValue {
		display: none;
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

<c:if test="${obs.voided}">
	<form action="" method="post">
		<div class="retiredMessage">
			<div>
				<spring:message code="general.voidedBy"/>
				${obs.voidedBy.personName}
				<openmrs:formatDate date="${obs.dateVoided}" type="medium" />
				-
				${obs.voidReason}
				<input type="submit" value='<spring:message code="Obs.unvoidObs"/>' name="unvoidObs"/>
			</div>
		</div>
	</form>
</c:if>

<form method="post" onSubmit="removeHiddenRows()" enctype="multipart/form-data">

<fieldset>

<spring:nestedPath path="obs">

<table id="obsTable">
	<c:if test="${obs.obsId != null}">
		<tr>
			<th><spring:message code="general.id"/></th>
			<td>
				<spring:bind path="obsId">
					${status.value}
				</spring:bind>
			</td>
		</tr>
	</c:if>
	<tr>
		<th><spring:message code="Obs.person"/></th>
		<td>
			<spring:bind path="person">
				<openmrs_tag:personField formFieldName="person" searchLabelCode="Person.findBy" initialValue="${status.editor.value.personId}" linkUrl="" callback="" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.encounter"/></th>
		<td>
			<spring:bind path="encounter">
				<c:choose>
					<c:when test="${obs.encounter == null}">
						<openmrs_tag:encounterField formFieldName="encounter" formFieldId="encounterId" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</c:when>
					<c:otherwise>
						${status.editor.value.location.name} - <openmrs:formatDate date="${status.editor.value.encounterDatetime}" type="medium" />
						<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${status.editor.value.encounterId}"><spring:message code="general.view"/>/<spring:message code="general.edit"/></a>
					</c:otherwise>
				</c:choose>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.order"/></th>
		<td>
			<spring:bind path="order">
				<input type="text" name="order" id="order" value="${status.editor.value.orderId}" size="7" <c:if test="${obs.obsId != null}">disabled</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.location"/></th>
		<td>
			<spring:bind path="location">
				<openmrs_tag:locationField formFieldName="location" initialValue="${status.value}"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.datetime"/></th>
		<td>
			<spring:bind path="obsDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" id="${status.expression}" />
				(<spring:message code="general.format"/>: <openmrs:datePattern />)
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Obs.concept"/></th>
		<td>
			<spring:bind path="obs.concept">
				<openmrs_tag:conceptField formFieldName="concept" formFieldId="conceptId" excludeDatatypes="N/A" initialValue="${status.editor.value.conceptId}" onSelectFunction="onQuestionSelect" />
				<div class="description" id="conceptDescription"></div>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${1 == 2}">
		<tr>
			<th><spring:message code="Obs.accessionNumber"/></th>
			<td>
				<spring:bind path="accessionNumber">
					<input type="text" name="${status.expression}" id="accessionNumber" value="${status.value}" size="10" <c:if test="${obs.obsId != null}">disabled</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Obs.valueGroupId"/></th>
			<spring:bind path="valueGroupId">
				<td>
					<input type="text" name="${status.expression}" id="valueGroupId" value="${status.value}" size="10" <c:if test="${obs.obsId != null}">disabled</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</td>
			</spring:bind>
		</tr>
	</c:if>
	<tr id="valueBooleanRow" class="obsValue">
		<th><spring:message code="Obs.booleanAnswer"/></th>
		<spring:bind path="valueBoolean">
			<td>
				<select name="${status.expression}" id="valueBooleanSelect">
					<option value="" <c:if test="${status.value == null || status.value == ''}">selected</c:if>></option>
					<option value="true" <c:if test="${status.value == 'true'}">selected</c:if>><spring:message code="general.true"/></option>
					<option value="false" <c:if test="${status.value == 'false'}">selected</c:if>><spring:message code="general.false"/></option>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueCodedRow" class="obsValue">
		<%-- TODO: add switch on drug search here. <drugId="${obs.valueDrug.drugId}" showVerboseListing="true" includeDrugConcepts="true"></div> --%>
		<th valign="top"><spring:message code="Obs.codedAnswer"/></th>
		<td>
			<spring:bind path="valueCoded">
				<openmrs_tag:conceptField formFieldName="valueCoded" formFieldId="valueCoded" initialValue="${status.editor.value.conceptId}" showAnswers="${obs.concept.conceptId}" onSelectFunction="onAnswerSelect"/>
				<div class="description" id="codedDescription"></div>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<spring:bind path="valueDrug">
				<input type="hidden" id="valueDrugId" value="${status.editor.value.drugId}" name="valueDrug" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="valueDatetimeRow" class="obsValue">
		<th><spring:message code="Obs.datetimeAnswer"/></th>
		<td>
			<spring:bind path="valueDatetime">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
				  (<spring:message code="general.format"/>: <openmrs:datePattern />)
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr id="valueNumericRow" class="obsValue">
		<th><spring:message code="Obs.numericAnswer"/></th>
		<spring:bind path="valueNumeric">
			<td>
				<input type="text" name="${status.expression}" value="${status.value}" size="10" onKeyUp="validateNumericRange(this.value)"/>
				<span id="numericUnits"></span>
				<span id="numericRangeError"></span>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueModifierRow" class="obsValue">
		<th><spring:message code="Obs.valueModifier"/></th>
		<spring:bind path="valueModifier">
			<td>
				<input type="text" name="${status.expression}" id="valueModifierInput" value="${status.value}" size="3" maxlength="2"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueTextRow" class="obsValue">
		<th><spring:message code="Obs.textAnswer"/></th>
		<spring:bind path="valueText">
			<td>
				<textarea name="${status.expression}" rows="9" cols="80">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueComplex" class="obsValue">
		<th><spring:message code="Obs.complexAnswer"/></th>
		<spring:bind path="valueComplex">
			<td>
				${status.value}<br/>
				<a href="${hyperlinkView}" target="_blank"><spring:message code="Obs.viewCurrentComplexValue"/></a><br/>
				${htmlView}<br/><br/>
				<spring:message code="Obs.valueComplex.uploadNew"/>
				<input type="file" name="complexDataFile" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</td>
		</spring:bind>
	</tr>
	<tr id="valueInvalidRow" class="obsValue">
		<th> &nbsp; </th>
		<td>
			<div class="error"><spring:message code="Obs.valueInvalid.description"/></div>
			<div id="valueInvalidPossibleConcepts"></div>
		</td>
	</tr>
	
	<openmrs:extensionPoint pointId="org.openmrs.admin.observations.belowValueRow" type="html" parameters="obsId=${obs.obsId}"></openmrs:extensionPoint>
	
	<%--
		<tr>
			<th><spring:message code="Obs.dateStarted"/></th>
			<td>
				<spring:bind path="dateStarted">			
					<input type="text" name="${status.expression}" size="10" 
						   value="${status.value}" onClick="showCalendar(this)" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="Obs.dateStopped"/></th>
			<td>
				<spring:bind path="dateStopped">			
					<input type="text" name="${status.expression}" size="10" 
						   value="${status.value}" onClick="showCalendar(this)" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
				</spring:bind>
			</td>
		</tr>
	--%>
	
	<tr>
		<th><spring:message code="Obs.comment"/></th>
		<spring:bind path="comment">
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
				${obs.creator.personName} -
				<openmrs:formatDate date="${obs.dateCreated}" type="medium" />
			</td>
		</tr>
	</c:if>
</table>
</spring:nestedPath>
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

<%-- You can't edit a voided obs --%>
<input type="submit" name="saveObs" value='<spring:message code="Obs.save"/>' <c:if test="${obs.voided}">disabled</c:if> >

&nbsp; 
<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1);">

</fieldset>
</form>

<br/>
<br/>

<c:if test="${not obs.voided && not empty obs.obsId}">
	<form action="" method="post">
		<fieldset>
			<h4><spring:message code="Obs.voidObs"/></h4>
			
			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="voidReason" />
			<spring:hasBindErrors name="obs">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'voidReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="Obs.voidObs"/>' name="voidObs"/>
		</fieldset>
	</form>
</c:if>

<c:if test="${obs.obsId != null}">
<br/>
<openmrs:extensionPoint pointId="org.openmrs.admin.observations.obsFormBottom" type="html" parameters="obsId=${obs.obsId}">
	<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<div class="boxHeader" style="font-weight: bold;"><spring:message code="${extension.title}" /></div>
		<div class="box" style="padding: 0px 0px 5px;"><spring:message code="${extension.content}" />
  			<c:if test="${extension.portletUrl != null}">
   				<openmrs:portlet url="${extension.portletUrl}" moduleId="${extension.moduleId}" id="${extension.portletUrl}" parameters="allowEdits=true|obsId=${obs.obsId}"/>
 			</c:if>
		</div>
		<br />
	</openmrs:hasPrivilege>
</openmrs:extensionPoint>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>