<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Edit Dictionary" otherwise="/login.htm"
	redirect="/dictionary/concept.form" />

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript" src="conceptForm.js"></script>
<script type="text/javascript">
	var setClasses = new Array();
	<c:forEach items="${classes}" var="cc">
	<c:if test="${cc.set}">setClasses.push("${cc.conceptClassId}");</c:if>
	</c:forEach>
</script>

<style>
	.smallButton {
		border: 1px solid lightgrey;
		background-color: whitesmoke;
		cursor: pointer;
		width: 75px;
		margin: 2px;
	}
	#conceptSearchForm {
		width: 500px;
		position: absolute;
		z-index: 10;
		margin: 5px;
	}
	#conceptSearchForm #wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 275px;
	}
	#conceptSearchResults {
		height: 200px;
		overflow: auto;
	}
	.closeButton {
		border: 1px solid gray;
		background-color: lightpink;
		font-size: 8px;
		color: black;
		float: right;
		margin: 2px;
		padding: 1px;
		cursor: pointer;
	}
	#newSearchForm {
		padding: 0px;
		margin: 0px;
		display: inline;
	}
		
</style>

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<a href="concept.form?conceptId=${concept.conceptId - 1}">&laquo; Previous</a> |
	<a href="concept.htm?conceptId=${concept.conceptId}">View</a> |
	<a href="concept.form?conceptId=${concept.conceptId + 1}">Next &raquo;</a>
</c:if>

<form id="newSearchForm" action="index.htm" method="get">
&nbsp; &nbsp; <input type="text" name="phrase" size="18"> <input type="submit" class="smallButton" value="<spring:message code="general.go"/>"/>
</form>

<br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><spring:message code="Concept.retiredMessage"/></div>
</c:if>
<br/>

