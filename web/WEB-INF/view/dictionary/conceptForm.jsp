<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/dictionary/concept.form" />

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<script type="text/javascript" src="conceptForm.js"></script>

<script type="text/javascript">
	function addName(anchor) {
		if (anchor.href.lastIndexOf("=") == anchor.href.length - 1)
			anchor.href += $("conceptName_${locale}").value;
	}
	
	// concept name tab functionality
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
	a.tab {
		border-bottom: 1px solid whitesmoke;
		padding-left: 3px;
		padding-right: 3px;
	}
	#newConceptMapping {
		display: none;
	}
</style>

<c:choose>
	<c:when test="${concept.conceptId != null}">
		<h2><spring:message code="Concept.edit.title" arguments="${concept.name}" /></h2>
	</c:when>
	<c:otherwise>
		<h2><spring:message code="Concept.creatingNewConcept" /></h2>
	</c:otherwise>
</c:choose>

<c:if test="${concept.conceptId != null}">
	<form class="inlineForm" id="jumpForm" action="" method="post">
		<input type="hidden" name="jumpAction" id="jumpAction" value="previous"/>
		<a href="#previousConcept" id="previousConcept" valign="middle" accesskey="," onclick="return jumpToConcept('previous')"><spring:message code="general.previous"/></a>
			|
		<a href="concept.htm?conceptId=${concept.conceptId}" id="viewConcept" accesskey="v"><spring:message code="general.view"/></a>
			|
		<a href="conceptStats.form?conceptId=${concept.conceptId}" id="conceptStats" accesskey="s" valign="middle"><spring:message code="Concept.stats"/></a>
			|
		<a href="#nextConcept" id="nextConcept" valign="middle" accesskey="." onclick="return jumpToConcept('next')"><spring:message code="general.next"/></a>
	</form>
</c:if>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>

<c:if test="${conceptsLocked != 'true'}">
	| <a href="concept.form" id="newConcept" valign="middle"><spring:message code="general.new"/></a>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFormHeader" type="html" />

<form class="inlineForm" action="index.htm" method="get">
  &nbsp; &nbsp; 
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.search"/>"/>
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

<c:if test="${concept.conceptId != null}">
	<c:if test="${concept.conceptClass.name == 'Question' && concept.datatype.name == 'N/A'}">
		<div class="highlighted">
			<spring:message code="Concept.checkClassAndDatatype"/>
		</div>
		<br/>
	</c:if>
</c:if>

<div id="newLocaleDialog" style="display:none; border: 1px dashed gray;padding:1em;">
	<spring:message code="Concept.newLocale" />	
	<p>
		<form name="newLocalForm" method="get" action="concept.form">
			<input type="hidden" name="conceptId" value="${concept.conceptId}" />			
			<spring:message code="Concept.newLocale.label"/>:
			<input type="text" name="newLocale" />
			<input type="submit" value="<spring:message code="general.submit"/>" /> |
			<a href="#" onclick="document.getElementById('newLocaleDialog').style.display = 'none';"><spring:message code="general.cancel"/></a>
		</form>
	</p>
</div>

