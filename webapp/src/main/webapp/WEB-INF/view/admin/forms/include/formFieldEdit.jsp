
<iframe id="fieldWarningIframe" src="javascript:false;" frameBorder="0" scrolling="no"></iframe>
<div id="fieldWarning">
	<openmrs:message code="Field.editWarning"/><br/>
	<a target="newField" href="field.form?fieldId=" onclick="return editAllFields();"><openmrs:message code="Field.editWarning.allForms"/></a> | 
	<a target="newField" href="#" onclick="return editFieldForThisForm();"><openmrs:message code="Field.editWarning.thisForm"/></a>
</div>

<table id="field" width="100%">
	<input type="hidden" id="fieldId" value=""/>
	<tr>
		<td valign="top">
			<openmrs:message code="Field.name"/>
		</td>
		<td>
			<input type="text" id="fieldName" size="30" />
			<openmrs:message code="Field.forms"/> <span id="numForms"></span>
		</td>
	</tr>
	<tr>
		<td valign="top"><openmrs:message code="general.description"/></td>
		<td><textarea id="description" rows="1" cols="35"></textarea></td>
	</tr>
	<tr>
		<td><openmrs:message code="Field.type"/></td>
		<td>
			<select id="fieldType" onChange="chooseFieldType(this.value)" onKeyUp="chooseFieldType(this.value)">
				<c:forEach items="${fieldTypes}" var="ft">
					<option value="${ft.fieldTypeId}"><c:out value="${ft.name}"/></option>
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr id="concept">
		<td valign="top"><openmrs:message code="Field.concept"/></td>
		<td valign="top">
			<div dojoType="ConceptSearch" widgetId="cSearch" showVerboseListing="true" ignoreClasses="N/A"></div>
			<div dojoType="OpenmrsPopup" widgetId="cSelection" hiddenInputId="conceptId" searchWidget="cSearch" searchTitle='<openmrs:message code="Concept.find" />' setPositionTop="false"></div>
		</td>
	</tr>
	<tr id="database">
		<td valign="top"><openmrs:message code="Field.database"/></td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<openmrs:message code="Field.tableName"/> <br/>
						<input type="input" id="tableName" size="15"/>
					</td>
					<td>
						<openmrs:message code="Field.attributeName"/> <br/>
						<input type="input" id="attributeName" size="15"/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<openmrs:message code="Field.defaultValue" />
		</td>
		<td>
			<input type="text" id="defaultValue" size="30" />
		</td>
	</tr>
	<tr>
		<td style="white-space: nowrap;"><label for="selectMultiple"><openmrs:message code="Field.selectMultiple"/></label></td>
		<td><input type="checkbox" id="selectMultiple" value="on"/></td>
	</tr>
</table>

<input type="hidden" id="formFieldId" value=""/>
<table>
	<tr>
		<td align="center"><openmrs:message code="FormField.fieldNumber"/></td>
		<td align="center"><openmrs:message code="FormField.fieldPart"/></td>
		<td align="center"><openmrs:message code="FormField.pageNumber"/></td>
		<td align="center"><openmrs:message code="FormField.minOccurs"/></td>
		<td align="center"><openmrs:message code="FormField.maxOccurs"/> <span class="smallMessage">(<openmrs:message code="FormField.maxOccurs.help"/>)</span> </td>
	</tr>
	<tr>
		<td align="center"><input type="text" size="5" id="fieldNumber"/></td>
		<td align="center"><input type="text" size="5" id="fieldPart" /></td>
		<td align="center"><input type="text" size="5" id="pageNumber" /></td>
		<td align="center"><input type="text" size="5" id="minOccurs" /></td>
		<td align="left"><input type="text" size="5" id="maxOccurs" /></td>
	</tr>
	<tr>
		<td><label for="required"><openmrs:message code="FormField.required"/></label></td>
		<td><input type="checkbox" id="required" /></td>
	</tr>
</table>