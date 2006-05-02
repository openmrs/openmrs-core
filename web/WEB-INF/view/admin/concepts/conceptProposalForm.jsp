<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptProposal.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWREncounterService.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>

<script type="text/javascript">

var mySearch = null;
var changeButton = null;
var searchType = "";

var onSelect = function(obj) {
	if (obj.length != null)
		var obj = obj[0];
	$("concept").value = obj.conceptId;
	$("conceptName").innerHTML = obj.name;
	mySearch.hide();
	searchType = "";
	if (changeButton != null)
		changeButton.focus();
	return false;
}

var onPossibleSelect = function(id) {
	$("concept").value = id;
	DWRConceptService.getConcept(onSelect, id);
	mySearch.hide();
	searchType = "";
	return false;
}

function showSearch(btn, type) {
	mySearch.hide();
	if (searchType != type) {
		setPosition(btn, $("searchForm"), 450, 350);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		searchType = type;
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	else {
		searchType = "";
		changeButton.focus();
	}
}

var init = function() {
	mySearch = new fx.Resize("searchForm", {duration: 100});
	mySearch.hide();
};

function closeBox() {
	searchType = "";
	mySearch.hide();
	return false;
}

var oldonload = window.onload;
if (typeof window.onload != 'function') {
	window.onload = init;
} else {
	window.onload = function() {
		oldonload();
		init();
	}
}

</script>

<h2><spring:message code="ConceptProposal.title"/></h2>

<style>
	th { text-align: left; }
	.searchForm {
		width: 450px;
		position: absolute;
		z-index: 10;
		margin: 5px;
		}
		.searchForm .wrapper {
			padding: 2px;
			background-color: whitesmoke;
			border: 1px solid grey;
			height: 350px;
		}
	.searchResults {
		height: 265px;
		overflow: auto;
		width: 440px;
	}
</style>

<form method="post">
<table>
	<c:if test="${conceptProposal.encounter != null}">
		<tr>
			<th valign="top"><spring:message code="ConceptProposal.encounter"/></th>
			<td class="sideNote">
				<table>
					<tr>
						<th><spring:message code="general.id"/></th>
						<td>${conceptProposal.encounter.encounterId}</td>
					</tr>
					<tr>
						<th><spring:message code="Encounter.type"/></th>
						<td>${conceptProposal.encounter.encounterType.name}</td>
					</tr>
					<tr>
						<th><spring:message code="Encounter.location"/></th>
						<td>${conceptProposal.encounter.location}</td>
					</tr>
					<tr>
						<th><spring:message code="Encounter.provider"/></th>
						<td>${conceptProposal.encounter.provider.firstName} ${conceptProposal.encounter.provider.lastName}</td>
					</tr>
					<tr>
						<th><spring:message code="Encounter.datetime"/></th>
						<td><openmrs:formatDate date="${conceptProposal.encounter.encounterDatetime}" type="long" /></td>
					</tr>
				</table>
			</td>
		</tr>
	</c:if>
	<c:if test="${conceptProposal.obs != null}">
		<tr>
			<th><spring:message code="ConceptProposal.obs" /></th>
			<td>
				${conceptProposal.obs.obsId}
			</td>
		</tr>
	</c:if>
	<c:if test="${obsConcept != null}">
		<tr>
			<th><spring:message code="ConceptProposal.obsConcept" /></th>
			<td>
				#${obsConcept.conceptId}: ${obsConcept.name}
			</td>
		</tr>
	</c:if>
	<c:if test="${!(conceptProposal.creator == null)}">
		<tr>
			<th><spring:message code="ConceptProposal.proposedBy" /></th>
			<td>
				${conceptProposal.creator.firstName} ${conceptProposal.creator.lastName} -
				<openmrs:formatDate date="${conceptProposal.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(conceptProposal.changedBy == null)}">
		<tr>
			<th><spring:message code="general.changedBy" /></th>
			<td>
				${conceptProposal.changedBy.firstName} ${conceptProposal.changedBy.lastName} -
				<openmrs:formatDate date="${conceptProposal.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
	<tr>
		<th><spring:message code="ConceptProposal.originalText"/></th>
		<td>${conceptProposal.originalText}</td>
	</tr>
	<tr>
		<th></th>
		<td>
			<div class="subnote">
				<spring:message code="ConceptProposal.possibleConcepts"/>:
				<table> 
					<tr>
						<td valign="top">
							<c:forEach items="${possibleConcepts}" var="listItem" varStatus="status" begin="0" end="6">
								<c:if test="${status.index == 4}"></td><td valign="top"></c:if>
								<a href="#selectObject" 
									onClick="return onPossibleSelect('${listItem.conceptId}')";
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
		<th><spring:message code="ConceptProposal.finalText"/></th>
		<td>
			<spring:bind path="conceptProposal.finalText">
				<input type="text" name="${status.expression}" id="finalText" value="<c:if test="${(status.value == null || status.value == '') && conceptProposal.mappedConcept == null}">${conceptProposal.originalText}</c:if><c:if test="${status.value != ''}">${status.value}</c:if>" size="50" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="ConceptProposal.comments"/></th>
		<td valign="top">
			<spring:bind path="conceptProposal.comments">
				<textarea name="${status.expression}" rows="3" cols="48">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			<i><spring:message code="ConceptProposal.commentsDescription"/></i>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptProposal.mappedConcept"/></th>
		<td>
			<spring:bind path="conceptProposal.mappedConcept">
				<a target="_blank" href="${pageContext.request.contextPath}/dictionary/concept.form?conceptName=" onclick="this.href=this.href + document.getElementById('finalText').value"><spring:message code="Concept.add"/></a>
				<spring:message code="general.or" />
				<br/>
				<input type="text" size="7" id="concept" value="${status.value.conceptId}" name="conceptId" />
				<div style="width:200px; float:left;" id="conceptName">${mappedConceptName}</div>
				<input type="button" id="conceptButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'concept')" />				
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br />
<!-- TODO create some sort of mechanism to scroll through the encounters -->
<c:if test="${fn:length(matchingProposals) > 1}">
	Note: This update will effect ${fn:length(matchingProposals)-1} other concept proposal<c:if test="${fn:length(matchingProposals) > 2}">s</c:if><br/>
</c:if>
<br />
<input type="submit" name="action" value="<spring:message code="ConceptProposal.update"/>">
<input type="submit" name="action" value="<spring:message code="ConceptProposal.saveAsConcept"/>">
<input type="submit" name="action" value="<spring:message code="ConceptProposal.saveAsSynonym"/>">
</form>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, event, false, 0); return false;">
			<h3><spring:message code="general.search"/></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);"> &nbsp;
			<input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
		</form>
		<div id="searchResults" class="searchResults">
			<table width="100%">
				<tbody id="searchBody">
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>