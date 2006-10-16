<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRFormEntryService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">
	<%-- TODO: fix this terrible hack --%>
	// returns a yyyy-mm-dd
	function toYmd(date) {
		if (date == null || date == '')
			return '';
		<c:choose>
			<c:when test="${locale == 'fr' || locale == 'en_GB'}">
				// dd/mm/yyyy 01/34/6789
				return date.substring(6,10) + '-' + date.substring(3,5) + '-' + date.substring(0,2);
			</c:when>
			<c:otherwise>
				// mm/dd/yyyy 01/34/6789
				return date.substring(6,10) + '-' + date.substring(0,2) + '-' + date.substring(3,5);
			</c:otherwise>
		</c:choose>
	}
	
	function handleSave() {
		if (!checkValues())
			return;
		
		var fieldIds = new Array();
		var fieldValues = new Array();
		var fieldDatetimes = new Array();
		var tmp;
		<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
			fieldIds.push(${formField.field.fieldId});
			tmp = DWRUtil.getValue('encounterFieldValue_${iter.index}');
			<c:if test="${formField.field.attributeName == 'encounter_datetime'}">
				tmp = toYmd(tmp);
			</c:if>
			fieldValues.push(tmp);
		</c:forEach>
		<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
			fieldIds.push(${formField.field.fieldId});
			fieldValues.push(DWRUtil.getValue('obsFieldValue_${iter.index}'));
			fieldDatetimes.push(toYmd(DWRUtil.getValue('obsFieldDate_${iter.index}')));
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
			/*
			var f = {
				fieldId: fieldIds[i],
				value: fieldValues[i],
				dateTime: fieldDatetimes[i]
			};
			*/
			fields.push(f);
		}
		if (fields.length > 0) {
			/*
			window.alert('about to submit : ');
			for (var i = 0; i < fields.length; ++i)
				window.alert(fields[i]);
			*/
			DWRFormEntryService.enterForm(${formData.patient.patientId}, ${formData.form.formId}, true, fields,
					function() {
						hideLayer('form_${formData.patient.patientId}_${formData.form.formId}');
						showLayer('message_${formData.patient.patientId}_${formData.form.formId}');
					}
				);
		} else {
			window.alert("You entered nothing");
		}
	}
	
	function checkValues() {
		var tmp;
		<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
			<c:if test="${formField.required}">
				tmp = DWRUtil.getValue('encounterFieldValue_${iter.index}');
				if (tmp == null || tmp == '') {
					window.alert('Missing ${formField.field.name}');
					return false;
				}
			</c:if>
		</c:forEach>
		<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
			<c:if test="${formField.required}">
				tmp = DWRUtil.getValue('obsFieldValue_${iter.index}');
				if (tmp == null || tmp == '') {
					window.alert('Missing ${formField.field.name}');
					return false;
				}
			</c:if>
		</c:forEach>
		return true;
	}
</script>

<div id="commandBox" style="border: 1px black dashed">
<pre>
<c:out value="${formData}" escapeXml="true"/>
${formData.encounterFields[0].field.name}=${formData.encounterData[0]}
${formData.encounterFields[1].field.name}=${formData.encounterData[1]}
${formData.encounterFields[2].field.name}=${formData.encounterData[2]}
---
${formData.obsData[0].concept}=${formData.obsData[0].valueNumeric}
${formData.obsData[1].concept}=${formData.obsData[1].valueNumeric}
</pre>
</div>

<div id="message_${formData.patient.patientId}_${formData.form.formId}" style="display: none">
	<span style="color: green; font-weight: bold">Done with ${formData.patient}</span>
</div>

<div id="form_${formData.patient.patientId}_${formData.form.formId}">
	<form method="post">
		<input type="hidden" name="formId" value="${formData.form.formId}"/>
		<table>
			<c:forEach var="formField" items="${formData.encounterFields}" varStatus="iter">
				<tr>
					<th>${formField.field.name}</th>
					<spring:bind path="formData.encounterData[${iter.index}]">
						<td>
							<openmrs:fieldGen
								type="${formData.encounterFieldClasses[iter.index]}"
								formFieldName="encounterFieldValue_${iter.index}"
								val="${status.editor.value}"
							/>
							<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</spring:bind>
				</tr>
			</c:forEach>
			<c:forEach var="formField" items="${formData.obsFields}" varStatus="iter">
				<tr>
					<th>${formField.field.name}</th>
					<spring:bind path="formData.obsData[${iter.index}].valueNumeric">
						<td>
							<openmrs:fieldGen
								type="${formData.obsFieldClasses[iter.index]}"
								formFieldName="obsFieldValue_${iter.index}"
								val="${status.editor.value}"
							/>
							${formData.obsData[iter.index].concept.units}
							<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</spring:bind>
					<spring:bind path="formData.obsData[${iter.index}].obsDatetime">
						<td>
							<openmrs:fieldGen
								type="java.util.Date"
								formFieldName="obsFieldDate_${iter.index}"
								val="${status.editor.value}"
							/>
							<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</spring:bind>
				</tr>
			</c:forEach>
		</table>
		
		<input type="button" value="Save with DWR" onClick="handleSave()"/>
		
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>