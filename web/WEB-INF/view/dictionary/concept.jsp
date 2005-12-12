<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="" otherwise="/login.htm"
	redirect="/dictionary/concept.htm" />

<style>
	#newSearchForm {
		padding: 0px;
		margin: 0px;
		display: inline;
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
	<c:if test="${previousConcept != null}"><a href="concept.htm?conceptId=${previousConcept.conceptId}" id="previousConcept" valign="middle">&laquo; Previous</a> |</c:if>
	<openmrs:hasPrivilege privilege="Edit Dictionary" converse="false">
		<a href="concept.form?conceptId=${concept.conceptId}" id="editConcept" valign="middle">Edit</a> |
	</openmrs:hasPrivilege>
	<c:if test="${nextConcept != null}"><a href="concept.htm?conceptId=${nextConcept.conceptId}" id="nextConcept" valign="middle">Next &raquo;</a></c:if>
</c:if>

<form id="newSearchForm" action="index.htm" method="get">
  &nbsp; &nbsp; &nbsp;
  Search 
  <input type="text" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.go"/>"/>
</form>

<br/><br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><div><spring:message code="Concept.retiredMessage"/></div></div>
</c:if>

<table>
	<tr>
		<td><spring:message code="general.id"/></td>
		<td>${concept.conceptId}</td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.name.help"/>">
			<spring:message code="general.name" />
		</td>
		<td><spring:bind path="conceptName.name">
				${status.value}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.shortName.help"/>">
			<spring:message code="Concept.shortName" />
		</td>
		<td><spring:bind path="conceptName.shortName">
			${status.value}
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description" /></td>
		<td valign="top"><spring:bind path="conceptName.description">
			${status.value}
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top" title="<spring:message code="Concept.synonyms.help"/>">
			<spring:message code="Concept.synonyms" />
		</td>
		<td valign="top">
			<c:forEach items="${conceptSynonyms}" var="syn">${syn}<br/></c:forEach>
		</td>
	</tr>
	<tr>
		<td  title="<spring:message code="Concept.conceptClass.help"/>">
			<spring:message code="Concept.conceptClass" />
		</td>
		<td valign="top">
			${concept.conceptClass.name}
		</td>
	</tr>
	<c:if test="${concept.conceptClass != null && concept.conceptClass.set}">
		<tr id="setOptions">
			<td valign="top"><spring:message code="Concept.conceptSets"/></td>
			<td valign="top">
				<c:forEach items="${conceptSets}" var="set">
					<a href="concept.htm?conceptId=${set.value[0]}">${set.value[1]} (${set.value[0]})</a><br/>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	<tr>
		<td title="<spring:message code="Concept.datatype.help"/>">
			<spring:message code="Concept.datatype" />
		</td>
		<td valign="top">
			${concept.datatype.name}
		</td>
	</tr>
	<c:if test="${concept.datatype != null && concept.datatype.name == 'Coded'}">
		<tr>
			<td valign="top"><spring:message code="Concept.answers"/></td>
			<td>
				<c:forEach items="${conceptAnswers}" var="answer">
					<a href="concept.htm?conceptId=${answer.key}">${answer.value} (${answer.key})</a><br/>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	<c:if test="${concept.numeric}">
		<tr>
			<td valign="top"><spring:message code="ConceptNumeric.name"/></td>
			<td>
				<spring:nestedPath path="concept.conceptNumeric">
					<table border="0">
						<tr>
							<th></th>
							<th><spring:message code="ConceptNumeric.low"/></th>
							<th><spring:message code="ConceptNumeric.high"/></th>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.absolute"/></th>
							<td valign="middle">
								<spring:bind path="lowAbsolute">
									${status.value}
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiAbsolute">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.critical"/></th>
							<td valign="middle">
								<spring:bind path="lowCritical">
									${status.value}
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiCritical">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.normal"/></th>
							<td valign="middle">
								<spring:bind path="lowNormal">
									${status.value}
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiNormal">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td></td>
							<td colspan="2"><small><em>(<spring:message code="ConceptNumeric.inclusive"/>)</em></small>
							</td>
						</tr>
						<tr>
							<td><spring:message code="ConceptNumeric.units"/></td>
							<td colspan="2">
								<spring:bind path="units">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td><spring:message code="ConceptNumeric.precise"/></td>
							<td colspan="2">
								<spring:bind path="precise">
									<c:if test="${status.value}">Yes</c:if>
									<c:if test="${!status.value}">No</c:if>
								</spring:bind>
							</td>
						</tr>
					</table>
				</spring:nestedPath>
			</td>
	</c:if>
	<tr>
		<td><spring:message code="Concept.version" /></td>
		<td><spring:bind path="concept.version">
			${status.value}
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="general.retired" /></td>
		<td><spring:bind path="concept.retired">
			${status.value}
		</spring:bind></td>
	</tr>
	<c:if test="${!(concept.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${concept.creator.firstName} ${concept.creator.lastName} -
				<openmrs:formatDate date="${concept.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(concept.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td>
				${concept.changedBy.firstName} ${concept.changedBy.lastName} -
				<openmrs:formatDate date="${concept.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
		<td valign="top">Resources</td>
		<td>
			<a href="index.htm?phrase=${conceptName.name}"
			       target="_similar_terms">Similar Concepts</a><br/>
			<a href="http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=${conceptName.name}"
			       target="_blank">Merriam Webster&reg;</a><br/>
			<a href="http://www.google.com/search?q=${conceptName.name}"
			       target="_blank">Google&trade;</a><br/>
			<a href="http://www.utdol.com/application/vocab.asp?search=${conceptName.name}&submit=Go"
			       target="_blank">UpToDate&reg;</a><br/>
			<a href="http://dictionary.reference.com/search?q=${conceptName.name}&submit=Go"
			       target="_blank">Dictionary.com&reg;</a><br/>
			<a href="http://search.atomz.com/search/?sp-q=${conceptName.name}&sp-a=sp1001878c"
			       target="_blank">Lab Tests Online</a>
		</td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>
