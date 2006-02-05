<%@ include file="/WEB-INF/template/include.jsp" %>

<style>
	#actionBox {
		background-color: #e0e0e0;
		padding: 4px;
	}
	#filterBox {
		width: 33%;
		float: right;
		border: 2px black solid;
		padding: 4px;
	}
	#activeFilterBox {
		width: 95%;
		border: 1px black solid;
		padding: 3px;
		spacing: 3px;
		background-color: #f0f0f0;
	}
	.filter {
		border: 1px black solid;
		background-color: #e0ffe0;
		padding: 4px 2px;
	}
	#patientSetBox {
		padding: 4px;
	}
</style>

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3 align="center"><spring:message code="Analysis.title"/></h3>

<div id="actionBox">
	<spring:message code="Analysis.actions"/>: [Reporting] [Visit Scheduling] [Drug Dispensing]
</div>

<div id="filterBox">
	<div id="activeFilterBox">
		<center><b><u><spring:message code="Analysis.activeFilters"/></u></b></center>
		<p>
		<c:if test="${model.no_filters}">
			<spring:message code="Analysis.noFiltersSelected"/>
		</c:if>
		<table>
		<c:forEach var="item" varStatus="stat" items="${model.filters}">
			<tr><td>
				<div class="filter">
					${item.description}
					<a href="analysis.form?method=removeFilter&patient_filter_index=<c:out value="${stat.index}"/>">[X]</a>
				</div>
			</td></tr>
		</c:forEach>
		</table>
	</div>

	<p>
	<a href="analysis.form?method=addFilter&patient_filter_id=1"><spring:message code="Analysis.addFilter"/> 1</a><br />
	<a href="analysis.form?method=addFilter&patient_filter_id=2"><spring:message code="Analysis.addFilter"/> 2</a><br />
	<a href="analysis.form?method=addFilter&patient_filter_id=3"><spring:message code="Analysis.addFilter"/> 3</a><br />
</div>

<div id="patientSetBox">
	<center><b><u><spring:message code="Analysis.currentPatientSet"/></u></b></center>
	<p>
	<i><spring:message code="Analysis.numPatients" arguments="${model.number_of_results}"/></i>
	<p>
	<pre>${model.analysis_results}</pre>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %> 