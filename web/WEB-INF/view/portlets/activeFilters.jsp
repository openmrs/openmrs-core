<%@ include file="/WEB-INF/template/include.jsp" %>

<center><b><u><spring:message code="Analysis.activeFilters"/></u></b></center>
<p>
<c:choose>
	<c:when test="${fn:length(model.patientAnalysis.patientFilters) == 0}">
		<spring:message code="Analysis.noFiltersSelected"/>
	</c:when>
	<c:otherwise>
		<table>
		<c:forEach var="item" varStatus="stat" items="${model.patientAnalysis.patientFilters}">
			<tr><td>
				<div class="activeFilter">
					${item.value.description}
					<a href="${model.deleteURL}<c:choose><c:when test="${fn:contains(model.deleteURL, '?')}">&</c:when><c:otherwise>?</c:otherwise></c:choose>patient_filter_key=${item.key}">[X]</a>
				</div>
			</td></tr>
		</c:forEach>
		</table>
	</c:otherwise>
</c:choose>

<c:if test="${model.addURL != null}">
	<p>
	<a href="javascript:toggleLayer('suggestedFilterBox')"><spring:message code="Analysis.addFilter"/></a>
	<div id="suggestedFilterBox">
		<div style="float:right"><a href="javascript:toggleLayer('suggestedFilterBox')">[X]</a></div>
		<c:if test="${fn:length(model.suggestedFilters) == 0}">
			<spring:message code="Analysis.noFiltersAvailable"/> <br/>
		</c:if>
		<c:forEach var="item" items="${model.suggestedFilters}">
			<div class="inactiveFilter">
				<a href="${model.addURL}<c:choose><c:when test="${fn:contains(model.addURL, '?')}">&</c:when><c:otherwise>?</c:otherwise></c:choose>patient_filter_id=<c:out value="${item.reportObjectId}"/>">
					${item.name}
					<small><i>(${item.description})</i></small>
				</a>
			</div>
		</c:forEach>
	</div>
	<script language="JavaScript">
	<!--
		document.getElementById("suggestedFilterBox").style.display = "none";
	-->
	</script>
</c:if>