<%@ include file="/WEB-INF/template/include.jsp" %>

<b><u><spring:message code="Analysis.activeFilters"/></u></b>
<c:choose>
	<c:when test="${fn:length(model.patientAnalysis.patientFilters) == 0}">
		<br/>
		<spring:message code="Analysis.noFiltersSelected"/>
	</c:when>
	<c:otherwise>
		<table>
		<c:forEach var="item" varStatus="stat" items="${model.patientAnalysis.patientFilters}">
			<tr><td>
				<div class="activeFilter">
					<c:choose>
						<c:when test="${item.value.name != null}">
							${item.value.name}
						</c:when>
						<c:otherwise>
							${item.value.description}
						</c:otherwise>
					</c:choose>
					<a href="${model.deleteURL}<c:choose><c:when test="${fn:contains(model.deleteURL, '?')}">&</c:when><c:otherwise>?</c:otherwise></c:choose>patient_filter_key=${item.key}">[X]</a>
				</div>
			</td></tr>
		</c:forEach>
		</table>
	</c:otherwise>
</c:choose>

<c:if test="${model.addURL != null}">
	<p>
	<b><u><spring:message code="Analysis.addFilter"/></u></b>
	<ul>
		<c:if test="${fn:length(model.suggestedFilters) == 0}">
			<li><spring:message code="Analysis.noFiltersAvailable"/></li>
		</c:if>
		<c:forEach var="item" items="${model.suggestedFilters}">
			<li>
				<a href="${model.addURL}<c:choose><c:when test="${fn:contains(model.addURL, '?')}">&</c:when><c:otherwise>?</c:otherwise></c:choose>patient_filter_id=<c:out value="${item.reportObjectId}"/>">
					${item.name}
					<%-- <small><i>(${item.description})</i></small> --%>
				</a>
			</li>
		</c:forEach>
	</ul>
	
	<%--
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
	--%>
</c:if>