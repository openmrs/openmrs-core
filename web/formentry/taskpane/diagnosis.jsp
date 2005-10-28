<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/forms/header.jsp" %>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>

<script>

	var timeout;
	
	function searchBoxChange(event, obj) {
		if (event.altKey == false &&
			event.ctrlKey == false &&
			((event.keyCode >= 32 && event.keyCode <= 127) || event.keyCode == 8)) {
				clearTimeout(timeout);
				timeout = setTimeout("updateConcepts()", 400);
		}
	}
	
	function updateConcepts() {
	    var phrase = document.getElementById('phrase').value;
		if (phrase.length > 1) {
		    DWRUtil.removeAllRows("conceptTableBody");
		    DWRConceptService.findConcepts(fillTable, phrase , ["Diagnosis", "Finding", "Symptom", "Symptom/Finding"]);
		}
	    return false;
	}
	
	var getLink	= function(obj) { 
			var str = "";
			str += "<a href=\"#top\" onClick=\"javascript:pickProblem('<%= request.getParameter("mode") %>', '//problem_list', this)\" ";
			str += "class='hit' value='" + obj.conceptId + "'>";
			str += obj.name;
			str += "(" + obj.conceptId + ")";
			str += "</a>";
			return str;
		};
	
	function fillTable(concept) {
	    DWRUtil.addRows("conceptTableBody", concept, [ getLink ]);
	}
	
</script>



<h1>Select a Diagnosis</h1>

<form method="POST" onSubmit="updateConcepts(); return false;">
	<input name="mode" type="hidden" value='${request.mode}'>
	<input name="phrase" id="phrase" type="text" class="prompt" size="10" onKeyUp="searchBoxChange(event, this)"/>
	<br />
	<small><em>HINT: type only the first few letters</em></small>
</form>

<table border="0">
	<tbody id="conceptTableBody">
	</tbody>
</table>

<br />
<br />
<p class="no_hit">
	If you believe that you have discovered a diagnosis that should be in the
	list, please
	<a href="mailto:amrsprod@iukenya.org&subject=Missing Diagnosis - ${request.phrase}"
		tal:attributes="href string:mailto:amrsprod@iukenya.org&subject=Missing Diagnosis '${request/phrase}'">
		click here
	</a>.
<p>


<script type="text/javascript">
  document.getElementById('phrase').focus();
</script>


<%@ include file="/WEB-INF/template/forms/footer.jsp" %>