<form method="post" action="">
<c:if test="${newLocaleProposed != null}">				
	<input type="hidden" name="newLocaleAdded" value="${newLocaleProposed}" />
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
			<a id="newLocaleTab" class="tab newLocale" href="#" onclick="document.getElementById('newLocaleDialog').style.display = '';">
				<spring:message code="general.add"/>
			</a>
			
		</td>
	</tr>
	<tr class="localeSpecific">
		<th title="<spring:message code="Concept.name.help"/>">
			<spring:message code="general.name" />
		</th>
		<c:forEach items="${locales}" var="loc">
			<td class="${loc}">
				<spring:bind path="conceptName_${loc}.name">
				<input type="text" name="${status.expression}_${loc}"
					value="${status.value}" id="conceptName_${loc}" class="largeWidth" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
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
				<spring:bind path="conceptShortName_${loc}.name">
					<!-- input class="smallWidth" type="text" name="${status.expression}_${loc}" -->
					<input class="smallWidth" type="text" name="shortName_${loc}"
						value="${status.value}" size="10" />
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind>
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th valign="top" title="<spring:message code="Concept.description.help"/>">
			<spring:message code="general.description" />
		</th>
		<c:forEach items="${locales}" var="loc">
			<td valign="top" class="${loc}">
				<spring:bind path="conceptDescription_${loc}.description">
					<textarea name="${status.expression}_${loc}" rows="4" cols="50">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
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
				<input type="text" class="largeWidth" id="addSyn${loc}" onKeyDown="return synonymKeyPress(this, event, '${loc}');"/>
				<input type="button" class="smallButton" value="<spring:message code="Concept.synonym.add"/>" onClick="addSynonym('${loc}');"/>
				<input type="hidden" name="newSynonyms_${loc}" id="newSynonyms${loc}" value="<c:forEach items="${conceptSynonymsByLocale[loc]}" var="syn">${syn},</c:forEach>" />
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th></th>
		<c:forEach items="${locales}" var="loc">
			<td class="${loc}">
				<table cellpadding="0" cellspacing="0">
					<tr>
						<td>
							<select class="largeWidth" size="5" multiple id="syns${loc}" onkeydown="listKeyPress('syns${loc}', 'newSynonyms${loc}', ',', event);">
								<c:forEach items="${conceptSynonymsByLocale[loc]}" var="syn"><option value="${syn}">${syn}</option></c:forEach>
							</select>
						</td>
						<td valign="top" class="buttons">
							&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('syns${loc}', 'newSynonyms${loc}', ',');" /> <br/>
						</td>
					</tr>
				</table>
			</td>
		</c:forEach>
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
	<tr>
		<th valign="top"><spring:message code="Concept.set"/></th>
		<td>
			<spring:bind path="concept.set">
				<input type="checkbox" name="conceptSet" id="conceptSet" <c:if test="${status.value}">checked="checked"</c:if> onClick="changeSetStatus(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="conceptSetRow">
		<th valign="top"><spring:message code="Concept.conceptSets"/></th>
		<td valign="top">
			<input type="hidden" name="conceptSets" id="conceptSets" size="40" value='<c:forEach items="${conceptSets}" var="set">${set.value[0]} </c:forEach>' />
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select class="largeWidth" size="6" id="conceptSetsNames" multiple onkeyup="listKeyPress('conceptSetsNames', 'conceptSets', ' ', event);">
							<c:forEach items="${conceptSets}" var="set">
								<option value="${set.value[0]}">${set.value[1]} (${set.value[0]})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<span dojoType="ConceptSearch" widgetId="sSearch"></span><span dojoType="OpenmrsPopup" searchWidget="sSearch" searchTitle='<spring:message code="Concept.find"/>' changeButtonValue='<spring:message code="general.add"/>' showConceptIds="true"></span>
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('conceptSetsNames', 'conceptSets', ' ');" style="display: block" />
						<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('conceptSetsNames', 'conceptSets');" style="display: block" />
						<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('conceptSetsNames', 'conceptSets');" style="display: block" />
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
						<select class="largeWidth" size="6" id="answerNames" multiple onKeyUp="listKeyPress('answerNames', 'answerIds', ' ', event)">
							<c:forEach items="${conceptAnswers}" var="answer">
								<option value="${answer.key}">${answer.value} (${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<span dojoType="ConceptSearch" widgetId="aSearch" includeDrugConcepts="true"></span><span dojoType="OpenmrsPopup" searchWidget="aSearch" searchTitle='<spring:message code="Concept.find"/>' changeButtonValue='<spring:message code="general.add"/>' showConceptIds="true" showIfHiding="true"></span>
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('answerNames', 'answerIds', ' ');"/><br/>
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
    <tr id="complexDatatypeRow">
        <th valign="top"><spring:message code="ConceptComplex.handler"/></th>
        <td>
            <select name="handlerSelect"> 
             <option value=""><spring:message code="general.select"/>...</option>
             <c:forEach var="handler" items="${handlers}">
             	<option value="${handler.key}" <c:if test="${concept.complex && handler.key == concept.handler}">selected="selected"</c:if>>
                     ${handler.key}
                 </option>
             </c:forEach>
            </select>
                 
            <!-- All handler key to class mappings
            <table>
             <c:forEach var="h" items="${handlers }">
                <tr name="handlerRow" id="handler_${h.key}">
                    <th >
                        <spring:message code="general.class"/>
                    </th>
                    <td>
                        ${h.value.class.name }
                    </td>
                </tr>
             </c:forEach>
            </table>
            -->
        </td>
    </tr>
	<tr id="conceptMapRow">
		<th valign="top" title="<spring:message code="Concept.mappings.help"/>">
			<spring:message code="Concept.mappings"/>
		</th>
		<td>
			<c:forEach var="mapping" items="${concept.conceptMappings}" varStatus="mapStatus">
				<span id="mapping-${mapStatus.index}">
					<spring:nestedPath path="concept.conceptMappings[${mapStatus.index}]">
						<spring:bind path="sourceCode">
							<input type="text" name="${status.expression}" value="${status.value}" size="10">
						</spring:bind>
						<spring:bind path="source">
							<select name="${status.expression}">
								<openmrs:forEachRecord name="conceptSource">
									<option value="${record.conceptSourceId}" <c:if test="${record.conceptSourceId == status.value}">selected</c:if> >
											${record.name} (${record.hl7Code})
									</option>
								</openmrs:forEachRecord>
							</select>
						</spring:bind>
					</spring:nestedPath>
					<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeMapping(this)" />
					<br/>
				</span>
			</c:forEach>
			<span id="newConceptMapping">
				<input type="text" name="newConceptMappingSourceCode" value="${status.value}" size="10">
				<select name="newConceptMappingSource">
					<openmrs:forEachRecord name="conceptSource">
						<option value="${record.conceptSourceId}">
								${record.name} (${record.hl7Code})
						</option>
					</openmrs:forEachRecord>
				</select>
				<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeMapping(this)" />
				<br/>
			</span>
			<input type="button" value='<spring:message code="Concept.mapping.add"/>' class="smallButton" onClick="addMapping(this)" />
			<br/>
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
				${concept.creator.personName} -
				<openmrs:formatDate date="${concept.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(concept.changedBy == null)}">
		<tr>
			<th><spring:message code="general.changedBy" /></th>
			<td>
				${concept.changedBy.personName} -
				<openmrs:formatDate date="${concept.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<!--
	<cif test="${forms != null}">
		<tr>
			<td>
				<b><spring:message code="Concept.forms" /></b><br />
					<c:forEach items="${formsInUse}" var="form">
						${form} <br/>
					</c:forEach>
				<br/>
			</td>
		</tr>
	</cif>
	-->
	
	<tr><td colspan="2"><br/></td></tr>
	
	<c:if test="${fn:length(questionsAnswered) > 0}">
		<tr>
			<th valign="top"><spring:message code="dictionary.questionsAnswered" /></th>
			<td>
				<c:forEach items="${questionsAnswered}" var="question">
					<a href="concept.htm?conceptId=${question.key}">${question.value}<br/>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	
	<c:if test="${fn:length(containedInSets) > 0}">
		<tr>
			<th valign="top"><spring:message code="dictionary.containedInSets" /></th>
			<td>
				<c:forEach items="${containedInSets}" var="set">
					<a href="concept.htm?conceptId=${set.key}">${set.value}<br/>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	
	<c:if test="${fn:length(formsInUse) > 0}">
		<tr>
			<th valign="top"><spring:message code="dictionary.forms" /></th>
			<td>
				<c:forEach items="${formsInUse}" var="form">
					<a href="${pageContext.request.contextPath}/admin/forms/formSchemaDesign.form?formId=${form.formId}">${form.name}<br/>
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
			<a href="index.htm?phrase=${conceptName.name}"
			       target="_similar_terms" onclick="addName(this)">Similar Concepts</a><br/>
			<a href="http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=${conceptName.name}"
			       target="_blank" onclick="addName(this)">Merriam Webster&reg;</a><br/>
			<a href="http://www.google.com/search?q=${conceptName.name}"
			       target="_blank" onclick="addName(this)">Google&trade;</a><br/>
			<a href="http://www.utdol.com/application/vocab.asp?submit=Go&search=${conceptName.name}"
			       target="_blank" onclick="addName(this)">UpToDate&reg;</a><br/>
			<a href="http://dictionary.reference.com/search?submit=Go&q=${conceptName.name}"
			       target="_blank" onclick="addName(this)">Dictionary.com&reg;</a><br/>
			<a href="http://search.atomz.com/search/?sp-a=sp1001878c&sp-q=${conceptName.name}"
			       target="_blank" onclick="addName(this)">Lab Tests Online</a><br/>
			<a href="http://en.wikipedia.org/wiki/${concept.name}"
			       target="_blank"><spring:message code="Concept.wikipedia" /></a>
		</td>
	</tr>
</table>

<div id="saveDeleteButtons" style="margin-top: 15px">
<c:if test="${conceptsLocked != 'true'}">	
	<input type="submit" name="action" value="<spring:message code="Concept.save"/>" onMouseUp="removeHiddenRows()"/>
	
	<c:if test="${concept.conceptId != null}">
		<openmrs:hasPrivilege privilege="Delete Concepts">
			 &nbsp; &nbsp; &nbsp;
			<input type="submit" name="action" value="<spring:message code="Concept.delete"/>" onclick="return confirm('Are you sure you want to delete this ENTIRE CONCEPT?')"/>
		</openmrs:hasPrivilege>
	</c:if>
</c:if>
</div>

</form>

<script type="text/javascript">
	selectTab(document.getElementById("${locale}Tab"));
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>