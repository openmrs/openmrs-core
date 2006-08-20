<%@ include file="/WEB-INF/template/include.jsp" %>

<h3 align="center">Cohort Summary <i>(${model.patientSet.size} patients)</i></h3>

<table width="100%"><tr valign="top"><td align="center">

	<div class="boxHeader"><spring:message code="Cohort.ageAndGender"/></div>
	<div class="box">
		${model.ageGenderTable.htmlTable}
	</div>

</td><td align="center">

	<div class="boxHeader"><spring:message code="Cohort.hivEnrollments"/></div>
	<div class="box">
		${model.hivEnrollmentTable.htmlTable}
	</div>

</td></tr></table>

<input type="hidden" id="hiddenPatientIds" value="${model.patientSet.commaSeparatedPatientIds}" />
<script type="text/javascript">
	patientIds = '${model.patientSet.commaSeparatedPatientIds}';
</script>