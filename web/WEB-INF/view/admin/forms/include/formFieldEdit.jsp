
<div id="fieldWarning">
		<spring:message code="Field.editWarning"/><br/>
		<a target="newField" href="field.form?fieldId=" onclick="return editAllFields();"><spring:message code="Field.editWarning.allForms"/></a> | 
		<a target="newField" href="#" onclick="return editFieldForThisForm();"><spring:message code="Field.editWarning.thisForm"/></a>
</div>

<table id="field" width="100%">
	<input type="hidden" id="fieldId" value=""/>
	<tr>
		<td valign="top">
			<spring:message code="Field.name"/>
		</td>
		<td>
			<input type="text" id="fieldName" size="30" />
			<spring:message code="Field.forms"/> <span id="numForms"></span>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td><textarea id="description" rows="1" cols="35"></textarea></td>
	</tr>
	<tr>
		<td><spring:message code="Field.type"/></td>
		<td>
			<select id="fieldType" onChange="chooseFieldType(this.value)">
				<c:forEach items="${fieldTypes}" var="ft">
					<option value="${ft.fieldTypeId}">${ft.name}</option>
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr id="concept">
		<td valign="top"><spring:message code="Field.concept"/></td>
		<td valign="top">
			<input type="hidden" id="conceptId" />
			<span id="conceptName"></span>
			<input type="button" id="conceptButton" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearchForm(this)" />
		</td>
	</tr>
	<tr id="database">
		<td valign="top"><spring:message code="Field.database"/></td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<spring:message code="Field.tableName"/> <br/>
						<input type="input" id="tableName" size="15"/>
					</td>
					<td>
						<spring:message code="Field.attributeName"/> <br/>
						<input type="input" id="attributeName" size="15"/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<spring:message code="Field.defaultValue" />
		</td>
		<td>
			<input type="text" id="defaultValue" size="30" />
		</td>
	</tr>
	<tr>
		<td style="white-space: nowrap;"><label for="selectMultiple"><spring:message code="Field.selectMultiple"/></label></td>
		<td><input type="checkbox" id="selectMultiple" value="on"/></td>
	</tr>
</table>

<table>
	<input type="hidden" id="formFieldId" value=""/>
	<tr>
		<td align="center"><spring:message code="FormField.fieldNumber"/></td>
		<td align="center"><spring:message code="FormField.fieldPart"/></td>
		<td align="center"><spring:message code="FormField.pageNumber"/></td>
		<td align="center"><spring:message code="FormField.minOccurs"/></td>
		<td align="center"><spring:message code="FormField.maxOccurs"/></td>
	</tr>
	<tr>
		<td align="center"><input type="text" size="5" id="fieldNumber"/></td>
		<td align="center"><input type="text" size="5" id="fieldPart" /></td>
		<td align="center"><input type="text" size="5" id="pageNumber" /></td>
		<td align="center"><input type="text" size="5" id="minOccurs" /></td>
		<td align="center"><input type="text" size="5" id="maxOccurs" /></td>
	</tr>
	<tr>
		<td><label for="required"><spring:message code="FormField.required"/></label></td>
		<td><input type="checkbox" id="required" /></td>
	</tr>
</table>