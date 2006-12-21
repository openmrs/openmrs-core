<%@ include file="/WEB-INF/template/include.jsp" %>

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRFormEntryService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<h1><spring:message code="BatchEntry.title"/>: ${formData.form.name}</h1>

<div id="batchEntryStepOne">

<b><i><spring:message code="BatchEntry.enterCommonInstructions"/></i></b>
<table>
	<tr>
	<spring:bind path="formData.encounterDatetime">
		<td><spring:message code="BatchEntry.common.encounterDatetime"/>:</td>
		<td>
			<openmrs:fieldGen
				type="java.util.Date"
				formFieldName="batch_encounter_datetime"
				val="${status.editor.value}"
			/>
		</td>
	</spring:bind>
	
	<td width="10"></td>
	
	<spring:bind path="formData.location">
		<td><spring:message code="BatchEntry.common.location"/>:</td>
		<td>
			<openmrs:fieldGen
				type="org.openmrs.Location"
				formFieldName="batch_location"
				val="${status.editor.value}"
			/>
		</td>
	</spring:bind>
	
	<td width="10"></td>

	<spring:bind path="formData.provider">
		<td><spring:message code="BatchEntry.common.provider"/>:</td>
		<td>
			<openmrs:fieldGen
				type="org.openmrs.User"
				formFieldName="batch_provider"
				val="${status.editor.value}"
			/>
		</td>
	</spring:bind>
	</tr>
</table>
<input type="button" value="<spring:message code="BatchEntry.prepare"/>" onClick="buildBatchEntry()"/>

</div>

<script type="text/javascript">
	<%-- TODO: fix this terrible hack --%>
	// returns a yyyy-mm-dd
	function toYmd(date) {
		if (date == null || date == '')
			return '';
		<c:choose>
			<c:when test="${datePattern == 'dd/MM/yyyy'}">
				// dd/mm/yyyy 01/34/6789
				return date.substring(6,10) + '-' + date.substring(3,5) + '-' + date.substring(0,2);
			</c:when>
			<c:otherwise>
				// mm/dd/yyyy 01/34/6789
				return date.substring(6,10) + '-' + date.substring(0,2) + '-' + date.substring(3,5);
			</c:otherwise>
		</c:choose>
	}

	var encounterFields = new Array();
	var obsFields = new Array();
	var fieldIds = new Array();
	{
	<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
		fieldIds.push(${formField.field.fieldId});
		encounterFields.push('${formField.field.attributeName}');
	</c:forEach>
	<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
		fieldIds.push(${formField.field.fieldId});
		obsFields.push(${formField.field.concept});
	</c:forEach>
	}
	
	var ids = new Array();
	{
	<c:forEach var="pt" items="${formData.patientSet.patients}">
		ids.push(${pt.patientId});
	</c:forEach>
	}
	
	var locationId;
	var datetime;
	var providerId;

	var providerIdField = -1;
	var encounterDatetimeField = -1;
	var locationIdField = -1;

	function buildBatchEntry() {
		locationId = DWRUtil.getValue('batch_location');
		if (locationId == null || locationId == '') {
			window.alert('<spring:message code="BatchEntry.error.missingLocation" javaScriptEscape="true"/>');
			return;
		}
		datetime = DWRUtil.getValue('batch_encounter_datetime');
		if (datetime == null || datetime == '') {
			window.alert('<spring:message code="BatchEntry.error.missingEncounterDatetime" javaScriptEscape="true"/>');
			return;
		}
		providerId = DWRUtil.getValue('batch_provider');
		if (providerId == null || providerId == '') {
			window.alert('<spring:message code="BatchEntry.error.missingProvider" javaScriptEscape="true"/>');
			return;
		}
		
		for (var i = 0; i < encounterFields.length; ++i) {
			if (encounterFields[i].toLowerCase() == 'provider_id')
				providerIdField = i;
			else if (encounterFields[i].toLowerCase() == 'location_id')
				locationIdField = i;
			else if (encounterFields[i].toLowerCase() == 'encounter_datetime')
				encounterDatetimeField = i;
		}
				
		for (var i = 0; i < ids.length; ++i) {
			DWRUtil.setValue('enc_' + ids[i] + '_' + providerIdField, providerId);
			DWRUtil.setValue('enc_' + ids[i] + '_' + locationIdField, locationId);
			DWRUtil.setValue('enc_' + ids[i] + '_' + encounterDatetimeField, datetime);
		}
		showLayer('batchEntryStepTwo');
	}
	
	function checkValues(ptId) {
		var tmp;
		<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
			<c:if test="${formField.required}">
				tmp = DWRUtil.getValue('enc_' + ptId + '_${iter.index}');
				if (tmp == null || tmp == '') {
					window.alert('<spring:message code="BatchEntry.error.missing" javaScriptEscape="true" arguments="${formField.field.name}"/>');
					return false;
				}
			</c:if>
		</c:forEach>
		var anyObs = false;
		<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
			tmp = DWRUtil.getValue(ptId + '_obs_${iter.index}');
			if (tmp != null && tmp != '')
				anyObs = true;
			<c:if test="${formField.required}">
				if (tmp == null || tmp == '') {
					window.alert('<spring:message code="BatchEntry.error.missing" javaScriptEscape="true" arguments="${formField.field.name}"/>');
					return false;
				}
			</c:if>
		</c:forEach>
		if (!anyObs) {
			window.alert("<spring:message code="BatchEntry.error.nothing"/>");
			return false;
		}
		return true;
	}
	
	function handleSave(ptId) {
		if (!checkValues(ptId))
			return;
		
		var fieldValues = new Array();
		var fieldDatetimes = new Array();
		var tmp;
		<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
			tmp = DWRUtil.getValue('enc_' + ptId + '_${iter.index}');
			<c:if test="${formField.field.attributeName == 'encounter_datetime'}">
				tmp = toYmd(tmp);
			</c:if>
			fieldValues.push(tmp);
		</c:forEach>
		<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
			fieldValues.push(DWRUtil.getValue(ptId + '_obs_${iter.index}'));
			fieldDatetimes.push(toYmd(DWRUtil.getValue(ptId + '_obsdate_${iter.index}')));
		</c:forEach>

		var fields = new Array();
		for (var i = 0; i < fieldIds.length; ++i) {
			if (fieldValues[i] == null || fieldValues[i] == '')
				continue;
			var f = fieldIds[i] + "^";
			if (fieldDatetimes[i] != null && fieldDatetimes[i] != '')
				f += fieldDatetimes[i];
			f += "^";
			f += fieldValues[i];
			fields.push(f);
		}
		if (fields.length > 0) {
			DWRFormEntryService.enterForm(ptId, ${formData.form.formId}, true, fields,
					function() {
						hideLayer(ptId + '_unsaved');
						showLayer(ptId + '_saved');
						for (var i = 0; i < obsFields.length; ++i) {
							document.getElementById(ptId + '_obs_' + i).disabled = true;
							document.getElementById(ptId + '_obsdate_' + i).disabled = true;
						}
						for (var i = 0; i < encounterFields.length; ++i) {
							if (i == providerIdField) {
								// TODO: figure out how to disable user selector. The following line is wrong
								document.getElementById('enc_' + ptId + '_' + i + '_search').style.display = 'none';
							} else {
								document.getElementById('enc_' + ptId + '_' + i).disabled = true;
							}
						}
					}
				);
		} else {
			window.alert("<spring:message code="BatchEntry.error.nothing"/>");
		}
	}
	
	function toggleEncounterFields() {
		var arr = document.getElementsByTagName("*");
		for (var i = 0; i < arr.length; ++i) {
			if (arr[i].className == 'encCell') {
				var s = arr[i].style;
				if (s.display == 'none')
					s.display = '';
				else
					s.display = 'none';
			}
		}
	}
