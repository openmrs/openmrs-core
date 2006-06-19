<br />
<table>
	<tr>
		<td><spring:message code="DataExport.columnName"/></td>
		<td><input type="text" name="calculatedName" size="30" /></td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="DataExport.columnValue"/></td>
		<td valign="top"><textarea name="calculatedValue" rows="3" cols="50"></textarea>
			<==
			<select name="calculatedPatient" onclick="updateCalcColumn(this)">
				<option value=""> </option>
				<option value="#foreach($var in $!{fn.getPatientAttr('PatientName', 'familyName', true)})$!{var}|#end">&nbsp; <spring:message code="PatientName.familyName" /></option>
				<option value="#foreach($var in $!{fn.getPatientAttr('PatientAddress', 'address1', true)})$!{var}|#end">&nbsp; <spring:message code="PatientAddress.address1" /></option>
				<option value="#foreach($var in $!{fn.getPatientAttr('PatientIdentifier', 'identifier', true)})$!{var}|#end">&nbsp; <spring:message code="PatientIdentifier.identifier" /></option>
				<option value="#foreach($var in $!{fn.getPatientAttr('PatientIdentifier', 'identifier', true)})$!{fn.isValidCheckDigit($!{var})}|#end">&nbsp; <spring:message code="PatientIdentifier.validCheckDigit" /></option>
			</select>
		</td>
	</tr>
</table>
<br/>