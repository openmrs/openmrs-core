<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/admin/concepts/proposeConcept.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>

<script type="text/javascript">

var mySearch = null;
var changeButton = null;
var searchType = "";

var init = function() {
	mySearch = new fx.Resize("searchForm", {duration: 100});
	mySearch.hide();
};

var findObjects = function(txt) {
	if (searchType == 'concept') {
		DWRConceptService.findConcepts(fillTable, txt, [], 0, ['N/A']);
	}
	return false;
}

var onSelect = function(objs) {
	var obj;
	if (objs instanceof Array)
		obj = objs[0];
	else
		obj = objs;
	if (searchType == 'concept') {
		$("concept").value = obj.conceptId;
		$("conceptName").innerHTML = obj.name;
		$("conceptDescription").innerHTML = obj.description;
	}
	
	mySearch.hide();
	searchType = "";
	if (changeButton != null)
		changeButton.focus();
	
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
		left: -1000px;
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
<c:if test="${conceptProposal.encounter != null}">
	<table>
		<tr>
			<th valign="top"><spring:message code="ConceptProposal.encounter"/></th>
			<td>
				<spring:bind path="conceptProposal.encounter">
					${status.value.encounterId}
					<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${status.value.encounterId}"><spring:message code="general.view"/>/<spring:message code="general.edit"/></a>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<th valign="top"><spring:message code="ConceptProposal.obsConcept" /></th>
			<td>
				<spring:bind path="conceptProposal.obsConcept">
					<c:choose>
						<c:when test="${conceptProposal.obsConcept != null}">
							<div id="conceptName">${conceptName}</div>
						</c:when>
						<c:otherwise>
							<div style="width:200px; float:left;" id="conceptName">${conceptName}</div>
							<input type="hidden" id="concept" value="${status.value.conceptId}" name="conceptId" />
							<input type="button" id="conceptButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this, 'concept')" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</c:otherwise>
					</c:choose>
					<div class="description" style="clear: left;" id="conceptDescription">${conceptName.description}</div>
				</spring:bind>
			</td>
		</tr>
	</table>
</c:if>

<spring:message code="ConceptProposal.proposeWarning"/> <br/>
<spring:message code="ConceptProposal.proposeInfo"/>
<spring:bind path="conceptProposal.originalText">
	<input type="text" name="${status.expression}" id="originalText" value="" size="60" />
	<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
</spring:bind>

<br /><br />
<input type="submit" value="<spring:message code="ConceptProposal.propose"/>">

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
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>