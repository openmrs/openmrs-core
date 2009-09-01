<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/concept.htm" />

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
	
	function jumpToConcept(which) {
		var action = document.getElementById('jumpAction');
		action.value = which;
		var jumpForm = document.getElementById('jumpForm');
		jumpForm.submit();
		return false;
	}
		

</script>

<c:choose>
	<c:when test="${command.concept.conceptId != null}">
		<h2><spring:message code="Concept.view.title" arguments="${command.concept.name}" /></h2>
	</c:when>
	<c:otherwise>
		<h2><spring:message code="Concept.noConceptSelected" /></h2>
	</c:otherwise>
</c:choose>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>

<c:if test="${command.concept.conceptId != null}">
	<form class="inlineForm" id="jumpForm" action="" method="post">
		<input type="hidden" name="jumpAction" id="jumpAction" value="previous"/>
		<a href="#previousConcept" id="previousConcept" valign="middle" accesskey="," onclick="return jumpToConcept('previous')"><spring:message code="general.previous"/></a> |
		<c:if test="${conceptsLocked != 'true'}">
		<openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form?conceptId=${command.concept.conceptId}" id="editConcept" accesskey="e" valign="middle"></openmrs:hasPrivilege><spring:message code="general.edit"/><openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege> |
		</c:if>
		<a href="conceptStats.form?conceptId=${command.concept.conceptId}" accesskey="s" id="conceptStats" valign="middle"><spring:message code="Concept.stats"/></a> |
		<a href="#nextConcept" id="nextConcept" valign="middle" accesskey="." onclick="return jumpToConcept('next')"><spring:message code="general.next"/></a> 
	</form>
</c:if>

<c:if test="${conceptsLocked != 'true'}">
	| <openmrs:hasPrivilege privilege="Edit Concepts"><a href="concept.form" id="newConcept" valign="middle"></openmrs:hasPrivilege><spring:message code="general.new"/><openmrs:hasPrivilege privilege="Edit Concepts"></a></openmrs:hasPrivilege>
</c:if>

<form class="inlineForm" action="index.htm" method="get">
  &nbsp; &nbsp; &nbsp;
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.search"/>"/>
</form>

<br/><br/>

