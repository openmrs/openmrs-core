<%@ include file="/WEB-INF/template/include.jsp" %>

<h3 align="center"><spring:message code"Cohort.summary" arguments="${model.patientSet.size}"/></h3>

	<div class="boxHeader"><spring:message code="Cohort.ageAndGender"/></div>
	<div class="box">
		<img src="${pageContext.request.contextPath}/pieChartServlet?width=500&height=300&chartTitle=Age&nbsp;and&nbsp;Gender"/>
	</div>
	
	<div class="boxHeader"><spring:message code="Cohort.hivEnrollments"/></div>
	<div class="box">
		<img src="${pageContext.request.contextPath}/timelineGraphServlet?width=900&height=300&startDate=2005-06-01&chartTitle=HIV&nbsp;Program&nbsp;Enrollments&rangeAxisTitle=Enrollments&domainAxisTitle=Month"/>
	</div>

<input type="hidden" id="hiddenPatientIds" value="${model.patientSet.commaSeparatedPatientIds}" />
<script type="text/javascript">
	patientIds = '${model.patientSet.commaSeparatedPatientIds}';
</script>