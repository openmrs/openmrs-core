<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:message var="pageTitle" code="Concept.view.titlebar" scope="page" htmlEscape="true" arguments="${command.concept.name}"/>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:message var="pageTitle" code="Concept.view.title" scope="page" arguments="${command.concept.name}"/>
<openmrs:require privilege="View Concepts" otherwise="/login.htm"
	redirect="/dictionary/concept.htm" />

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
	a.tab{
		border-bottom: 1px solid whitesmoke;
		padding-left: 3px;
		padding-right: 3px;
	}
	#footer {
		clear:both;
	}
</style>

<script type="text/javascript">

	function selectTab(tab) {
		var displays = new Array();
		if(tab!=null){
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

<c:choose>
	<c:when test="${command.concept.conceptId != null}">
		<h2><openmrs:message code="Concept.view.header" arguments="${command.concept.name}" /></h2>
	</c:when>
	<c:otherwise>
		<h2><openmrs:message code="Concept.noConceptSelected" /></h2>
	</c:otherwise>
</c:choose>

<openmrs:globalProperty key="concepts.locked" var="conceptsLocked"/>

<c:if test="${command.concept.conceptId != null}">
	<form class="inlineForm" id="jumpForm" action="" method="post">
		<input type="hidden" name="jumpAction" id="jumpAction" value="previous"/>
		<a href="#previousConcept" id="previousConcept" valign="middle" accesskey="," onclick="return jumpToConcept('previous')"><openmrs:message code="general.previous"/></a> |
		<c:if test="${conceptsLocked != 'true'}">
		<openmrs:hasPrivilege privilege="Manage Concepts"><a href="concept.form?conceptId=${command.concept.conceptId}" id="editConcept" accesskey="e" valign="middle"></openmrs:hasPrivilege><openmrs:message code="general.edit"/><openmrs:hasPrivilege privilege="Manage Concepts"></a></openmrs:hasPrivilege> |
		</c:if>
		<a href="conceptStats.form?conceptId=${command.concept.conceptId}" accesskey="s" id="conceptStats" valign="middle"><openmrs:message code="Concept.stats"/></a> |
		<a href="#nextConcept" id="nextConcept" valign="middle" accesskey="." onclick="return jumpToConcept('next')"><openmrs:message code="general.next"/></a> 
	</form>
</c:if>

<c:if test="${conceptsLocked != 'true'}">
	| <openmrs:hasPrivilege privilege="Manage Concepts"><a href="concept.form" id="newConcept" valign="middle"></openmrs:hasPrivilege><openmrs:message code="general.new"/><openmrs:hasPrivilege privilege="Manage Concepts"></a></openmrs:hasPrivilege>
</c:if>

<form class="inlineForm" action="index.htm" method="get">
  &nbsp; &nbsp; &nbsp;
  <input type="text" id="searchPhrase" name="phrase" size="18"> 
  <input type="submit" class="smallButton" value="<openmrs:message code="general.search"/>"/>
</form>

<br/><br/>

<c:if test="${command.concept.conceptId != null}">

	<c:if test="${command.concept.retired}">
	<div class="retiredMessage">
	<div><openmrs:message code="Concept.retiredMessage"/> </div>
	<div>  <c:if test="${command.concept.retiredBy.personName != null}">  <openmrs:message code="general.byPerson"/> <c:out value="${command.concept.retiredBy.personName}" /> </c:if> <c:if test="${command.concept.dateRetired != null}"> <openmrs:message code="general.onDate"/>  <openmrs:formatDate date="${command.concept.dateRetired}" type="long" /> </c:if> <c:if test="${command.concept.retireReason!=''}"> - <c:out value="${command.concept.retireReason}" /> </c:if> </div>
	</div>
	</c:if>
	
	<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptHeader" type="html" />
	
<div id="conceptMainarea">
	<table id="conceptTable" cellpadding="2" cellspacing="0">
		<tr>
			<th title="<openmrs:message code="Concept.id.help"/>"><openmrs:message code="general.id"/></th>
			<td>${command.concept.conceptId}</td>
		</tr>
		<tr>
			<th title="<openmrs:message code="Concept.uiid.help"/>"><openmrs:message code="general.uuid"/></th>
			<td><c:out value="${command.concept.uuid}" /></td>
		</tr>
		<tr>
			<th title="<openmrs:message code="Concept.locale.help"/>"><openmrs:message code="general.locale"/></th>
			<td style="padding-bottom: 0px; padding-left: 0px;">
				<c:forEach items="${command.locales}" var="loc" varStatus="varStatus">
					<a id="${loc}Tab" class="tab ${loc}" href="#select${loc.displayName}" onclick="return selectTab(this)">${loc.displayName}</a><c:if test="${varStatus.last==false}"> | </c:if>
				</c:forEach>
				
			</td>
		</tr>
		<tr class="localeSpecific">
			<th title="<openmrs:message code="Concept.fullySpecified.help"/>">
				<openmrs:message code="Concept.fullySpecifiedName" />
			</th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}"><c:out value="${command.namesByLocale[loc].name}" /></td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th valign="top" title="<openmrs:message code="Concept.synonyms.help"/>"><openmrs:message code="Concept.synonyms" /></th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}">
					<c:forEach var="synonym" items="${command.synonymsByLocale[loc]}" varStatus="varStatus">
						<spring:bind path="command.synonymsByLocale[${loc}][${varStatus.index}]">
							<c:if test="${!status.value.voided}">
								<div>
									<c:out value="${status.value.name}" />
								</div>
							</c:if>
						</spring:bind>
					</c:forEach>
				</td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th valign="top" title="<openmrs:message code="Concept.indexTerms.help"/>"><openmrs:message code="Concept.indexTerms" /></th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}">
					<c:forEach var="indexTerm" items="${command.indexTermsByLocale[loc]}" varStatus="varStatus">
						<spring:bind path="command.indexTermsByLocale[${loc}][${varStatus.index}]">
							<c:if test="${!status.value.voided}">
								<div>
									<c:out value="${status.value.name}" />
								</div>
							</c:if>
						</spring:bind>
					</c:forEach>
				</td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th title="<openmrs:message code="Concept.shortName.help"/>">
				<openmrs:message code="Concept.shortName" />
			</th>
			<c:forEach items="${command.locales}" var="loc">
				<td class="${loc}"><c:out value="${command.shortNamesByLocale[loc].name}" /></td>
			</c:forEach>
		</tr>
		<tr class="localeSpecific">
			<th valign="top" title="<openmrs:message code="Concept.description.help"/>">
				<openmrs:message code="general.description" />
			</th>
			<c:forEach items="${command.locales}" var="loc">
				<td valign="top" class="${loc}">
					<spring:bind path="command.descriptionsByLocale[${loc}].description">
						${status.value}
					</spring:bind>
				</td>
			</c:forEach>
		</tr>
		<tr>
			<th  title="<openmrs:message code="Concept.conceptClass.help"/>">
				<openmrs:message code="Concept.conceptClass" />
			</th>
			<td valign="top">
				${command.concept.conceptClass.name}
			</td>
		</tr>
		<c:if test="${command.concept.set}">
			<tr id="setOptions">
				<th valign="top"><openmrs:message code="Concept.conceptSets"/></th>
				<td valign="top">
					<c:if test="${fn:length(command.concept.setMembers) == 0}"><openmrs:message code="Concept.conceptSets.empty"/></c:if>
					<c:forEach items="${command.concept.setMembers}" var="setMember">
						<a href="concept.htm?conceptId=${setMember.conceptId}" <c:if test="${setMember.retired}">class="retired"</c:if>><openmrs:format concept="${setMember}"/> (${setMember.conceptId})</a><br/>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<tr>
			<th title="<openmrs:message code="Concept.datatype.help"/>">
				<openmrs:message code="Concept.datatype" />
			</th>
			<td valign="top">
				<c:out value="${command.concept.datatype.name}" />
			</td>
		</tr>
		<c:if test="${command.concept.datatype != null && command.concept.datatype.name == 'Coded'}">
			<tr class="localeSpecific">
				<th valign="top"><openmrs:message code="Concept.answers"/></th>
                <c:forEach items="${command.locales}" var="loc">
                    <td class="${loc}">
                        <c:forEach items="${command.conceptAnswersByLocale[loc]}" var="answer">
                            <a href="concept.htm?conceptId=${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))}"><c:out value="${answer.value}" /> (${fn:substring(answer.key, 0, fn:indexOf(answer.key, '^'))})</a><br/>
                        </c:forEach>
                    </td>
                </c:forEach>
			</tr>
		</c:if>
		<c:if test="${command.concept.numeric}">
			<tr>
				<th valign="top"><openmrs:message code="ConceptNumeric.name"/></th>
				<td>
					<table border="0">
						<tr>
							<th valign="middle"><openmrs:message code="ConceptNumeric.absoluteHigh"/></th>
							<td valign="middle"><c:out value="${command.concept.hiAbsolute}" /></td>
						</tr>
						<tr>
							<th valign="middle"><openmrs:message code="ConceptNumeric.criticalHigh"/></th>
							<td valign="middle"><c:out value="${command.concept.hiCritical}" /></td>
						</tr>
						<tr>
							<th valign="middle"><openmrs:message code="ConceptNumeric.normalHigh"/></th>
							<td valign="middle"><c:out value="${command.concept.hiNormal}" /></td>
						</tr>
						<tr>
							<th valign="middle"><openmrs:message code="ConceptNumeric.normalLow"/></th>
							<td valign="middle"><c:out value="${command.concept.lowNormal}" /></td>
						</tr>
						<tr>
							<th valign="middle"><openmrs:message code="ConceptNumeric.criticalLow"/></th>
							<td valign="middle"><c:out value="${command.concept.lowCritical}" /></td>
						</tr>
						<tr>
							<th valign="middle"><openmrs:message code="ConceptNumeric.absoluteLow"/></th>
							<td valign="middle"><c:out value="${command.concept.lowAbsolute}" /></td>
						</tr>
						<tr>
							<td></td>
							<td colspan="2"><small><em>(<openmrs:message code="ConceptNumeric.inclusive"/>)</em></small>
							</td>
						</tr>
						<tr>
							<th><openmrs:message code="ConceptNumeric.units"/></th>
							<td colspan="2"><c:out value="${command.concept.units}" /></td>
						</tr>
						<tr>
							<th><openmrs:message code="ConceptNumeric.allowDecimal"/></th>
							<td colspan="2">
								<spring:bind path="command.concept.precise">
									<c:if test="${status.value}">Yes</c:if>
									<c:if test="${!status.value}">No</c:if>
								</spring:bind>
							</td>
						</tr>
						<tr>
							<c:if test="${command.concept.precise}">
								<th><openmrs:message code="ConceptNumeric.displayPrecision"/></th>
								<td colspan="2">
									<spring:bind path="command.concept.displayPrecision">
										<c:out value="${status.value}" />
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>
								</td>
							</c:if>
						</tr>
					</table>
				</td>
			</tr>
		</c:if>
	 	<tr id="conceptMapRow">
			<th valign="top" style="padding-top: 8px" title="<openmrs:message code="Concept.mappings.help"/>">
				<openmrs:message code="Concept.mappings"/>
			</th>
			<td>
				<table cellpadding="5" cellspacing="3" align="left" class="lightBorderBox">
				<tr id="conceptMappingsHeadersRow" <c:if test="${fn:length(command.conceptMappings) == 0}">style="display:none"</c:if>>
					<th style="text-align: center"><openmrs:message code="Concept.mappings.relationship"/></th>
					<th style="text-align: center"><openmrs:message code="ConceptReferenceTerm.source"/></th>
					<th style="text-align: center"><openmrs:message code="ConceptReferenceTerm.code"/></th>
					<th style="text-align: center"><openmrs:message code="general.name"/></th>
				</tr>
				<c:forEach var="mapping" items="${command.conceptMappings}" varStatus="mapStatus">
					<tr <c:if test="${mapStatus.index % 2 == 0}">class='evenRow'</c:if>>
						<td>${mapping.conceptMapType.name}</td>
						<td>${mapping.conceptReferenceTerm.conceptSource.name}</td>
						<td>${mapping.conceptReferenceTerm.code}</td>
						<td>${mapping.conceptReferenceTerm.name}</td>
					</tr>
				</c:forEach>
				
				</table>
			</td>
		</tr>
		
        <c:if test="${command.concept.complex}">
            <tr>
                <th valign="top"><openmrs:message code="ConceptComplex.handler"/></th>
                <td valign="middle">${command.concept.handler}</td>
            </tr>
        </c:if>
		<tr>
			<th title="<openmrs:message code="Concept.version.help"/>"><openmrs:message code="Concept.version" /></th>
			<td>
				<spring:bind path="command.concept.version">
					<c:out value="${status.value}" />
				</spring:bind>
			</td>
		</tr>
		
		<c:if test="${!(command.concept.creator == null)}">
			<tr>
				<th><openmrs:message code="general.createdBy" /></th>
				<td>
					<c:out value="${command.concept.creator.personName}" /> -
					<openmrs:formatDate date="${command.concept.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<c:if test="${!(command.concept.changedBy == null)}">
			<tr>
				<th><openmrs:message code="general.changedBy" /></th>
				<td>
					<c:out value="${command.concept.changedBy.personName}" /> -
					<openmrs:formatDate date="${command.concept.dateChanged}" type="long" />
				</td>
			</tr>
		</c:if>
		
		<tr><td colspan="2"><br/></td></tr>
		
		<openmrs:hasPrivilege privilege="Edit Concepts">
			<c:if test="${ not empty command.conceptDrugList and fn:length(command.conceptDrugList) > 0}">
				<tr>
					<td colspan="2">
						<openmrs:message code="Concept.drugFormulations" />:<br/>
						<ul>
							<c:forEach var="drug" items="${command.conceptDrugList}">
								<c:choose>
									<c:when test="${not empty drug.dosageForm}">
										<li class="<c:if test="${drug.retired}">retired </c:if>"><c:out value="${drug.name}" /> <c:out value="${drug.strength}" /> <c:out value="${drug.dosageForm.name}" /></li>
									</c:when>
									<c:otherwise>
										<li class="<c:if test="${drug.retired}">retired </c:if>"><c:out value="${drug.name}" /> <c:out value="${drug.strength}" /></li>
									</c:otherwise>
								</c:choose>
							</c:forEach>
							<li>
								<a href="${pageContext.request.contextPath}/admin/concepts/conceptDrug.list?conceptId=${command.concept.conceptId}"><openmrs:message code="Concept.manageDrugFormulary" /></a><br/>
							</li>
						</ul> 
					</td>
				</tr>
			</c:if>
		</openmrs:hasPrivilege>
		
	</table>
	
	<openmrs:extensionPoint pointId="org.openmrs.dictionary.conceptFooter" type="html" />
</c:if>

</div>

<div id="conceptSidebar">

<%@ include file="/WEB-INF/view/dictionary/conceptSidebar.jsp"%>

</div>



<script type="text/javascript">
	document.getElementById("searchPhrase").focus();
	selectTab(document.getElementById("${command.locales[0]}Tab"));
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>