<c:if test="${command.concept.conceptId != null}">

	<c:if test="${command.concept.retired}">
		<div class="retiredMessage"><div><spring:message code="Concept.retiredMessage"/></div></div>
	</c:if>
	
	<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptHeader" type="html" />
	
	<table id="conceptTable" cellpadding="2" cellspacing="0">
		<tr>
			<th><spring:message code="general.id"/></th>
			<td>${command.concept.conceptId}</td>
		</tr>
		
		<tr>
			<th><spring:message code="general.locale"/></th>
			<td style="padding-bottom: 0px; padding-left: 0px;">
				<c:forEach items="${command.locales}" var="loc" varStatus="varStatus">
					<a id="${loc}Tab" class="tab ${loc}" href="#select${loc.displayName}" onclick="return selectTab(this)">${loc.displayName}</a><c:if test="${varStatus.last==false}"> | </c:if>
				</c:forEach>
				
			</td>
		</tr>
		<tr class="localeSpecific">
			<th title="<spring:message code="Concept.name.help"/>">
				<spring:message code="general.name" />
			</th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}">${command.namesByLocale[loc].name}</td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th title="<spring:message code="Concept.shortName.help"/>">
				<spring:message code="Concept.shortName" />
			</th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}">${command.shortNamesByLocale[loc].name}</td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th valign="top" title="<spring:message code="Concept.description.help"/>">
				<spring:message code="general.description" />
			</th>
			<c:forEach items="${command.locales}" var="loc">
				<td valign="top" class="${loc}">
					<spring:bind path="command.descriptionsByLocale[${loc}].description">
						${status.value}
					</spring:bind>
				</td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th valign="top"><spring:message code="Concept.synonyms" /></th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}">
					<c:forEach var="synonym" items="${command.synonymsByLocale[loc]}" varStatus="varStatus">
						<spring:bind path="command.synonymsByLocale[${loc}][${varStatus.index}]">
							<c:if test="${!status.value.voided}">
								<div>
									${status.value.name}
								</div>
							</c:if>
						</spring:bind>
					</c:forEach>
				</td>
			</c:forEach>
		</tr>
		<tr>
			<th  title="<spring:message code="Concept.conceptClass.help"/>">
				<spring:message code="Concept.conceptClass" />
			</th>
			<td valign="top">
				${command.concept.conceptClass.name}
			</td>
		</tr>
		<c:if test="${command.concept.set}">
			<tr id="setOptions">
				<th valign="top"><spring:message code="Concept.conceptSets"/></th>
				<td valign="top">
					<c:if test="${fn:length(command.concept.conceptSets) == 0}"><spring:message code="Concept.conceptSets.empty"/></c:if>
					<c:forEach items="${command.concept.conceptSets}" var="set">
						<a href="concept.htm?conceptId=${set.concept.conceptId}"><openmrs:format concept="${set.concept}"/> (${set.concept.conceptId})</a><br/>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<tr>
			<th title="<spring:message code="Concept.datatype.help"/>">
				<spring:message code="Concept.datatype" />
			</th>
			<td valign="top">
				${command.concept.datatype.name}
			</td>
		</tr>
		<c:if test="${command.concept.datatype != null && command.concept.datatype.name == 'Coded'}">
			<tr>
				<th valign="top"><spring:message code="Concept.answers"/></th>
				<td>
					<c:forEach items="${command.conceptAnswers}" var="answer">
						<a href="concept.htm?conceptId=${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))}">${answer.value} (${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))})</a><br/>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<c:if test="${command.concept.numeric}">
			<tr>
				<th valign="top"><spring:message code="ConceptNumeric.name"/></th>
				<td>
					<table border="0">
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.absoluteHigh"/></th>
							<td valign="middle">${command.concept.hiAbsolute}</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.criticalHigh"/></th>
							<td valign="middle">${command.concept.hiCritical}</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.normalHigh"/></th>
							<td valign="middle">${command.concept.hiNormal}</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.normalLow"/></th>
							<td valign="middle">${command.concept.lowNormal}</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.criticalLow"/></th>
							<td valign="middle">${command.concept.lowCritical}</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.absoluteLow"/></th>
							<td valign="middle">${command.concept.lowAbsolute}</td>
						</tr>
						<tr>
							<td></td>
							<td colspan="2"><small><em>(<spring:message code="ConceptNumeric.inclusive"/>)</em></small>
							</td>
						</tr>
						<tr>
							<th><spring:message code="ConceptNumeric.units"/></th>
							<td colspan="2">${command.concept.units}</td>
						</tr>
						<tr>
							<th><spring:message code="ConceptNumeric.precise"/></th>
							<td colspan="2">
								<spring:bind path="command.concept.precise">
									<c:if test="${status.value}">Yes</c:if>
									<c:if test="${!status.value}">No</c:if>
								</spring:bind>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</c:if>
	 	<tr id="conceptMapRow">
			<th valign="top" title="<spring:message code="Concept.mappings.help"/>">
				<spring:message code="Concept.mappings"/>
			</th>
			<td>
				<c:forEach var="mapping" items="${command.mappings}" varStatus="mapStatus">
					${mapping.source.name}: ${mapping.sourceCode} <br/>
				</c:forEach>
			</td>
		</tr>
		
        <c:if test="${command.concept.complex}">
            <tr>
                <th valign="top"><spring:message code="ConceptComplex.handler"/></th>
                <td valign="middle">${command.concept.handler}</td>
            </tr>
        </c:if>
		<tr>
			<th><spring:message code="Concept.version" /></th>
			<td>
				<spring:bind path="command.concept.version">
					${status.value}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th><spring:message code="general.retired" /></th>
			<td>
				<spring:bind path="command.concept.retired">
					${status.value}
				</spring:bind>
			</td>
		</tr>
		<c:if test="${!(command.concept.creator == null)}">
			<tr>
				<th><spring:message code="general.createdBy" /></th>
				<td>
					${command.concept.creator.personName} -
					<openmrs:formatDate date="${command.concept.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<c:if test="${!(command.concept.changedBy == null)}">
			<tr>
				<th><spring:message code="general.changedBy" /></th>
				<td>
					${command.concept.changedBy.personName} -
					<openmrs:formatDate date="${command.concept.dateChanged}" type="long" />
				</td>
			</tr>
		</c:if>
		
		<tr><td colspan="2"><br/></td></tr>
		
		<c:if test="${fn:length(command.questionsAnswered) > 0}">
			<tr>
				<th valign="top"><spring:message code="dictionary.questionsAnswered" /></th>
				<td>
					<c:forEach items="${command.questionsAnswered}" var="question">
						<a href="concept.htm?conceptId=${question.conceptId}"><openmrs:format concept="${question}" /></a><br/>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		
		<c:if test="${fn:length(command.containedInSets) > 0}">
			<tr>
				<th valign="top"><spring:message code="dictionary.containedInSets" /></th>
				<td>
					<c:forEach items="${command.containedInSets}" var="set">
						<a href="concept.htm?conceptId=${set.conceptSet.conceptId}"><openmrs:format concept="${set.conceptSet}" /></a><br/>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		
		<c:if test="${fn:length(command.formsInUse) > 0}">
			<tr>
				<th valign="top"><spring:message code="dictionary.forms" /></th>
				<td>
					<c:forEach items="${command.formsInUse}" var="form">
						<a href="${pageContext.request.contextPath}/admin/forms/formSchemaDesign.form?formId=${form.formId}">${form.name}</a><br/>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		
		<tr><td colspan="2"><br/></td></tr>
		
		<tr>	
			<td valign="top">
				<b><spring:message code="Concept.resources" /></b>
			</td>
			<td>
				<a href="index.htm?phrase=<openmrs:format concept="${command.concept}" />"
				       target="_similar_terms" onclick="addName(this)">Similar Concepts</a><br/>
				<a href="http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=<openmrs:format concept="${command.concept}" />"
				       target="_blank" onclick="addName(this)">Merriam Webster&reg;</a><br/>
				<a href="http://www.google.com/search?q=<openmrs:format concept="${command.concept}" />"
				       target="_blank" onclick="addName(this)">Google&trade;</a><br/>
				<a href="http://www.utdol.com/application/vocab.asp?submit=Go&search=<openmrs:format concept="${command.concept}" />"
				       target="_blank" onclick="addName(this)">UpToDate&reg;</a><br/>
				<a href="http://dictionary.reference.com/search?submit=Go&q=<openmrs:format concept="${command.concept}" />"
				       target="_blank" onclick="addName(this)">Dictionary.com&reg;</a><br/>
				<a href="http://search.atomz.com/search/?sp-a=sp1001878c&sp-q=<openmrs:format concept="${command.concept}" />"
				       target="_blank" onclick="addName(this)">Lab Tests Online</a><br/>
				<a href="http://en.wikipedia.org/wiki/<openmrs:format concept="${command.concept}" />"
				       target="_blank"><spring:message code="Concept.wikipedia" /></a>
			</td>
		</tr>
	</table>
	
	<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFooter" type="html" />
</c:if>

<script type="text/javascript">
	document.getElementById("searchPhrase").focus();
	selectTab(document.getElementById("${command.locales[0]}Tab"));
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
