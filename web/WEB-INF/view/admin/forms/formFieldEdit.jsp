<table>
	<tr>
		<td><spring:message code="FormField.field"/></td>
		<td>
			<select name="field" id="field">
			</select>
		</td>
	</tr>
	<tr>
		<td colspan="2"><hr></td>
	</tr>
	<tr>
		<td><spring:message code="general.id"/></td>
		<td><input type="hidden" name="formFieldId" id="formFieldId"/></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.parent"/></td>
		<td>
			<select name="parent" id="parent">
			</select>
		</td>
	</tr>
	<tr>
		<td><spring:message code="FormField.fieldNumber"/></td>
		<td><input type="text" name="fieldNumber" size="5" id="fieldNumber"/></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.fieldPart"/></td>
		<td><input type="text" name="fieldPart" size="5" id="fieldPart" /></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.pageNumber"/></td>
		<td><input type="text" name="pageNumber" size="5" id="pageNumber" /></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.minOccurs"/></td>
		<td><input type="text" name="minOccurs" size="5" id="minOccurs" /></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.maxOccurs"/></td>
		<td><input type="text" name="maxOccurs" size="5" id="maxOccurs" /></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.required"/></td>
		<td><input type="checkbox" name="required" /></td>
	</tr>
	<tr>
		<td><spring:message code="general.createdBy" /></td>
		<td id="createdBy"></td>
	</tr>
	<tr>
		<td><spring:message code="general.changedBy" /></td>
		<td id="changedBy"></td>
	</tr>
</table>	