</script>

<div id="batchEntryStepTwo" style="display: none">
	<a href="javascript:toggleEncounterFields()"><spring:message code="BatchEntry.togglePerPatient"/></a>
	<p/>
	<table class="thinBorder">
		<tr>
			<th colspan="2"><spring:message code="Encounter.patient"/></th>
			<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
				<th class="encCell"><spring:message code="BatchEntry.encounterField.${formField.field.name}"/></th>
			</c:forEach>
			<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
				<th>${formField.field.name}</th>
			</c:forEach>
		</tr>
		<c:forEach var="patient" items="${formData.patientSet.patients}" varStatus="iter">
			<tr id="patient_row_${patient.patientId}"
				<c:choose>
					<c:when test="${iter.count % 2 == 0}">
						class="oddRow"
					</c:when>
					<c:otherwise>
						class="evenRow"
					</c:otherwise>
				</c:choose>
			>
				<th>
					<span style="font-weight: normal; font-size: .7em; text-decoration: underline">${patient.patientIdentifier.identifierType}</span><br/>${patient.patientIdentifier.identifier}
				</th>
				<th>
					${patient.patientName}
				</th>
				<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
					<td class="encCell">
						<openmrs:fieldGen
							type="${formData.encounterFieldClasses[iter.index]}"
							formFieldName="enc_${patient.patientId}_${iter.index}"
							val=""
						/>
					</td>
				</c:forEach>
				<c:forEach var="param" items="${formData.obsFieldParameters}">
					<!-- TESTING: ${param} -->
				</c:forEach>
				<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
					<td>
						<openmrs:fieldGen
							type="${formData.obsFieldClasses[iter.index]}"
							formFieldName="${patient.patientId}_obs_${iter.index}"
							val=""
							parameters="${formData.obsFieldParameters[iter.index]}"
						/>
						<!-- TESTING: parameters: ${formData.obsFieldParameters[iter.index]}, index: ${iter.index} -->
						<%-- TODO: put units here for numeric concepts --%>
						<br/>
						<spring:message code="general.onDate"/>
						<openmrs:fieldGen
							type="java.util.Date"
							formFieldName="${patient.patientId}_obsdate_${iter.index}"
							val=""
						/>
					</td>
				</c:forEach>
				<td>
					<span id="${patient.patientId}_unsaved">
						<input type="button" value="<spring:message code="general.save"/>" onClick="handleSave(${patient.patientId})"/>
					</span>
					<span id="${patient.patientId}_saved" class="batchEntrySavedMessage" style="display: none;">
						<spring:message code="general.saved"/>
					</span>
				</td>
			</tr>
		</c:forEach>
	</table>
	
	<p/>
	<a href="index.htm"><spring:message code="BatchEntry.finished"/></a>
</div>

<script type="text/javascript">
	toggleEncounterFields();
</script>