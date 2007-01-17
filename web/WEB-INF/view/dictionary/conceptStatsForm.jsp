<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View Observations" otherwise="/login.htm"
	redirect="/dictionary/conceptStats.form" />

<style>
	#newSearchForm {
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
</style>

<script type="text/javascript">

	function hotkeys(event) {
		var k = event.keyCode;
		if (event.cntrlKey == true) {
			if (k == 69) { // e
				document.location = document.getElementById('editConcept').href;
			}
		}
		if (k == 37) { // left key
			document.location = document.getElementById('previousConcept').href;
		}
		else if (k == 39) { //right key
			document.location = document.getElementById('nextConcept').href;
		}
	}
	
	document.onkeypress = hotkeys;

</script>

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<c:if test="${previousConcept != null}"><a href="concept.htm?conceptId=${previousConcept.conceptId}" id="previousConcept" valign="middle"><spring:message code="general.previous"/></a> |</c:if>
	<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form?conceptId=${concept.conceptId}" id="editConcept" valign="middle"></openmrs:hasPrivilege><spring:message code="general.edit"/><openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege> |
	<a href="conceptStats.form?conceptId=${concept.conceptId}" id="conceptStats" valign="middle"><spring:message code="Concept.stats"/></a> |
	<c:if test="${nextConcept != null}"><a href="concept.htm?conceptId=${nextConcept.conceptId}" id="nextConcept" valign="middle"><spring:message code="general.next"/></a></c:if> |
</c:if>

<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form" id="newConcept" valign="middle"></openmrs:hasPrivilege><spring:message code="general.new"/><openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFormHeader" type="html" />

<form id="newSearchForm" action="index.htm" method="get">
  &nbsp; &nbsp; &nbsp;
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.search"/>"/>
</form>

<br/><br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><div><spring:message code="Concept.retiredMessage"/></div></div>
</c:if>

<h2>${concept.name}</h2>

<c:choose>
	<c:when test="${empty obsNumerics}">
		<spring:message code="Concept.stats.notNumeric"/>
	</c:when>
	<c:otherwise>
		<table>
			<tr>
				<td><spring:message code="Concept.stats.numberObs"/></td>
				<td>${fn:length(obsNumerics)}</td>
			</tr>
			<c:if test="${fn:length(obsNumerics) > 0}">
				<tr>
					<td><spring:message code="Concept.stats.minValue"/></td>
					<td>${min}</td>
				</tr>
				<tr>
					<td><spring:message code="Concept.stats.maxValue"/></td>
					<td>${max}</td>
				</tr>
				<tr>
					<td><spring:message code="Concept.stats.meanValue"/></td>
					<td>${mean}</td>
				</tr>
				<tr>
					<td><spring:message code="Concept.stats.medianValue"/></td>
					<td>${median}</td>
				</tr>
				<tr>
					<td valign="top"><spring:message code="Concept.stats.histogram"/></td>
					<td>
						<openmrs:displayChart chart="${histogram}" width="500" height="300" />
					</td>
				</tr>
				<tr>
					<td valign="top"><spring:message code="Concept.stats.lineChart"/></td>
					<td>
						<openmrs:displayChart chart="${lineChart}" width="500" height="300" />
					</td>
				</tr>
			</table>
		</c:if>
	</c:otherwise>
</c:choose>
	


<%@ include file="/WEB-INF/template/footer.jsp"%>
