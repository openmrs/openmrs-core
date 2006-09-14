<%@ include file="/WEB-INF/template/include.jsp" %>

<style>
table#labTestTable {
	border: 2px solid black;
	border-spacing: 0px;
	border-collapse: collapse;
	margin: 2px;
}

table#labTestTable td {
	border: 1px solid black;
	padding: 3px;
}

table#labTestTable th {
	border: 1px solid black;
	padding: 3px;
}
</style>

	
	<div class="boxHeader"><spring:message code="patientDashboard.graphs"/></div>
	<div class="box">
		<table width="100%">
			<tr>
				<td align="center">
					<openmrs:obsTable observations="${model.patientObs}" concepts="name:CD4 COUNT|name:WEIGHT (KG)|set:name:LABORATORY EXAMINATIONS CONSTRUCT" id="labTestTable" showEmptyConcepts="false" />
				</td>
			</tr>
			<tr>
				<td align="center" id="weightGraphBox">
					<spring:message code="general.loading"/>
				</td>
			</tr>
			<tr>
				<td align="center" id="cd4GraphBox">
					<spring:message code="general.loading"/>
				</td>
			</tr>
		</table>
	</div>
	
	<script type="text/javascript">
		function loadGraphs() {
			document.getElementById('weightGraphBox').innerHTML = '<img src="${pageContext.request.contextPath}/showGraphServlet?patientId=${patient.patientId}&conceptId=<openmrs:globalProperty key="concept.weight" defaultValue="" />&width=500&height=300&minRange=0.0&maxRange=100.0"/>';
			document.getElementById('cd4GraphBox').innerHTML = '<img src="${pageContext.request.contextPath}/showGraphServlet?patientId=${patient.patientId}&conceptId=<openmrs:globalProperty key="concept.cd4_count" defaultValue="" />&width=500&height=300&minRange=0.0&maxRange=1000.0"/>';
		}
		window.setTimeout(loadGraphs, 1000);
	</script>