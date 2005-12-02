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
<script type="text/javascript">

	var nameListBox;
	var idListBox;

	window.onload = function() {
		myConceptSearchMod = new fx.Resize("conceptSearchForm", {duration: 100});
		myConceptSearchMod.hide();
	};
	function removeItem(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		var lastIndex = -1;
		var i = 0;
		while (i<optList.length) {
			// loop over and erase all selected items
			if (optList[i].selected) {
				optList[i] = null;
				lastIndex = i;
			}
			else {
				i++;
			}
		}
		while (lastIndex >= optList.length)
			lastIndex = lastIndex - 1;
		if (lastIndex >= 0)
			optList[lastIndex].selected = true;
		copyIds(nameList, idList);
	}
	function addConcept(nameList, idList)
	{
		nameList = document.getElementById(nameList);
		idList   = document.getElementById(idList);
		if (idList != idListBox) {
			myConceptSearchMod.hide();
			nameListBox = nameList;	// used by onSelect()
			idListBox   = idList;	// used by onSelect()
		}
		
		conceptSearchForm = document.getElementById("conceptSearchForm");
		conceptSearchForm.style.left = (getElementLeft(nameList) + nameList.offsetWidth + 20) + "px";
		conceptSearchForm.style.top = (getElementTop(nameList)-50) + "px";
		
		DWRUtil.removeAllRows("conceptSearchBody");
		
		myConceptSearchMod.toggle();
		var searchText = document.getElementById("searchText");
		searchText.value = '';
		searchText.select();
		//searchText.focus();  //why does this cause the inner box to shift position?!?
	}
	function moveUp(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		for (var i=1; i<optList.length; i++) {
			// loop over and move up all selected items
			if (optList[i].selected) {
				var id   = optList[i].value;
				var name = optList[i].text;
				optList[i].value = optList[i-1].value;
				optList[i].text  = optList[i-1].text;
				optList[i].selected = false;
				optList[i-1].value = id;
				optList[i-1].text  = name;
				optList[i-1].selected = true;
			}
		}
		copyIds(nameList, idList);
	}
	function moveDown(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		for (var i=optList.length-2; i>=0; i--) {
			if (optList[i].selected) {
				var id   = optList[i].value;
				var name = optList[i].text;
				optList[i].value = optList[i+1].value;
				optList[i].text  = optList[i+1].text;
				optList[i].selected = false;
				optList[i+1].value = id;
				optList[i+1].text  = name;
				optList[i+1].selected = true;
			}
		}
		copyIds(nameList, idList);
	}
	function copyIds(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		var remaining = new Array();
		var i=0;
		while (i < optList.length)
		{
			remaining.push(optList[i].value);
			i++;
		}
		input.value = remaining.join(' ');
	}
	
	function getElementLeft(elm) {
		var x = 0;
		while (elm != null) {
			x+= elm.offsetLeft;
			elm = elm.offsetParent;
		}
		return parseInt(x);
	}
	function getElementTop(elm) {
		var y = 0;
		while (elm != null) {
			y+= elm.offsetTop;
			elm = elm.offsetParent;
		}
		return parseInt(y);
	}
	function addSynonym(event) {
		if (event == null || event.keyCode == 13) {
			var obj = document.getElementById("addSynonym");
			var synonyms = document.getElementById("syns").options;
			if (obj.value != "") {
				var addable = true;
				for (var i=0; i<synonyms.length; i++) {
					if (synonyms[i].value == obj.value)
						addable = false;
				}
				if (addable) {
					var opt = new Option(obj.value, obj.value);
					opt.selected = true;
					synonyms[synonyms.length] = opt;
				}
			}
			obj.value = "";
			obj.focus();
		}
		return false;
	}
	
	var onSelect = function(conceptList) {
		var options = nameListBox.options;
		for (i=0; i<conceptList.length; i++) {
			var addable = true;	
			var conceptId = conceptList[i].conceptId;
			var conceptName = conceptList[i].name;
			for (x=0; x<options.length; x++) {
				if (options[x].value == conceptId) {
					addable = false;
				}
			}
			if (addable) {
				var opt = new Option(conceptName + ' ('+conceptId+')', conceptId);
				opt.selected = true;
				options[options.length] = opt;
			}
				
		}
		copyIds(nameListBox.id, idListBox.id);
		myConceptSearchMod.hide();
		nameListBox.focus();
	};
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
		
