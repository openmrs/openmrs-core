<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
showIfSet=true/false (defaults to true)
	Whether or not to show important ids that are set
showIfMissing=true/false (defaults to true)
	Whether or not to show (and allow entry of) important ids that are missing
highlightMissing=true/false (defaults to true)
	Whether or not to highlight missing important ids
--%>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<c:set var="showIfSet" value="${empty model.showIfSet || model.showIfSet == 'true'}"/>
<c:set var="showIfMissing" value="${empty model.showIfMissing || model.showIfMissing == 'true'}"/>
<c:set var="highlightMissing" value="${empty model.highlightMissing || model.highlightMissing == 'true'}"/>

<openmrs:globalProperty var="identifierTypes" key="patient_identifier.importantTypes" />

<script type="text/javascript">
	var currIndex = 0;

	function handleSaveIdentifier(index) {
		currIndex = index;
		var identifierType = dwr.util.getValue('identifierType_' + index);
		var identifier = dwr.util.getValue('identifier_' + index);
		//alert("id is " + identifier + " and type is " + identifierType);
		var identifierLocationId = dwr.util.getValue('identifierLocationId_' + index);
		oldId = identifier;
		oldLocation = identifierLocationId;
		if (identifierType != null && identifierType != '' && identifier != null && identifier != '')
			DWRPatientService.addIdentifier(${model.patientId}, identifierType, identifier, identifierLocationId, finishSave);
	}
	
	function finishSave(data) {
		//alert("getting here with data: " + data);
		if ( data ) {
			if ( data.length > 0 ) {
				displayIdError(currIndex, data);
				document.getElementById("identifier_" + currIndex).select();
				document.getElementById("identifier_" + currIndex).focus();
			} else {
				refreshPage();
			}
		} else {
			refreshPage();
		}
	}
	
	function displayIdError(index, msg) {
		dwr.util.setValue("msg_" + index, getMessage(msg));
		if ( msg.length > 0 ) {
			document.getElementById("msg_" + index).style.display = "";
		} else {
			document.getElementById("msg_" + index).style.display = "none";
		}		
	}
	
	function getMessage(msg) {
		var ret = "";
	
		if ( msg == "PatientIdentifier.error.formatInvalid" ) ret = "<spring:message code="PatientIdentifier.error.formatInvalid" />";
		if ( msg == "PatientIdentifier.error.checkDigit" ) ret = "<spring:message code="PatientIdentifier.error.checkDigit" />";
		if ( msg == "PatientIdentifier.error.notUnique" ) ret = "<spring:message code="PatientIdentifier.error.notUnique" />";
		if ( msg == "PatientIdentifier.error.duplicate" ) ret = "<spring:message code="PatientIdentifier.error.duplicate" />";
		if ( msg == "PatientIdentifier.error.insufficientIdentifiers" ) ret = "<spring:message code="PatientIdentifier.error.insufficientIdentifiers" />";
		if ( msg == "PatientIdentifier.error.general" ) ret = "<spring:message code="PatientIdentifier.error.general" />";
		
		return ret;
	}
	
	function identifierFieldChanged(index) {
		var id = dwr.util.getValue('identifier_' + index);
		if (id == null || id == '') {
			document.getElementById('idSaveButton_' + index).disabled = true;
		} else {
			document.getElementById('idSaveButton_' + index).disabled = false;
		}
	}
</script>

<table cellspacing="0" cellpadding="2">
	<c:forTokens var="idHighlightAndLocation" items="${identifierTypes}" delims="," varStatus="iter">
		<c:set var="temp" value="${fn:split(idHighlightAndLocation, ':')}" />
		<c:set var="idTypeName" value="${temp[0]}"/>
		<c:set var="highlight" value="${temp[1]}"/>
		<c:set var="locationName" value="${temp[2]}"/>
	
		<c:set var="found" value="${null}" />
		
		<!-- TESTING:  -->
		
		<c:forEach var="identifier" items="${model.patient.identifiers}">
			<c:if test="${!identifier.voided && identifier.identifierType.name == idTypeName}">
				<c:set var="found" value="${identifier}"/>
			</c:if>
		</c:forEach>

		<c:if test="${ (found!=null && showIfSet) || (found==null && showIfMissing) }">
			<tr <c:if test="${found==null && highlightMissing && highlight=='true'}"> class="highlighted"</c:if>>
				<td>${idTypeName}:</td>
				<td>
					<input type="hidden" id="identifierType_${iter.index}" value="${idTypeName}" />
					<c:if test="${found!=null}">
						<input type="text" id="identifier_${iter.index}" onKeyUp="identifierFieldChanged(${iter.index})" value="${found.identifier}" />
						<openmrs:fieldGen
							type="org.openmrs.Location"
							formFieldName="identifierLocationId_${iter.index}"
							val="${found.location}"
							allowUserDefault="true"
						/>
					</c:if>
					<c:if test="${found==null}">
						<input type="text" id="identifier_${iter.index}" onKeyUp="identifierFieldChanged(${iter.index})" />
						<openmrs:fieldGen
							type="org.openmrs.Location"
							formFieldName="identifierLocationId_${iter.index}"
							val=""
							allowUserDefault="true"
						/>
					</c:if>
					<input id="idSaveButton_${iter.index}" type="button" value="<spring:message code="general.save" />" disabled="true" onClick="handleSaveIdentifier(${iter.index})"/>
				</td>
				<td>
					<span id="msg_${iter.index}" style="display:none;" class="error"></span>
				</td>
			</tr>
		</c:if>		
	</c:forTokens>
</table>
