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
	.filter {
		border: 1px black solid;
		background-color: #e0ffe0;
		padding: 4px 2px;
	}
	#patientSetBox {
		padding: 4px;
	}
</style>

<div id="actionBox">
	Actions: [Reporting] [Visit Scheduling] [Drug Dispensing]
</div>

<div id="filterBox">
	<center><b><u>Filters</u></b></center>
	<p>
	<c:if test="${model.no_filters}">
		You haven't selected any filters
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

	<p>
	<a href="analysis.form?method=addFilter&patient_filter_id=1">Add filter 1</a><br />
	<a href="analysis.form?method=addFilter&patient_filter_id=2">Add filter 2</a><br />
	<a href="analysis.form?method=addFilter&patient_filter_id=3">Add filter 3</a><br />
</div>

<div id="patientSetBox">
	<center><b><u>Patient Set</u></b></center>
	<p>
	<i>${model.number_of_results} patient(s)</i>
	<p>
	<pre>${model.analysis_results}</pre>
</div>