</style>

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<a href="concept.form?conceptId=${concept.conceptId - 1}">&laquo; Previous</a> |
	<a href="concept.htm?conceptId=${concept.conceptId}">View</a> |
	<a href="concept.form?conceptId=${concept.conceptId + 1}">Next &raquo;</a>
</c:if>

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

<form method="post" action="">
<table>
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
		<td valign="top"><spring:bind path="concept.description">
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
			<input type="text" size="40" id="addSynonym" onKeyUp="return addSynonym(event);"/> <input type="button" class="smallButton" value="Add Synonym" onClick="addSynonym();"/>
			<input type="hidden" name="synonyms" id="synonyms" value="<c:forEach items="${conceptSynonyms}" var="syn">${syn}||</c:forEach>" />
			<br/>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<select size="5" multiple id="syns">
							<c:forEach items="${conceptSynonyms}" var="syn"><option value="${syn}">${syn}</option></c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('syns', 'synonyms');" /> <br/>
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
			<select name="${status.expression}">
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
	<c:if test="${concept.conceptClass != null && concept.conceptClass.set}">
		<tr id="setOptions">
			<td valign="top"><spring:message code="Concept.conceptSets"/></td>
			<td valign="top">
				<input type="hidden" name="conceptSets" id="conceptSets" size="40" value='<c:forEach items="${conceptSets}" var="set">${set.key} </c:forEach>' />
				<table cellpadding="0" cellspacing="0">
					<tr>
						<td valign="top">
							<select size="6" id="conceptSetsNames" multiple>
								<c:forEach items="${conceptSets}" var="set">
									<option value="${set.value[0]}">${set.value[1]} (${set.value[0]})</option>
								</c:forEach>
							</select>
						</td>
						<td valign="top" class="buttons">
							<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('conceptSetsNames', 'conceptSets');" /> <br/>
							<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('conceptSetsNames', 'conceptSets');" /> <br/>
							<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('conceptSetsNames', 'conceptSets');" /><br/>
							<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('conceptSetsNames', 'conceptSets');" /><br/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<tr>
		<td title="<spring:message code="Concept.datatype.help"/>">
			<spring:message code="Concept.datatype" />
		</td>
		<td valign="top"><spring:bind path="concept.datatype">
			<select name="${status.expression}" onChange="changeDatatype(this);">
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
	<c:if test="${concept.datatype != null && concept.datatype.name == 'Coded'}">
		<tr>
			<td valign="top"><spring:message code="Concept.answers"/></td>
			<td>
				<input type="hidden" name="answers" id="answerIds" size="40" value='<c:forEach items="${conceptAnswers}" var="answer">${answer.key} </c:forEach>' />
				<table cellspacing="0" cellpadding="0">
					<tr>
						<td valign="top">
							<select size="6" id="answerNames" multiple>
								<c:forEach items="${conceptAnswers}" var="answer">
									<option value="${answer.key}">${answer.value} (${answer.key})</option>
								</c:forEach>
							</select>
						</td>
						<td valign="top" class="buttons">
							<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('answerNames', 'answerIds');"/><br/>
							<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('answerNames', 'answerIds');"/><br/>
						</td>
					</tr>
				</table>
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
	</c:if>
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
</table>
<input type="submit" value="<spring:message code="Concept.save"/>" /></form>

<div id="conceptSearchForm">
	<div id="wrapper">
		<input type="button" onClick="myConceptSearchMod.toggle(); return false;" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('conceptSearchBody', null, searchText); return null;">
			<h3><spring:message code="Concept.find"/></h3>
			<input type="text" id="searchText" size="45" onkeyup="searchBoxChange('conceptSearchBody', event, this, 400);">
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
