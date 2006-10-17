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
	function handleSaveIdentifier(index) {
		var identifierType = DWRUtil.getValue('identifierType_' + index);
		var identifier = DWRUtil.getValue('identifier_' + index);
		var identifierLocationId = DWRUtil.getValue('identifierLocationId_' + index);
		if (identifierType != null && identifierType != '' && identifier != null && identifier != '')
			DWRPatientService.addIdentifier(${model.patientId}, identifierType, identifier, identifierLocationId, refreshPage());
	}
	function identifierFieldChanged(index) {
		var id = DWRUtil.getValue('identifier_' + index);
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
		<c:set var="location" value="${model.locationsByName[locationName]}"/>
	
		<c:set var="found" value="${null}" />
		<c:forEach var="identifier" items="${model.patient.identifiers}">
			<c:if test="${!identifier.voided && identifier.identifierType.name == idTypeName}">
				<c:set var="found" value="${identifier}"/>
			</c:if>
		</c:forEach>

		<c:if test="${ (found!=null && showIfSet) || (found==null && showIfMissing) }">
			<tr <c:if test="${found==null && highlightMissing && highlight=='true'}"> class="highlighted"</c:if>>
				<td>${idTypeName}:</td>
				<td>
					<c:if test="${found!=null}">
						${found.identifier}
					</c:if>
					<c:if test="${found==null}">
						<input type="hidden" id="identifierType_${iter.index}" value="${idTypeName}" />
						<input type="text" id="identifier_${iter.index}" onKeyUp="identifierFieldChanged(${iter.index})" />
						<openmrs:fieldGen
							type="org.openmrs.Location"
							formFieldName="identifierLocationId_${iter.index}"
							val="${location}"
						/>
						<input id="idSaveButton_${iter.index}" type="button" value="<spring:message code="general.save" />" disabled="true" onClick="handleSaveIdentifier(${iter.index})"/>
					</c:if>
				</td>
			</tr>
		</c:if>		
	</c:forTokens>
</table>