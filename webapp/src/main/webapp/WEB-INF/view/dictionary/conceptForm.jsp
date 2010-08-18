<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/dictionary/concept.form" />

<c:choose>
	<c:when test="${command.concept.conceptId != null}">
		<spring:message var="pageTitle" code="Concept.edit.titlebar" scope="page" arguments="${command.concept.name}"/>
	</c:when>
	<c:otherwise>
		<spring:message var="pageTitle" code="Concept.creatingNewConcept.titlebar" scope="page"/>
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />

<openmrs:htmlInclude file="/dictionary/conceptForm.js" />

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
		text-align: right; padding-right: 15px;
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
	.hidden, #newConceptSynonym, #newConceptMapping {
		display: none;
	}
	.checkbox_void{
		margin-left: 80px;
	}
	.help_icon_bottom{
		vertical-align: bottom;
	}
	.help_icon_top{
		vertical-align: top;
	}
	#preferredLabel{
		padding-left: 345px; padding-top:10px;
	}	
</style>

<c:choose>
	<c:when test="${command.concept.conceptId != null}">
		<h2><spring:message code="Concept.edit.title" arguments="${command.concept.name}" /></h2>
	</c:when>
	<c:otherwise>
		<h2><spring:message code="Concept.creatingNewConcept" /></h2>
	</c:otherwise>
</c:choose>

<c:if test="${command.concept.conceptId != null}">
	<form class="inlineForm" id="jumpForm" action="" method="post">
		<input type="hidden" name="jumpAction" id="jumpAction" value="previous"/>
		<a href="#previousConcept" id="previousConcept" valign="middle" accesskey="," onclick="return jumpToConcept('previous')"><spring:message code="general.previous"/></a>
			|
		<a href="concept.htm?conceptId=${command.concept.conceptId}" id="viewConcept" accesskey="v"><spring:message code="general.view"/></a>
			|
		<a href="conceptStats.form?conceptId=${command.concept.conceptId}" id="conceptStats" accesskey="s" valign="middle"><spring:message code="Concept.stats"/></a>
			|
		<a href="#nextConcept" id="nextConcept" valign="middle" accesskey="." onclick="return jumpToConcept('next')"><spring:message code="general.next"/></a>
	</form>
</c:if>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>

<c:if test="${conceptsLocked != 'true'}">
	| <a href="concept.form" id="newConcept" valign="middle"><spring:message code="general.new"/></a>
</c:if>

<form class="inlineForm" action="index.htm" method="get">
  &nbsp; &nbsp; 
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<spring:message code="general.search"/>"/>
</form>

<br/><br/>
<c:if test="${command.concept.retired}">
	<div class="retiredMessage"><div><spring:message code="Concept.retiredMessage"/></div></div>
</c:if>

<spring:hasBindErrors name="command">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>

<c:if test="${command.concept.conceptId != null}">
	<c:if test="${command.concept.conceptClass.name == 'Question' && command.concept.datatype.name == 'N/A'}">
		<div class="highlighted">
			<spring:message code="Concept.checkClassAndDatatype"/>
		</div>
		<br/>
	</c:if>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFormHeader" type="html" />

<form method="post" action="">

