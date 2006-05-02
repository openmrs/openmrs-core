<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Concepts" otherwise="/login.htm" redirect="/admin/concepts/conceptDrug.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>

<script type="text/javascript">

	var mySearch = null;
	var findObjects = null;
	var searchType = "";
	var changeButton = null;
	<request:existsParameter name="autoJump">
		autoJump = <request:parameter name="autoJump"/>;
	</request:existsParameter>
	var display = new Array();
	
	var init = function() {
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
	};
	
	var findObjects = function(txt) {
		DWRConceptService.findConcepts(fillTable, txt, ['Drug'], false, []);
		return false;
	}
	
	var onSelect = function(objs) {
		var concept = objs[0];
		$("concept").value = concept.conceptId;
		$("conceptName").innerHTML = concept.name;
		changeButton.focus();
		mySearch.hide();
		return false;
	}
	
	function showSearch(btn) {
		mySearch.hide();
		setPosition(btn, $("searchForm"), 465, 350);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		$('searchTitle').innerHTML = '<spring:message code="ConceptDrug.find"/>';
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	
	function closeBox() {
		mySearch.toggle();
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
	
	function gotoConcept(tagName, conceptId) {
		if (conceptId == null)
			conceptId = $(tagName).value;
		window.location = "${pageContext.request.contextPath}/dictionary/concept.form?conceptId=" + userId;
		return false;
	}

	function gotoUser(tagName, userId) {
		if (userId == null)
			userId = $(tagName).value;
		window.location = "${pageContext.request.contextPath}/admin/users/user.form?userId=" + userId;
		return false;
	}

</script>

<style>
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
		height: 330px;
	}
	.searchResults {
		height: 270px;
		overflow: auto;
	}
	#table th {
		text-align: left;
	}
</style>

<h2><spring:message code="ConceptDrug.manage.title"/></h2>

<spring:hasBindErrors name="drug">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post">
<table cellpadding="3" cellspacing="0" id="table">
	<tr>
		<th><spring:message code="general.name"/></th>
		<td>
			<spring:bind path="drug.name">			
				<input type="text" name="${status.expression}" size="40" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.concept"/></th>
		<td>
			<spring:bind path="drug.concept">
				<table>
					<tr>
						<td><a id="conceptName" href="#View Concept" onclick="return gotoConcept('concept')">${conceptName}</a></td>
						<td>
							&nbsp;
							<input type="hidden" id="concept" value="${status.value.conceptId}" name="conceptId" />
							<input type="button" id="conceptButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this)" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</tr>
				</table>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.inn"/></th>
		<td>
			<spring:bind path="drug.inn">			
				<input type="text" name="${status.expression}" size="30" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.combination"/></th>
		<td>
			<spring:bind path="drug.combination">	
				<input type="hidden" name="_${status.expression}" value=""/>		
				<input type="checkbox" name="${status.expression}" 
					   <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.dailyMgPerKg"/></th>
		<td>
			<spring:bind path="drug.dailyMgPerKg">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.dosageForm"/></th>
		<td>
			<spring:bind path="drug.dosageForm">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.doseStrength"/></th>
		<td>
			<spring:bind path="drug.doseStrength">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.minimumDose"/></th>
		<td>
			<spring:bind path="drug.minimumDose">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.maximumDose"/></th>
		<td>
			<spring:bind path="drug.maximumDose">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.route"/></th>
		<td>
			<spring:bind path="drug.route">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.shelfLife"/></th>
		<td>
			<spring:bind path="drug.shelfLife">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.therapyClass"/></th>
		<td>
			<spring:bind path="drug.therapyClass">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="ConceptDrug.units"/></th>
		<td>
			<spring:bind path="drug.units">			
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(drug.creator == null)}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				<a href="#View User" onclick="return gotoUser(null, '${drug.creator.userId}')">${drug.creator.firstName} ${drug.creator.lastName}</a> -
				<openmrs:formatDate date="${drug.dateCreated}" type="medium" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="hidden" name="phrase" value='<request:parameter name="phrase" />'/>
<input type="submit" value='<spring:message code="ConceptDrug.save"/>'>
&nbsp;
<input type="button" value='<spring:message code="general.cancel"/>' onclick="history.go(-1); return; document.location='index.htm?autoJump=false&phrase=<request:parameter name="phrase"/>'">
</form>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;">
			<h3 id="searchTitle"></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
			<input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
		</form>
		<div id="searchResults" class="searchResults">
			<table cellpadding="2" cellspacing="0">
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