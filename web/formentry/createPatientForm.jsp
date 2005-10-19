<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ page import="org.openmrs.Patient" />

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>Create Patient</h2>

<br>
<form method="post" action="createPatient">

	<% Patient patient = new Patient(); %>

	<b class="boxHeader">Patient Identifiers</b>
	<div id="patientIdentifier" class="box">
		<%@ include file="/WEB-INF/include/patientInfo.jsp" %>
	</div>

	<br />

	<b class="boxHeader">Patient Names</b>
	<div id="patientName" class="box">
		<%@ include file="/WEB-INF/include/patientName.jsp" %>
	</div>

	<br />

	<div id="patientAddress" class="box">
		<b class="boxHeader">Patient Addresses</b>
		<table>
			<tr>
				<td>Address</td>
				<td><input type="text" name="address1" id="address1" /></td>
			</tr>
			<tr>
				<td>Address2</td>
				<td><input type="text" name="address2" id="address2" /></td>
			</tr>
			<tr>
				<td>City/Village</td>
				<td><input type="text" name="cityVillage" id="cityVillage" /></td>
				<td>State/Province</td>
				<td><input type="text" name="stateProvince" id="stateProvince" size="10" /></td>
				<td>Country</td>
				<td><input type="text" name="country" id="country" size="15" /></td>
			</tr>
			<tr>
				<td>Latitude</td>
				<td><input type="text" name="latitute" id="latitude" /></td>
				<td>Longitude</td>
				<td><input type="text" name="longitude" id="longitude" /></td>
			</tr>
		</table>
	</div>
	<br />
	<b class="boxHeader">Patient Information</b>
	<div id="patientInformation" class="box">
		<%@ include file="/WEB-INF/include/patientInfo.jsp" %>
	</div>

<input type="submit" value="Save Patient">
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
