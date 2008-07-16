<br />
<table>
	<tr>
		<td colspan="2">
			<spring:message code="PatientSearch.title"/>:
			<select name="patientSearchIdValue" onChange="updateCohortColumn(this)">
				<option value=""></option>
				<openmrs:forEachRecord name="reportObject" reportObjectType="Patient Search">
					<option value="${record.reportObjectId}">${record.name}</option>
				</openmrs:forEachRecord>
			</select>			
			<i><spring:message code="general.or"/></i>
			<spring:message code="Cohort.title.endUser"/>:
			<select name="cohortIdValue" onChange="updateCohortColumn(this)">
				<option value=""></option>
				<openmrs:forEachRecord name="cohort">
					<option value="${record.cohortId}">${record.name}</option>
				</openmrs:forEachRecord>
			</select>
			<i><spring:message code="general.or"/></i>
			<spring:message code="PatientFilter.title.endUser"/>:
			<select name="filterIdValue" onChange="updateCohortColumn(this)">
				<option value=""></option>
				<openmrs:forEachRecord name="reportObject" reportObjectType="Patient Filter">
					<option value="${record.reportObjectId}">${record.name}</option>
				</openmrs:forEachRecord>
			</select>			
		</td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.columnName"/></td>
		<td><input type="text" name="cohortName" size="30" /></td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.cohort.valueIfTrue"/></td>
		<td><input type="text" name="cohortIfTrue"/></td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.cohort.valueIfFalse"/></td>
		<td><input type="text" name="cohortIfFalse"/></td>
	</tr>
</table>
<br/>
