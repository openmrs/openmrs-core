<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/concept.htm" />

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
	.localeSpecific td, a.selectedTab {
		background-color: whitesmoke;
	}
	a.tab{
		border-bottom: 1px solid whitesmoke;
		padding-left: 3px;
		padding-right: 3px;
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

	function selectTab(tab) {
		var displays = new Array();
		
		var tabs = tab.parentNode.getElementsByTagName("a");
		for (var tabIndex=0; tabIndex<tabs.length; tabIndex++) {
			var index = tabs[tabIndex].id.indexOf("Tab");
			var tabName = tabs[tabIndex].id.substr(0, index);
			if (tabs[tabIndex] == tab) {
				displays[tabName] = "";
				addClass(tabs[tabIndex], 'selectedTab');
			}
			else {
				displays[tabName] = "none";
				removeClass(tabs[tabIndex], 'selectedTab');
			}
		}
		
		var parent = tab.parentNode.parentNode.parentNode;
		var elements = parent.getElementsByTagName("td");	
		for (var i=0; i<elements.length; i++) {
			if (displays[elements[i].className] != null)
					elements[i].style.display = displays[elements[i].className];
		}
		
		tab.blur();
		return false;
	}

</script>

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<c:if test="${previousConcept != null}"><a href="concept.htm?conceptId=${previousConcept.conceptId}" id="previousConcept" valign="middle"><spring:message code="general.previous"/></a> |</c:if>
	<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form?conceptId=${concept.conceptId}" id="editConcept" valign="middle"></openmrs:hasPrivilege>
		<spring:message code="general.edit"/>
	<openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege>
	|
	<c:if test="${nextConcept != null}"><a href="concept.htm?conceptId=${nextConcept.conceptId}" id="nextConcept" valign="middle"><spring:message code="general.next"/></a></c:if> |
</c:if>

<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form" id="newConcept" valign="middle"></openmrs:hasPrivilege>
	<spring:message code="general.new"/>
<openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege>

<form id="newSearchForm" action="index.htm" method="get">
  &nbsp; &nbsp; &nbsp;
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.search"/>"/>
</form>

<br/><br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><div><spring:message code="Concept.retiredMessage"/></div></div>
</c:if>

<table id="conceptTable" cellpadding="2" cellspacing="0">
	<tr>
		<th><spring:message code="general.id"/></th>
		<td colspan="${fn:length(locales)}">${concept.conceptId}</td>
	</tr>
	
	<tr>
		<th><spring:message code="general.locale"/></th>
		<td style="padding-bottom: 0px; padding-left: 0px;">
			<c:forEach items="${locales}" var="loc" varStatus="varStatus">
				<a id="${loc}Tab" class="tab ${loc}" href="#select${loc.displayName}" onclick="return selectTab(this)">${loc.displayName}</a><c:if test="${varStatus.last==false}"> | </c:if>
			</c:forEach>
		</td>
	</tr>
	<tr class="localeSpecific">
		<th title="<spring:message code="Concept.name.help"/>">
			<spring:message code="general.name" />
		</th>
		<c:forEach items="${locales}" var="loc">
			<td class="${loc}">
				<spring:bind path="conceptName_${loc}.name">
					${status.value}
				</spring:bind>
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th title="<spring:message code="Concept.shortName.help"/>">
			<spring:message code="Concept.shortName" />
		</th>
		<c:forEach items="${locales}" var="loc">
			<td class="${loc}">
				<spring:bind path="conceptName_${loc}.shortName">
					${status.value}
				</spring:bind>
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th valign="top"><spring:message code="general.description" /></th>
		<c:forEach items="${locales}" var="loc">
			<td valign="top" class="${loc}">
				<spring:bind path="conceptName_${loc}.description">
					${status.value}
				</spring:bind>
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th valign="top" title="<spring:message code="Concept.synonyms.help"/>">
			<spring:message code="Concept.synonyms" />
		</th>
		<c:forEach items="${locales}" var="loc">
			<td valign="top" class="${loc}">
				<c:forEach items="${conceptSynonymsByLocale[loc]}" var="syn">${syn}<br/></c:forEach>
			</td>
		</c:forEach>
	</tr>


	<tr>
		<th  title="<spring:message code="Concept.conceptClass.help"/>">
			<spring:message code="Concept.conceptClass" />
		</th>
		<td valign="top" colspan="${fn:length(locales)}">
			${concept.conceptClass.name}
		</td>
	</tr>
	<c:if test="${concept.set}">
		<tr id="setOptions">
			<th valign="top"><spring:message code="Concept.conceptSets"/></th>
			<td valign="top" colspan="${fn:length(locales)}">
				<c:if test="${fn:length(conceptSets) == 0}"><spring:message code="Concept.conceptSets.empty"/></c:if>
				<c:forEach items="${conceptSets}" var="set">
					<a href="concept.htm?conceptId=${set.value[0]}">${set.value[1]} (${set.value[0]})</a><br/>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	<tr>
		<th title="<spring:message code="Concept.datatype.help"/>">
			<spring:message code="Concept.datatype" />
		</th>
		<td valign="top" colspan="${fn:length(locales)}">
			${concept.datatype.name}
		</td>
	</tr>
	<c:if test="${concept.datatype != null && concept.datatype.name == 'Coded'}">
		<tr>
			<th valign="top"><spring:message code="Concept.answers"/></th>
			<td colspan="${fn:length(locales)}">
				<c:forEach items="${conceptAnswers}" var="answer">
					<a href="concept.htm?conceptId=${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))}">${answer.value} (${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))})</a><br/>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	<c:if test="${concept.numeric}">
		<tr>
			<th valign="top"><spring:message code="ConceptNumeric.name"/></th>
			<td colspan="${fn:length(locales)}">
				<table border="0">
					<tr>
						<th></th>
						<th><spring:message code="ConceptNumeric.low"/></th>
						<th><spring:message code="ConceptNumeric.high"/></th>
					</tr>
					<tr>
						<th valign="middle"><spring:message code="ConceptNumeric.absolute"/></th>
						<td valign="middle">${concept.lowAbsolute}</td>
						<td valign="middle">${concept.hiAbsolute}</td>
					</tr>
					<tr>
						<th valign="middle"><spring:message code="ConceptNumeric.critical"/></th>
						<td valign="middle">${concept.lowCritical}</td>
						<td valign="middle">${concept.hiCritical}</td>
					</tr>
					<tr>
						<th valign="middle"><spring:message code="ConceptNumeric.normal"/></th>
						<td valign="middle">${concept.lowNormal}</td>
						<td valign="middle">${concept.hiNormal}</td>
					</tr>
					<tr>
						<td></td>
						<td colspan="2"><small><em>(<spring:message code="ConceptNumeric.inclusive"/>)</em></small>
						</td>
					</tr>
					<tr>
						<th><spring:message code="ConceptNumeric.units"/></th>
						<td colspan="2">${concept.units}</td>
					</tr>
					<tr>
						<th><spring:message code="ConceptNumeric.precise"/></th>
						<td colspan="2">
							<spring:bind path="concept.precise">
								<c:if test="${status.value}">Yes</c:if>
								<c:if test="${!status.value}">No</c:if>
							</spring:bind>
						</td>
					</tr>
				</table>
			</td>
	</c:if>
	<tr>
		<th><spring:message code="Concept.version" /></th>
		<td colspan="${fn:length(locales)}">
			<spring:bind path="concept.version">
				${status.value}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="general.retired" /></th>
		<td colspan="${fn:length(locales)}">
			<spring:bind path="concept.retired">
				${status.value}
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(concept.creator == null)}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td colspan="${fn:length(locales)}">
				${concept.creator.firstName} ${concept.creator.lastName} -
				<openmrs:formatDate date="${concept.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(concept.changedBy == null)}">
		<tr>
			<th><spring:message code="general.changedBy" /></th>
			<td colspan="${fn:length(locales)}">
				${concept.changedBy.firstName} ${concept.changedBy.lastName} -
				<openmrs:formatDate date="${concept.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
		<th valign="top">Resources</th>
		<td colspan="${fn:length(locales)}">
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

<script type="text/javascript">
	document.getElementById("searchPhrase").focus();
	selectTab(document.getElementById("${locale}Tab"));
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
