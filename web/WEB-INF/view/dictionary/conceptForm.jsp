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
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
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
		margin: 1px;
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
		height: 220px;
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
	.smallWidth {
		width: 140px;
		}
		select.smallWidth {
			width: 144px;
		}
	.mediumWidth {
		width: 340px;
		}
		select.mediumWidth {
			width: 344px;
		}
	#conceptTable th {
		text-align: left;
	}
</style>

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<c:if test="${previousConcept != null}"><a href="concept.form?conceptId=${previousConcept.conceptId}">Previous</a> |</c:if>
	<a href="concept.htm?conceptId=${concept.conceptId}" id="viewConcept" >View</a> |
	<c:if test="${nextConcept != null}"><a href="concept.form?conceptId=${nextConcept.conceptId}">Next</a></c:if>
</c:if>

<form id="newSearchForm" action="index.htm" method="get">
  &nbsp; &nbsp; 
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.go"/>"/>
</form>

<br/><br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><div><spring:message code="Concept.retiredMessage"/></div></div>
</c:if>

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
<table id=conceptTable>
	<tr>
		<th><spring:message code="general.id"/></th>
		<td>${concept.conceptId}</td>
	</tr>
	<tr>
		<th title="<spring:message code="Concept.name.help"/>">
			<spring:message code="general.name" />
		</th>
		<td><spring:bind path="conceptName.name">
			<input type="text" name="${status.expression}"
				value="${status.value}" class="mediumWidth" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<th title="<spring:message code="Concept.shortName.help"/>">
			<spring:message code="Concept.shortName" />
		</th>
		<td><spring:bind path="conceptName.shortName">
			<input class="smallWidth" type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<th valign="top" title="<spring:message code="Concept.description.help"/>">
			<spring:message code="general.description" />
		</th>
		<td valign="top"><spring:bind path="conceptName.description">
			<textarea name="${status.expression}" rows="4" cols="50">${status.value}</textarea>
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<th valign="top" title="<spring:message code="Concept.synonyms.help"/>">
			<spring:message code="Concept.synonyms" />
		</th>
		<td valign="top">
			<input type="text" class="mediumWidth" id="addSyn" onKeyDown="return synonymKeyPress(this, event);"/>
			<input type="button" class="smallButton" value="<spring:message code="Concept.synonym.add"/>" onClick="addSynonym();"/>
			<input type="hidden" name="newSynonyms" id="newSynonyms" value="<c:forEach items="${conceptSynonyms}" var="syn">${syn},</c:forEach>" />
		</td>
	</tr>
	<tr>
		<th></th>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<select class="mediumWidth" size="5" multiple id="syns" onkeydown="listKeyPress('syns', 'newSynonyms', ',', event);">
							<c:forEach items="${conceptSynonyms}" var="syn"><option value="${syn}">${syn}</option></c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('syns', 'newSynonyms', ',');" /> <br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th title="<spring:message code="Concept.conceptClass.help"/>">
			<spring:message code="Concept.conceptClass" />
		</th>
		<td valign="top"><spring:bind path="concept.conceptClass">
			<select class="smallWidth" name="${status.expression}" id="conceptClass" onChange="changeClass(this);">
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
		<th valign="top"><spring:message code="Concept.conceptSets"/></th>
		<td valign="top">
			<input type="hidden" name="conceptSets" id="conceptSets" size="40" value='<c:forEach items="${conceptSets}" var="set">${set.value[0]} </c:forEach>' />
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select class="mediumWidth" size="6" id="conceptSetsNames" multiple onkeyup="listKeyPress('conceptSetsNames', 'conceptSets', ' ', event);">
							<c:forEach items="${conceptSets}" var="set">
								<option value="${set.value[0]}">${set.value[1]} (${set.value[0]})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('conceptSetsNames', 'conceptSets', this);" /> <br/>
						&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('conceptSetsNames', 'conceptSets', ' ');" /> <br/>
						&nbsp;<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('conceptSetsNames', 'conceptSets');" /><br/>
						&nbsp;<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('conceptSetsNames', 'conceptSets');" /><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th title="<spring:message code="Concept.datatype.help"/>">
			<spring:message code="Concept.datatype" />
		</th>
		<td valign="top"><spring:bind path="concept.datatype">
			<select class="smallWidth" name="${status.expression}" id="datatype" onChange="changeDatatype(this);">
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
		<th valign="top"><spring:message code="Concept.answers"/></th>
		<td>
			<input type="hidden" name="answers" id="answerIds" size="40" value='<c:forEach items="${conceptAnswers}" var="answer">${answer.key} </c:forEach>' />
			<table cellspacing="0" cellpadding="0">
				<tr>
					<td valign="top">
						<select class="mediumWidth" size="6" id="answerNames" multiple onKeyUp="listKeyPress('answerNames', 'answerIds', ' ', event)">
							<c:forEach items="${conceptAnswers}" var="answer">
								<option value="${answer.key}">${answer.value} (${answer.key})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('answerNames', 'answerIds', this);"/><br/>
						&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('answerNames', 'answerIds', ' ');"/><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr id="numericDatatypeRow">
		<th valign="top"><spring:message code="ConceptNumeric.name"/></th>
		<td>
			<table border="0">
				<tr>
					<th></th>
					<th><spring:message code="ConceptNumeric.low"/></th>
					<th><spring:message code="ConceptNumeric.high"/></th>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.absolute"/></th>
					<td valign="middle">
						<input type="text" name="lowAbsolute" value="<c:if test="${concept.numeric}">${concept.lowAbsolute}</c:if>" size="10" />
					</td>
					<td valign="middle">
						<input type="text" name="hiAbsolute" value="<c:if test="${concept.numeric}">${concept.hiAbsolute}</c:if>" size="10"/>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.critical"/></th>
					<td valign="middle">
						<input type="text" name="lowCritical" value="<c:if test="${concept.numeric}">${concept.lowCritical}</c:if>" size="10" />
					</td>
					<td valign="middle">
						<input type="text" name="hiCritical" value="<c:if test="${concept.numeric}">${concept.hiCritical}</c:if>" size="10"/>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.normal"/></th>
					<td valign="middle">
						<input type="text" name="lowNormal" value="<c:if test="${concept.numeric}">${concept.lowNormal}</c:if>" size="10" />
					</td>
					<td valign="middle">
						<input type="text" name="hiNormal" value="<c:if test="${concept.numeric}">${concept.hiNormal}</c:if>" size="10"/>
					</td>
				</tr>
				<tr>
					<td></td>
					<td colspan="2"><small><em>(<spring:message code="ConceptNumeric.inclusive"/>)</em></small>
					</td>
				</tr>
				<tr>
					<th><spring:message code="ConceptNumeric.units"/></th>
					<td colspan="2">
						<input type="text" name="units" value="<c:if test="${concept.numeric}">${concept.units}</c:if>" size="15"/>
					</td>
				</tr>
				<tr>
					<th><spring:message code="ConceptNumeric.precise"/></th>
					<td colspan="2">
						<input type="hidden" name="_precise" value=""/>
						<input type="checkbox" name="precise" <c:if test="${concept.numeric && concept.precise}">checked="checked"</c:if>/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Concept.version" /></th>
		<td><spring:bind path="concept.version">
			<input class="smallWidth" type="text" name="${status.expression}"
				value="${status.value}" size="10" />
			<c:if test="${status.errorMessage != ''}">
				<span class="error">${status.errorMessage}</span>
			</c:if>
		</spring:bind></td>
	</tr>
	<tr>
		<th><spring:message code="general.retired" /></th>
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
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${concept.creator.firstName} ${concept.creator.lastName} -
				<openmrs:formatDate date="${concept.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(concept.changedBy == null)}">
		<tr>
			<th><spring:message code="general.changedBy" /></th>
			<td>
				${concept.changedBy.firstName} ${concept.changedBy.lastName} -
				<openmrs:formatDate date="${concept.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
		<th valign="top">Resources</th>
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
		<input type="button" onClick="return closeConceptBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('conceptSearchBody', searchText, null, false, 0); return false;">
			<h3><spring:message code="Concept.find"/></h3>
			<input type="text" id="searchText" size="45" onkeyup="return searchBoxChange('conceptSearchBody', this, event, false, 400);">
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

<script type="text/javascript">
	document.getElementById("searchPhrase").focus();
</script>


<%@ include file="/WEB-INF/template/footer.jsp"%>
