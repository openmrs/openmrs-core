<%@ include file="/WEB-INF/template/include.jsp" %>

	<openmrs:portlet url="patientHeader" id="patientSummaryHeader" patientId="${patient.patientId}"/>
	
	<div class="boxHeader"><spring:message code="Patient.info.regimen"/></div>
	<div class="box">
		<table width="100%">
			<tr>
				<td width="50%">
					Current regimen and allergies will go here...
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
				</td>
				<td width="50%">
					Regimen history will go here...
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
				</td>
			</tr>
		</table>
	</div>
	
	<div class="boxHeader"><spring:message code="Patient.info.vitalsAndLabs"/></div>
	<div class="box">
		<table width="100%">
			<tr>
				<td width="50%">
					Current regimen and allergies will go here...
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
				</td>
				<td width="50%">
					Regimen history will go here...
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
					<br />
				</td>
			</tr>
		</table>
	</div>
	
	<div class="boxHeader"><spring:message code="Patient.info.alerts"/></div>
	<div class="box">
		Alerts will go here...
		<br />
		<br />
		<br />
		<br />
		<br />
		<br />
		<br />
		<br />
	</div>

