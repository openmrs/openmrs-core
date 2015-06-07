<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Observations" otherwise="/login.htm" redirect="/dictionary/conceptStats.form" />
<openmrs:message var="pageTitle" code="Concept.stats.title" scope="page" arguments="${concept.name}"/>	
<openmrs:message var="pageTitle" code="Concept.stats.titlebar" scope="page" arguments="${concept.name}"/>	

<%@ include file="/WEB-INF/template/header.jsp"%>


<style>
	.inlineForm {
		padding: 0px;
		margin: 0px;
		display: inline;
	}
	#conceptTable th {
		text-align: left;
	}
	#conceptNameTable th {
		text-align: left;	
	}
	#outliers {
		height: 100px;
		overflow: auto;
	}
</style>

<script type="text/javascript">
	function showHideOutliers(btn) {
		var table = document.getElementById("outliers");
		if (btn.innerHTML == '<openmrs:message code="Concept.stats.histogram.showOutliers"/>') {
			table.style.display = "";
			btn.innerHTML = '<openmrs:message code="Concept.stats.histogram.hideOutliers"/>';
		}
		else {
			table.style.display = "none";
			btn.innerHTML = '<openmrs:message code="Concept.stats.histogram.showOutliers"/>';
		}
		return false;
	}
	
	function jumpToConcept(which) {
		var action = document.getElementById('jumpAction');
		action.value = which;
		var jumpForm = document.getElementById('jumpForm');
		jumpForm.submit();
		return false;
	}

</script>

<h2><openmrs:message code="Concept.stats.title" arguments="${concept.name}" /></h2>

<c:if test="${concept.conceptId != null}">
	<form class="inlineForm" id="jumpForm" action="" method="post">
		<input type="hidden" name="jumpAction" id="jumpAction" value="previous"/>
		<a href="#previousConcept" id="previousConcept" valign="middle" accesskey="," onclick="return jumpToConcept('previous')"><openmrs:message code="general.previous"/></a>
			|
		<a href="concept.htm?conceptId=${concept.conceptId}" id="viewConcept" accesskey="v" ><openmrs:message code="general.view"/></a> |
		<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form?conceptId=${concept.conceptId}" accesskey="e" id="editConcept" valign="middle"></openmrs:hasPrivilege><openmrs:message code="general.edit"/><openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege> |
		<a href="#nextConcept" id="nextConcept" accesskey="." valign="middle" onclick="return jumpToConcept('next')"><openmrs:message code="general.next"/></a>
			|
	</form>
</c:if>

<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form" id="newConcept" valign="middle"></openmrs:hasPrivilege><openmrs:message code="general.new"/><openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFormHeader" type="html" parameters="conceptId=${command.concept.conceptId}" />

<form class="inlineForm" action="index.htm" method="get">
  &nbsp; &nbsp; &nbsp;
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<openmrs:message code="general.search"/>"/>
</form>

<br/><br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><div><openmrs:message code="Concept.retiredMessage"/></div></div>
</c:if>

<c:choose>
	<c:when test="${displayType == 'numeric'}">
		<table>
			<tr>
				<td><openmrs:message code="Concept.stats.numberObs"/></td>
				<td>${size}</td>
			</tr>
			<c:if test="${size > 0}">
				<tr>
					<td><openmrs:message code="Concept.stats.minValue"/></td>
					<td>${min}</td>
				</tr>
				<tr>
					<td><openmrs:message code="Concept.stats.maxValue"/></td>
					<td>${max}</td>
				</tr>
				<tr>
					<td><openmrs:message code="Concept.stats.meanValue"/></td>
					<td>${mean}</td>
				</tr>
				<tr>
					<td><openmrs:message code="Concept.stats.medianValue"/></td>
					<td>${median}</td>
				</tr>
				<tr>
					<td valign="top"><openmrs:message code="Concept.stats.histogram"/></td>
					<td>
						<openmrs:displayChart chart="${histogram}" width="800" height="300" />
					</td>
				</tr>
				<c:if test="${fn:length(outliers) > 0}">
					<tr>
						<td valign="top"><openmrs:message code="Concept.stats.histogramOutliers"/></td>
						<td>
							<openmrs:displayChart chart="${histogramOutliers}" width="800" height="300" />
							<br/> <a href="#" onclick="return showHideOutliers(this)"><openmrs:message code="Concept.stats.histogram.showOutliers"/></a> (<c:out value="${fn:length(outliers)}"/>)
							<br/>
							<div id="outliers" style="display: none">
								<table>
								<c:forEach items="${outliers}" var="outlier">
									<tr>
										<td><a target="_edit_obs" href="${pageContext.request.contextPath}/admin/observations/obs.form?obsId=${outlier.obsId}">
											<openmrs:message code="general.edit"/></a>
										</td>
										<td><b>${outlier.valueNumeric}</b></td>
										<td>(<openmrs:formatDate date="${outlier.obsDatetime}" type="long" />)</td>
									</tr>
								</c:forEach>
								</table>
							</div>
						</td>
					</tr>
				</c:if>
				<tr>
					<td valign="top"><openmrs:message code="Concept.stats.timeSeries"/></td>
					<td>
						<openmrs:displayChart chart="${timeSeries}" width="800" height="300" />
					</td>
				</tr>
			</c:if>
		</table>
	</c:when>
	<c:when test="${displayType == 'boolean'}">
		<openmrs:message code="Concept.stats.booleanPieChart"/>
		<br/>
		<br/>
		<openmrs:displayChart chart="${pieChart}" width="700" height="700" />
	</c:when>
	<c:when test="${displayType == 'coded'}">
		<openmrs:message code="Concept.stats.codedPieChart"/>
		<br/>
		<br/>
		<openmrs:displayChart chart="${pieChart}" width="700" height="700" />
	</c:when>
	<c:otherwise>
		<openmrs:message code="Concept.stats.notDisplayable"/>
	</c:otherwise>
</c:choose>
	
<%@ include file="/WEB-INF/template/footer.jsp"%>
