<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptProposal.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	var cSearch;
	var popup;
	
	var selectConceptId = function(id) {
		var closure = function(thisObj, method) { return function(obj) { return thisObj[method]({"obj":obj}); }; };
		DWRConceptService.getConcept(id, closure(cSearch, "select"));
		return false;
	}
	
	dojo.addOnLoad( function() {
		cSearch = dojo.widget.manager.getWidgetById("cSearch");
		popup = dojo.widget.manager.getWidgetById("cSelection");
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				if (msg) {
					var concept = msg.objs[0];
					popup.displayNode.innerHTML = concept.name;
					popup.hiddenInputNode.value = concept.conceptId;
				}
			}
		);
		
		<c:if test="${conceptProposal.mappedConcept != null}">
			var conceptId = ${conceptProposal.mappedConcept.conceptId};
			selectConceptId(conceptId);
		</c:if>
		
	})

</script>

<h2><openmrs:message code="ConceptProposal.title"/></h2>

<style>
	th { text-align: left; }
</style>

<form method="post">
<table>
	<c:if test="${conceptProposal.encounter != null}">
		<tr>
			<th valign="top"><openmrs:message code="ConceptProposal.encounter"/></th>
			<td class="sideNote">
				<table>
					<tr>
						<th><openmrs:message code="general.id"/></th>
						<td>${conceptProposal.encounter.encounterId}</td>
					</tr>
					<tr>
						<th><openmrs:message code="Encounter.type"/></th>
						<td>${conceptProposal.encounter.encounterType.name}</td>
					</tr>
					<tr>
						<th><openmrs:message code="Encounter.location"/></th>
						<td>${conceptProposal.encounter.location}</td>
					</tr>
					<tr>
						<th><openmrs:message code="Encounter.provider"/></th>
						<td><c:out value="${conceptProposal.encounter.provider.personName}" /></td>
					</tr>
					<tr>
						<th><openmrs:message code="Encounter.datetime"/></th>
						<td><openmrs:formatDate date="${conceptProposal.encounter.encounterDatetime}" type="long" /></td>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<c:if test="${conceptProposal.obs != null}">
		<tr>
			<th><openmrs:message code="ConceptProposal.obs" /></th>
			<td>
				${conceptProposal.obs.obsId}
			</td>
		</tr>
	</c:if>
	<c:if test="${obsConcept != null}">
		<tr>
			<th><openmrs:message code="ConceptProposal.obsConcept" /></th>
			<td>
				#${obsConcept.conceptId}: <c:out value="${obsConcept.name}" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(conceptProposal.creator == null)}">
		<tr>
			<th><openmrs:message code="ConceptProposal.proposedBy" /></th>
			<td>
				<c:out value="${conceptProposal.creator.personName}" /> -
				<openmrs:formatDate date="${conceptProposal.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(conceptProposal.changedBy == null)}">
		<tr>
			<th><openmrs:message code="general.changedBy" /></th>
			<td>
				<c:out value="${conceptProposal.changedBy.personName}" /> -
				<openmrs:formatDate date="${conceptProposal.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
		<th><openmrs:message code="ConceptProposal.originalText"/></th>
		<td>${conceptProposal.originalText}</td>
	</tr>
	<tr>
		<th></th>
		<td>
			<div class="subnote">
				<openmrs:message code="ConceptProposal.possibleConcepts"/>:
				<table> 
					<tr>
						<td valign="top">
							<c:forEach items="${possibleConcepts}" var="listItem" varStatus="status" begin="0" end="6">
								<c:if test="${status.index == 4}"></td><td valign="top"></c:if>
								<a href="#selectObject" 
									onClick="return selectConceptId('${listItem.conceptId}')";
									title="${listItem.description}"
									class='searchHit'>
									${status.index + 1})
									<c:choose >
										<c:when test="${listItem.synonym != ''}">
											<span class='mainHit'>${listItem.synonym}</span>
											<span class='additionalHit'>&rArr; ${listItem.name}</span>
										</c:when>
										<c:otherwise>
											<span class='mainHit'>${listItem.name}</span>
										</c:otherwise>
									</c:choose>
								</a><br/>
							</c:forEach>
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
	
	<tr><td>&nbsp;</td><td></td></tr>
	<tr>
		<th><openmrs:message code="general.locale"/></th>
		<td>
			<select name="conceptNamelocale">
				<c:forEach items="${locales}" var="locale">
					<option value="${locale}">${locale.displayName}</option>
			    </c:forEach>
			</select>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptProposal.finalText"/></th>
		<td>
			<spring:bind path="conceptProposal.finalText">
				<input type="text" name="${status.expression}" id="finalText" value="<c:if test="${(status.value == null || status.value == '') && conceptProposal.mappedConcept == null}"><c:out value="${conceptProposal.originalText}" /></c:if><c:if test="${status.value != ''}"><c:out value="${status.value}" /></c:if>" size="50" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="ConceptProposal.comments"/></th>
		<td valign="top">
			<spring:bind path="conceptProposal.comments">
				<textarea name="${status.expression}" rows="3" cols="48"><c:out value="${status.value}" /></textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<i><openmrs:message code="ConceptProposal.commentsDescription"/></i>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptProposal.mappedConcept"/></th>
		<td>
			<spring:bind path="conceptProposal.mappedConcept">
				<a target="_blank" href="${pageContext.request.contextPath}/dictionary/concept.form?conceptName=" onclick="this.href=this.href + document.getElementById('finalText').value"><openmrs:message code="Concept.add"/></a>
				<openmrs:message code="general.or" />
				<br/>
				<div dojoType="ConceptSearch" widgetId="cSearch" showVerboseListing="true"></div>
				<div dojoType="OpenmrsPopup" widgetId="cSelection" hiddenInputName="conceptId" searchWidget="cSearch" searchTitle='<openmrs:message code="Concept.find"/>'></div>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="ConceptProposal.actionToTake"/></th>
		<td>
			<input type="radio" name="actionToTake" value="saveAsSynonym" checked="checked"> <openmrs:message code="ConceptProposal.saveAsSynonym"/>
			<input type="radio" name="actionToTake" value="saveAsMapped"> <openmrs:message code="ConceptProposal.saveAsMapped"/>
		</td>
	</tr>
</table>
<br />
<!-- TODO create some sort of mechanism to scroll through the encounters -->
<c:if test="${fn:length(matchingProposals) > 1}">
	<openmrs:message code="ConceptProposal.update.note" arguments="${fn:length(matchingProposals)-1}"/>
	<c:if test="${fn:length(matchingProposals) > 2}">
	    <openmrs:message code="ConceptProposal.update.note.plural"/>
	</c:if>
	<br/>
</c:if>
<br />
<input type="submit" name="action" value="<openmrs:message code="general.submit"/>">
<input type="submit" name="action" value="<openmrs:message code="ConceptProposal.ignore"/>">
<input type="submit" name="action" value="<openmrs:message code="general.cancel"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>