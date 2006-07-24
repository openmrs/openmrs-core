<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript">
	var timeOut = null;

	function startDownloading() {
		timeOut = setTimeout("goBack()", 30000);
	}
	
	function goBack() {
		document.location='index.htm';
	}
	
	function switchPatient() {
		document.location='index.htm?phrase=john&autoJump=false';
	}
	
	function cancelTimeout() {
		if (timeOut != null)
			clearTimeout(timeOut);
	}	
</script>

<table class="breadcrumbHeader">
	<tr>
		<th>
			<spring:message code="Patient.dashboard.title"/>
		</th>
		<td>
			<a href="#switch" onClick="switchPatient()">Select Another Patient</a>
		</td>
	</tr>
</table>
<openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${patient.patientId}"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>
