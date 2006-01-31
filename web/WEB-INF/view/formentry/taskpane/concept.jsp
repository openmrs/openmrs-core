<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/concept.htm" />

<%@ include file="/WEB-INF/template/taskpane/header.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>
<script src='<%= request.getContextPath() %>/scripts/conceptSearch.js'></script>

<script>

	var conceptClasses = new Array();
	<request:parameters id="className" name="class">
		conceptClasses.push("${class}");
	</request:parameters>

	var onSelect = function(conceptList) {
		for (i=0; i<conceptList.length; i++) {
			pickProblem('<%= request.getParameter("mode") %>', '//problem_list', conceptList[i]);
		}
	}
	
	function search(delay, event) {
		var searchBox = document.getElementById("phrase");
		return searchBoxChange('conceptSearchBody', searchBox, event, false, delay);
	}
	
	function preFillTable(concepts) {
		if (concepts.length == 1 && typeof concepts[0] == 'string') {
			//if the only object in the list is a string, its an error message
			concepts.push("<a href='#proposeConcept' onclick='showProposeConceptForm()'>Possible new concept?</a>");
			fillTable(concepts);
		}
	}
	
	function showProposeConceptForm() {
		$('proposeConceptForm').style.display = "";
		$('proposedText').focus();
	}
	
	function proposeConcept() {
		var box = $('proposedText');
		if (box.value == '')  {
			alert("Proposed Concept text must be entered");
			box.focus();
		}
		else {
			$('proposeConceptForm').style.display = "none";
			DWRConceptService.findProposedConcepts(preProposedConcepts, box.value);
		}
	}
	
	function preProposedConcepts(concepts) {
		if (concepts.length == 0) {
			var concept = new miniConcept();
			conceptList = new Array();
			conceptList.push(concept);
			onSelect(conceptList);
		}
		else {
			//display a box telling them to pick a preposed concept:
			$("preProposedAlert").style.display = "";
			fillTable(concepts);
		}
	}
	
	function miniConcept(n) {
		this.conceptId = 0;
		if (n == null)
			this.name = $('proposedText').value;
		else
			this.name = n;
	}
		
</script>

<style>
	#proposeConceptForm {
		display: none;
	}
</style>

<h1><spring:message code="diagnosis.title"/></h1>

<div id="preProposedAlert">
	These concepts were proposed and accepted with the same text
</div>

<form method="POST" onSubmit="return search(0, event);">
	<input name="mode" type="hidden" value='${request.mode}'>
	<input name="phrase" id="phrase" type="text" class="prompt" size="10" onkeyup="search(400, event)"/> &nbsp;
	<input type="checkbox" id="verboseListing" value="true" onclick="search(0, event); phrase.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
	<br />
	<small><em><spring:message code="diagnosis.hint"/></em></small>
</form>

<table border="0">
	<tbody id="conceptSearchBody">
	</tbody>
</table>

<div id="proposeConceptForm">
	<table>
		<tr>
			<td><spring:message code="ConceptProposal.text"/></td>
			<td><input type="text" name="text" id="proposedText" value="" size="40" /></td>
		</tr>
	</table>
	This text will be saved temporarily until an administrator can review and it to the list.<br/>
	<input type="button" onclick="proposeConcept()" value="<spring:message code="ConceptProposal.propose" />" />
</div>

<br />

<script type="text/javascript">
  document.getElementById('phrase').focus();
</script>

<%@ include file="/WEB-INF/template/taskpane/footer.jsp" %>