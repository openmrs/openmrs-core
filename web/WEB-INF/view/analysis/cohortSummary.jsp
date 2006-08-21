<%@ include file="/WEB-INF/template/include.jsp" %>

<h3 align="center">Cohort Summary <i>(${model.patientSet.size} patients)</i></h3>

<table width="100%"><tr valign="top"><td align="center">

	<div class="boxHeader"><spring:message code="Cohort.ageAndGender"/></div>
	<div class="box">
		<img src="/openmrs/pieChartServlet?width=500&height=300&chartTitle=Age&nbsp;and&nbsp;Gender"/>
	</div>
	
	<div class="boxHeader"><spring:message code="Cohort.hivEnrollments"/></div>
	<div class="box">
		<img src="/openmrs/timelineGraphServlet?width=1200&height=300&chartTitle=HIV&nbsp;Program&nbsp;Enrollments&rangeAxisTitle=Enrollments&domainAxisTitle=Month"/>
	</div>

</td></tr></table>

<input type="hidden" id="hiddenPatientIds" value="${model.patientSet.commaSeparatedPatientIds}" />
<script type="text/javascript">
	patientIds = '${model.patientSet.commaSeparatedPatientIds}';
</script>