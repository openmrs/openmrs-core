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
	#suggestedFilterBox {
		border: 1px solid black;
		margin-bottom: 15px;
		background-color: #ffe0e0;
		position: absolute;
		z-index: 1;
	}
	.activeFilter {
		border: 1px black solid;
		background-color: #e0ffe0;
		padding: 4px 2px;
	}
	.inactiveFilter {
		border: 1px black solid;
		background-color: #e0e0ff;
		padding: 4px 2px;		
	}
	#patientSetBox {
		padding: 4px;
	}
</style>
<script language="JavaScript">
<!--
	function toggleLayer(whichLayer) {
		if (document.getElementById) {
            var style2 = document.getElementById(whichLayer).style;
         	if (style2.display == "none") {
                style2.display = "";
            } else {
                style2.display = "none";
            }
        } else {
            window.alert("Your browser doesn't support document.getElementById");
        }
	}
-->
</script>

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
		<c:forEach var="item" varStatus="stat" items="${model.active_filters}">
			<tr><td>
				<div class="activeFilter">
					${item.description}
					<a href="analysis.form?method=removeFilter&patient_filter_index=<c:out value="${stat.index}"/>">[X]</a>
				</div>
			</td></tr>
		</c:forEach>
		</table>
	</div>

	<p>
	<a href="javascript:toggleLayer('suggestedFilterBox')"><spring:message code="Analysis.addFilter"/></a>
	<div id="suggestedFilterBox">
		<div style="float:right"><a href="javascript:toggleLayer('suggestedFilterBox')">[X]</a></div>
		<c:forEach var="item" items="${model.suggested_filters}">
			<div class="inactiveFilter">
				<a href="analysis.form?method=addFilter&patient_filter_id=<c:out value="${item.reportObjectId}"/>">${item.description}</a>
			</div>
		</c:forEach>
	</div>
	<script language="JavaScript">
	<!--
		document.getElementById("suggestedFilterBox").style.display = "none";
	-->
	</script>
</div>

<div id="patientSetBox">
	<center><b><u><spring:message code="Analysis.currentPatientSet"/></u></b></center>
	<p>
	<i><spring:message code="Analysis.numPatients" arguments="${model.number_of_results}"/></i>
	<p>
	<c:if test="${model.xml_debug != null}">
		<pre><c:out value="${model.xml_debug}" escapeXml="true"/></pre>
	</c:if>
	
	<pre>${model.analysis_results}</pre>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %> 