<table id="conceptTable" cellpadding="2" cellspacing="0">

	<tr>
		<th title="<spring:message code="Concept.id.help"/>"><spring:message code="general.id"/></th>
		<td colspan="${fn:length(locales)}">${command.concept.conceptId}</td>
	</tr>

	<tr>
		<th title="<spring:message code="Concept.locale.help"/>"><spring:message code="general.locale"/></th>
		<td style="padding-bottom: 0px; padding-left: 0px;">
			<c:forEach items="${command.locales}" var="loc" varStatus="varStatus">
				<a id="${loc}Tab" class="tab ${loc}" href="#select${loc.displayName}" onclick="return selectTab(this)">${loc.displayName}</a><c:if test="${varStatus.last==false}"> | </c:if>
			</c:forEach>
		</td>
	</tr>
	<tr class="localeSpecific">
		<td style="visibility: hidden">&nbsp;</td>
		<td id="preferredLabel">
			<spring:message code="Concept.name.localePreferred" /> 
			<img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.name.localePreferred.help"/>" />
		</td>
	</tr>
	<tr class="localeSpecific">
		<th valign="bottom">
			<spring:message code="Concept.fullySpecifiedName" /> 
			<img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.fullySpecified.help"/>"/>
		</th>
		<c:forEach items="${command.locales}" var="loc">
			<td class="${loc}">				
				<spring:bind path="command.namesByLocale[${loc}].name">
				<input type="text" name="${status.expression}" value="${status.value}" id="${status.expression}" class="largeWidth"  onchange="setRadioValue(this, 'fullySpecPreferred[${loc}]')" />
				</spring:bind>
				<spring:bind path="command.preferredNamesByLocale[${loc}]" ignoreNestedPath="true">			
				<input id="fullySpecPreferred[${loc}]" type="radio" name="${status.expression}" value="${command.namesByLocale[loc].name}" <c:if test="${command.namesByLocale[loc].localePreferred}">checked=checked</c:if>  />
				</spring:bind>
				<spring:bind path="command.namesByLocale[${loc}].name">
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>					
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th valign="top">
			<spring:message code="Concept.synonyms" /> <img class="help_icon_bottom" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.synonyms.help"/>"/>
		</th>
		<c:forEach items="${command.locales}" var="loc">
			<td class="${loc}">
				<c:forEach var="synonym" items="${command.synonymsByLocale[loc]}" varStatus="varStatus">
					<spring:nestedPath path="command.synonymsByLocale[${loc}][${varStatus.index}]">
						<input type="hidden" name="_synonymsByLocale[${loc}][${varStatus.index}].name" value="" />
						<div>							
							<spring:bind path="name">																
							<input type="text" name="${status.expression}" value="${status.value}" class="largeWidth" 
							<c:if test="${conceptNameHasObsMap[synonym.conceptNameId] ==  null}">onchange="setRadioValue(this, 'possiblePrefName_[${loc}][${varStatus.index}]')" </c:if> 
							<c:if test="${conceptNameHasObsMap[synonym.conceptNameId] !=  null}">readonly="readonly"</c:if> />							
							</spring:bind>
							<spring:bind path="command.preferredNamesByLocale[${loc}]" ignoreNestedPath="true">							
							<input id="possiblePrefName_[${loc}][${varStatus.index}]" type="radio" name="${status.expression}" value="${synonym.name}" <c:if test="${synonym.localePreferred}">checked=checked</c:if> />							
							</spring:bind>
							<!-- If this was a new synonym that failed validation, it can be removed without a void reason  -->
							<c:if test="${command.synonymsByLocale[loc][varStatus.index].conceptNameId == null}">
							<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)"/>							
							</c:if>	
							<c:if test="${command.synonymsByLocale[loc][varStatus.index].conceptNameId != null}">
							<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="voidName('synonymsByLocale[${loc}][${varStatus.index}].isVoided', 'synonymsByLocale[${loc}][${varStatus.index}].voidedInfo')"/>
							</c:if>
								<span id="synonymsByLocale[${loc}][${varStatus.index}].voidedInfo" <c:if test="${!command.synonymsByLocale[loc][varStatus.index].voided}">style="display: none"</c:if> >
									<spring:message code="general.reason"/>?
									<spring:bind path="voidReason">
									<input id="synonymsByLocale[${loc}][${varStatus.index}].voidReasonText" type="text" value="${status.value}" size="mediumWidth" name="${status.expression}" />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>										
									<input type="button" value='<spring:message code="general.cancel"/>' class="smallButton" 
											onClick="cancelNameRemoval('synonymsByLocale[${loc}][${varStatus.index}].isVoided', 'synonymsByLocale[${loc}][${varStatus.index}].voidedInfo', 'synonymsByLocale[${loc}][${varStatus.index}].voidReasonText')" />
									<spring:bind path="voidedBy">
									<c:if test="${status.value != null}">
										<spring:message code="general.voidedBy" />:
										${status.value.personName} -
										<openmrs:formatDate date="${command.synonymsByLocale[loc][varStatus.index].dateVoided}" type="long" />
										</c:if>
									</spring:bind>
								</span>
							<spring:bind path="name">
							<c:if test="${status.errorMessage != ''}">
							<span class="error">${status.errorMessage}</span>
							</c:if>
							</spring:bind>														
							<spring:bind path="voided">
								<input type="hidden" name="_${status.expression}" value="" />
								<input id="synonymsByLocale[${loc}][${varStatus.index}].isVoided" type="hidden" name="${status.expression}" value="${status.value}" />
							</spring:bind>							
						</div>
					</spring:nestedPath>
				</c:forEach>
				<div id="newConceptSynonym-${loc}" style="display: none">
					<input type="text" name="[x].name" value="" class="largeWidth" onchange="setCloneRadioValue(this)">
					<input type="radio" name="preferredNamesByLocale[${loc}]" value="" />
					<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)" />
				</div>
				<input type="button" value='<spring:message code="Concept.synonym.add"/>' class="smallButton" 
				       onClick="cloneElement('newConceptSynonym-${loc}', ${fn:length(command.synonymsByLocale[loc])}, 'synonymsByLocale[${loc}]')" />				
			</td>			
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th valign="top">
			<spring:message code="Concept.indexTerms" /> <img class="help_icon_bottom" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.indexTerms.help"/>"/>
		</th>
		<c:forEach items="${command.locales}" var="loc">
			<td class="${loc}">
				<c:forEach var="indexTerm" items="${command.indexTermsByLocale[loc]}" varStatus="varStatus">
					<spring:nestedPath path="command.indexTermsByLocale[${loc}][${varStatus.index}]">
						<input type="hidden" name="_indexTermsByLocale[${loc}][${varStatus.index}].name" value="" />
						<div>							
							<spring:bind path="name">
							<input type="text" name="${status.expression}" value="${status.value}" class="largeWidth" 
							<c:if test="${conceptNameHasObsMap[indexTerm.conceptNameId] !=  null}">readonly="readonly"</c:if> />								
							</spring:bind>
							<!-- A place holder radio button just to maintain alignment of the voided checkbox on the page  -->
							<input type="radio" name="placeHolder" value="" style="visibility: hidden" />
							<!-- If this was a new index term that failed validation, it can be removed without a void reason  -->
							<c:if test="${command.indexTermsByLocale[loc][varStatus.index].conceptNameId == null}">
							<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)"/>							
							</c:if>
							<c:if test="${command.indexTermsByLocale[loc][varStatus.index].conceptNameId != null}">
							<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="voidName('indexTermsByLocale[${loc}][${varStatus.index}].isVoided', 'indexTermsByLocale[${loc}][${varStatus.index}].voidedInfo')"/>							
							</c:if>
							<span id="indexTermsByLocale[${loc}][${varStatus.index}].voidedInfo" <c:if test="${!command.indexTermsByLocale[loc][varStatus.index].voided}">style="display: none"</c:if> >
								<spring:message code="general.reason"/>?
								<spring:bind path="voidReason">
									<input  id="indexTermsByLocale[${loc}][${varStatus.index}].voidReasonText" type="text" value="${status.value}" size="mediumWidth" name="${status.expression}" />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
								<input type="button" value='<spring:message code="general.cancel"/>' class="smallButton" 
										onClick="cancelNameRemoval('indexTermsByLocale[${loc}][${varStatus.index}].isVoided', 'indexTermsByLocale[${loc}][${varStatus.index}].voidedInfo', 'indexTermsByLocale[${loc}][${varStatus.index}].voidReasonText')" />
								<spring:bind path="voidedBy">
								<c:if test="${status.value != null}">
									<spring:message code="general.voidedBy" />:
									${status.value.personName} -
									<openmrs:formatDate date="${command.indexTermsByLocale[loc][varStatus.index].dateVoided}" type="long" />
								</c:if>
								</spring:bind>
							</span>
							<spring:bind path="name">
							<c:if test="${status.errorMessage != ''}">
								<span class="error">${status.errorMessage}</span>
							</c:if>
							</spring:bind>
							<spring:bind path="voided">
								<input type="hidden" name="_${status.expression}" value="" />
								<input id="indexTermsByLocale[${loc}][${varStatus.index}].isVoided" type="hidden" name="${status.expression}" value="${status.value}" />
							</spring:bind>							
						</div>
					</spring:nestedPath>
				</c:forEach>
				<div id="newConceptIndexTerm-${loc}" style="display: none">
					<input type="text" name="[x].name" value="" class="largeWidth" />
					<input type="radio" name="placeHolder" value="" style="visibility: hidden" />
					<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)" />
				</div>
				<input type="button" value='<spring:message code="Concept.indexTerm.add"/>' class="smallButton" onClick="cloneElement('newConceptIndexTerm-${loc}', ${fn:length(command.indexTermsByLocale[loc])}, 'indexTermsByLocale[${loc}]')" />				
			</td>
			
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th>
			<spring:message code="Concept.shortName" /> <img class="help_icon_bottom" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.shortName.help"/>"/>
		</th>
		<c:forEach items="${command.locales}" var="loc">
			<td class="${loc}">
				<spring:bind path="command.shortNamesByLocale[${loc}].name">
					<input class="smallWidth" type="text" name="${status.expression}" value="${status.value}" />					
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</c:forEach>
	</tr>
	<tr class="localeSpecific">
		<th>
			<spring:message code="general.description" /> <img class="help_icon_top" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.description.help"/>"/>
		</th>
		<c:forEach items="${command.locales}" var="loc">
			<td valign="top" class="${loc}">
				<spring:bind path="command.descriptionsByLocale[${loc}].description">
					<textarea name="${status.expression}" rows="4" cols="50">${status.value}</textarea>					
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</c:forEach>
	</tr>		
	<tr>
		<th>
 			<spring:message code="Concept.conceptClass" /> <img class="help_icon_bottom" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.conceptClass.help"/>"/>
		</th>
		<td valign="top">
			<spring:bind path="command.concept.conceptClass">
				<select class="smallWidth" name="${status.expression}" id="conceptClass" onChange="changeClass(this);">
					<c:forEach items="${classes}" var="cc">
						<option value="${cc.conceptClassId}"
							<c:if test="${cc.conceptClassId == status.value}">selected="selected"</c:if>>${cc.name}
						</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top">
			<spring:message code="Concept.set"/> <img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.isSet.help"/>"/>
		</th>
		<td>
			<spring:bind path="command.concept.set">
				<input type="hidden" name="_${status.expression}" value=""/>
				<input type="checkbox" name="${status.expression}" id="conceptSet" <c:if test="${status.value}">checked="checked"</c:if> onClick="changeSetStatus(this)" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="conceptSetRow">
		<th valign="top"><spring:message code="Concept.conceptSets"/></th>
		<td valign="top">
			<spring:bind path="command.concept.conceptSets">
				<input type="hidden" name="${status.expression}" id="conceptSets" size="40" value='<c:forEach items="${command.concept.conceptSets}" var="set">${set.concept.conceptId} </c:forEach>' />
			</spring:bind>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select class="largeWidth" size="6" id="conceptSetsNames" multiple="multiple" onkeyup="listKeyPress('conceptSetsNames', 'conceptSets', ' ', event);">
							<c:forEach items="${command.concept.conceptSets}" var="set">
								<option value="${set.concept.conceptId}"><openmrs:format concept="${set.concept}"/> (${set.concept.conceptId})</option>
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
		<th>
			<spring:message code="Concept.datatype" /> <img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.datatype.help"/>"/>
		</th>
		<td valign="top">
			<spring:bind path="command.concept.datatype">
				<select class="smallWidth" name="${status.expression}" id="datatype" onChange="changeDatatype(this);"
										   <c:if test="${dataTypeReadOnly == true}">disabled="disabled"</c:if>>
					<c:forEach items="${datatypes}" var="cd">
						<option value="${cd.conceptDatatypeId}"
							<c:if test="${cd.conceptDatatypeId == status.value}">selected="selected"</c:if>>${cd.name}</option>
					</c:forEach>
				</select>
				<c:if test="${dataTypeReadOnly == true && isBoolean != null && isBoolean == true}">					
					<input type="button" value="<spring:message code="Concept.boolean.add.answer"/>" 
						   onclick="addAnswerToBooleanConcept('<spring:message code="Concept.boolean.confirm.add.answer"/>', '${command.concept.conceptId}')" 
						   title="<spring:message code="Concept.boolean.change.tooltip"/>"/>
					<spring:message code="Concept.boolean.warning.irreversible"/>
					<span id="addAnswerError" class="error" style="display:none"></span>
				</c:if>				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<c:if test="${dataTypeReadOnly == true && isBoolean == null}">(<spring:message code="Concept.datatype.readonly"/>)</c:if>
		</td>
	</tr>
	<tr id="codedDatatypeRow">
		<th valign="top"><spring:message code="Concept.answers"/></th>
		<td>
			<spring:bind path="command.concept.answers">
				<input type="hidden" name="${status.expression}" id="answerIds" size="40" value='<c:forEach items="${command.conceptAnswers}" var="answer">${answer.key} </c:forEach>' />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<table cellspacing="0" cellpadding="0">
				<tr>
					<td valign="top">
						<select class="largeWidth" size="6" id="answerNames" multiple="multiple" onKeyUp="listKeyPress('answerNames', 'answerIds', ' ', event)">
							<c:forEach items="${command.conceptAnswers}" var="answer">
								<option value="${answer.key}">${answer.value} (${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						<span dojoType="ConceptSearch" widgetId="aSearch" includeDrugConcepts="true"></span><span dojoType="OpenmrsPopup" searchWidget="aSearch" searchTitle='<spring:message code="Concept.find"/>' changeButtonValue='<spring:message code="general.add"/>' showConceptIds="true" showIfHiding="true"></span>
						<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('answerNames', 'answerIds', ' ');"/><br/>
						<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('answerNames', 'answerIds');" style="display: block" />
						<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('answerNames', 'answerIds');" style="display: block" />
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
					<th valign="middle"><spring:message code="ConceptNumeric.absoluteHigh"/></th>
					<td valign="middle">
						<spring:bind path="command.hiAbsolute">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.criticalHigh"/></th>
					<td valign="middle">
						<spring:bind path="command.hiCritical">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.normalHigh"/></th>
					<td valign="middle">
						<spring:bind path="command.hiNormal">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.normalLow"/></th>
					<td valign="middle">
						<spring:bind path="command.lowNormal">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.criticalLow"/></th>
					<td valign="middle">
						<spring:bind path="command.lowCritical">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<th valign="middle"><spring:message code="ConceptNumeric.absoluteLow"/></th>
					<td valign="middle">
						<spring:bind path="command.lowAbsolute">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
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
					<th><spring:message code="ConceptNumeric.units"/></th>
					<td colspan="2">
						<spring:bind path="command.units">
							<input type="text" name="${status.expression}" value="${status.value}" class="mediumWidth" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
				<tr>
					<th><spring:message code="ConceptNumeric.precise"/></th>
					<td colspan="2">
						<spring:bind path="command.precise">
							<input type="hidden" name="_${status.expression}" value=""/>
							<input type="checkbox" name="${status.expression}" <c:if test="${status.value}">checked="checked"</c:if>/>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
					</td>
				</tr>
			</table>
		</td>
	</tr>
    <tr id="complexDatatypeRow">
        <th valign="top"><spring:message code="ConceptComplex.handler"/></th>
        <td>
			<spring:bind path="command.handlerKey">
				<select name="${status.expression}"> 
					<option value=""><spring:message code="general.select"/>...</option>
					<c:forEach var="handler" items="${handlers}">
						<option value="${handler.key}" <c:if test="${handler.key == status.value}">selected="selected"</c:if>>
					        ${handler.key}
					    </option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
                 
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
		<th valign="top">
			<spring:message code="Concept.mappings"/> <img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.mappings.help"/>"/>
		</th>
		<td>
			<c:forEach var="mapping" items="${command.mappings}" varStatus="mapStatus">
				<spring:nestedPath path="command.mappings[${mapStatus.index}]">
					<span id="mapping-${mapStatus.index}">
						<spring:bind path="sourceCode">
							<input type="text" name="${status.expression}" value="${status.value}" class="smallWidth">
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
						<spring:bind path="source">
							<select name="${status.expression}">
								<openmrs:forEachRecord name="conceptSource">
									<option value="${record.conceptSourceId}" <c:if test="${record.conceptSourceId == status.value}">selected</c:if> >
											${record.name} (${record.hl7Code})
									</option>
								</openmrs:forEachRecord>
							</select>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</spring:bind>
						<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)" />
						<br/>
					</span>
					<input type="hidden" name="_mappings[${mapStatus.index}].sourceCode" value="" />
				</spring:nestedPath>
			</c:forEach>
			<span id="newConceptMapping">
				<input type="text" name="[x].sourceCode" value="${status.value}" class="smallWidth">
				<select name="[x].source">
					<openmrs:forEachRecord name="conceptSource">
						<option value="${record.conceptSourceId}">
								${record.name} (${record.hl7Code})
						</option>
					</openmrs:forEachRecord>
				</select>
				<input type="button" value='<spring:message code="general.remove"/>' class="smallButton" onClick="removeParentElement(this)" />
				<br/>
			</span>
			<input type="button" value='<spring:message code="Concept.mapping.add"/>' class="smallButton" onClick="cloneElement('newConceptMapping', ${fn:length(command.mappings)}, 'mappings')" />
			<br/>
		</td>
	</tr>
	<tr>
		<th>
			<spring:message code="Concept.version" /> <img class="help_icon_bottom" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.version.help"/>"/>
		</th>
		<td>
			<spring:bind path="command.concept.version">
				<input class="smallWidth" type="text" name="${status.expression}" value="${status.value}" class="smallWidth" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th>
			<spring:message code="general.retired" /> <img class="help_icon" src="${pageContext.request.contextPath}/images/help.gif" border="0" title="<spring:message code="Concept.retired.help"/>"/>
		</th>
		<td>
			<spring:bind path="command.concept.retired">
				<input type="hidden" name="_${status.expression}" value="">
				<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${command.concept.creator != null}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${command.concept.creator.personName} -
				<openmrs:formatDate date="${command.concept.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${command.concept.changedBy != null}">
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
		<th valign="top">
			<b><spring:message code="Concept.resources" /></b>
		</th>
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

<div id="saveDeleteButtons" style="margin-top: 15px">
<c:if test="${conceptsLocked != 'true'}">	
	<input type="submit" name="action" value="<spring:message code="Concept.save"/>" onMouseUp="removeHiddenRows()"/>
	
	<c:if test="${command.concept.conceptId != null}">
		<openmrs:hasPrivilege privilege="Delete Concepts">
			 &nbsp; &nbsp; &nbsp;
			<input type="submit" name="action" value="<spring:message code="Concept.delete"/>" onclick="return confirm('<spring:message code="Concept.confirmDelete"/>')"/>
		</openmrs:hasPrivilege>
	</c:if>
</c:if>
</div>

</form>

<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFormFooter" type="html" />

<script type="text/javascript">
	selectTab(document.getElementById("${command.locales[0]}Tab"));
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>