<spring:hasBindErrors name="concept">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<form method="post" action="" onSubmit="removeHiddenRows()">
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
			<input type="text" name="${status.expression}"
				value="${status.value}" size="50" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.shortName.help"/>">
			<spring:message code="Concept.shortName" />
		</td>
		<td><spring:bind path="conceptName.shortName">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top" title="<spring:message code="Concept.description.help"/>">
			<spring:message code="general.description" />
		</td>
		<td valign="top"><spring:bind path="conceptName.description">
			<textarea name="${status.expression}" rows="4" cols="50">${status.value}</textarea>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top" title="<spring:message code="Concept.synonyms.help"/>">
			<spring:message code="Concept.synonyms" />
		</td>
		<td valign="top">
			<input type="text" size="40" id="addSyn" onKeyDown="if (event.keyCode==13) {addSynonym(); return false;}"/> <input type="button" class="smallButton" value="<spring:message code="Concept.synonym.add"/>" onClick="addSynonym();"/>
			<input type="hidden" name="newSynonyms" id="newSynonyms" value="<c:forEach items="${conceptSynonyms}" var="syn">${syn},</c:forEach>" />
		</td>
	</tr>
	<tr>
		<td></td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<select size="5" multiple id="syns" onkeydown="listKeyPress('syns', 'newSynonyms', ',', event);">
							<c:forEach items="${conceptSynonyms}" var="syn"><option value="${syn}">${syn}</option></c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('syns', 'synonyms', ',');" /> <br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.conceptClass.help"/>">
			<spring:message code="Concept.conceptClass" />
		</td>
		<td valign="top"><spring:bind path="concept.conceptClass">
			<select name="${status.expression}" id="conceptClass" onChange="changeClass(this);">
				<c:forEach items="${classes}" var="cc">
					<option value="${cc.conceptClassId}"
						<c:if test="${cc.conceptClassId == status.value}">selected="selected"</c:if>>${cc.name}</option>
				</c:forEach>
			</select>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr id="setClassRow">
		<td valign="top"><spring:message code="Concept.conceptSets"/></td>
		<td valign="top">
			<input type="hidden" name="conceptSets" id="conceptSets" size="40" value='<c:forEach items="${conceptSets}" var="set">${set.key} </c:forEach>' />
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select size="6" id="conceptSetsNames" multiple onkeyup="listKeyPress('conceptSetsNames', 'conceptSets', ' ', event);">
							<c:forEach items="${conceptSets}" var="set">
								<option value="${set.value[0]}">${set.value[1]} (${set.value[0]})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('conceptSetsNames', 'conceptSets', this);" /> <br/>
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('conceptSetsNames', 'conceptSets', ' ');" /> <br/>
						<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('conceptSetsNames', 'conceptSets');" /><br/>
						<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('conceptSetsNames', 'conceptSets');" /><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.datatype.help"/>">
			<spring:message code="Concept.datatype" />
		</td>
		<td valign="top"><spring:bind path="concept.datatype">
			<select name="${status.expression}" id="datatype" onChange="changeDatatype(this);">
				<c:forEach items="${datatypes}" var="cd">
					<option value="${cd.conceptDatatypeId}"
						<c:if test="${cd.conceptDatatypeId == status.value}">selected="selected"</c:if>>${cd.name}</option>
				</c:forEach>
			</select>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr id="codedDatatypeRow">
		<td valign="top"><spring:message code="Concept.answers"/></td>
		<td>
			<input type="hidden" name="answers" id="answerIds" size="40" value='<c:forEach items="${conceptAnswers}" var="answer">${answer.key} </c:forEach>' />
			<table cellspacing="0" cellpadding="0">
				<tr>
					<td valign="top">
						<select size="6" id="answerNames" multiple onKeyUp="listKeyPress('answerNames', 'answerIds', ' ', event)">
							<c:forEach items="${conceptAnswers}" var="answer">
								<option value="${answer.key}">${answer.value} (${answer.key})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('answerNames', 'answerIds', this);"/><br/>
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('answerNames', 'answerIds', ' ');"/><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr id="numericDatatypeRow">
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
								<input type="text" name="${status.expression}" value="${status.value}" size="10" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
						<td valign="middle">
							<spring:bind path="hiAbsolute">
								<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
					<tr>
						<th valign="middle"><spring:message code="ConceptNumeric.critical"/></th>
						<td valign="middle">
							<spring:bind path="lowCritical">
								<input type="text" name="${status.expression}" value="${status.value}" size="10" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
						<td valign="middle">
							<spring:bind path="hiCritical">
								<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
					<tr>
						<th valign="middle"><spring:message code="ConceptNumeric.normal"/></th>
						<td valign="middle">
							<spring:bind path="lowNormal">
								<input type="text" name="${status.expression}" value="${status.value}" size="10" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
						<td valign="middle">
							<spring:bind path="hiNormal">
								<input type="text" name="${status.expression}" value="${status.value}" size="10"/>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
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
								<input type="text" name="${status.expression}" value="${status.value}" size="15"/>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
					<tr>
						<td><spring:message code="ConceptNumeric.precise"/></td>
						<td colspan="2">
							<spring:bind path="precise">
								<input type="hidden" name="_${status.expression}" value=""/>
								<input type="checkbox" name="${status.expression}" <c:if test="${status.value}">checked="checked"</c:if>/>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
				</table>
			</spring:nestedPath>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Concept.version" /></td>
		<td><spring:bind path="concept.version">
			<input type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="general.retired" /></td>
		<td><spring:bind path="concept.retired">
			<input type="hidden" name="_${status.expression}">
			<input type="checkbox" name="${status.expression}" value="true"
				<c:if test="${status.value == true}">checked</c:if> />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
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
<input type="submit" value="<spring:message code="Concept.save"/>" /></form>

<div id="conceptSearchForm">
	<div id="wrapper">
		<input type="button" onClick="myConceptSearchMod.toggle(); return false;" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('conceptSearchBody', searchText); return null;">
			<h3><spring:message code="Concept.find"/></h3>
			<input type="text" id="searchText" size="45" onkeyup="searchBoxChange('conceptSearchBody', this, event, 400);">
		</form>
		<div id="conceptSearchResults">
			<table>
				<tbody id="conceptSearchBody">
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
