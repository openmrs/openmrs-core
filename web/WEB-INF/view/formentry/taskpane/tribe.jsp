<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/tribe.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="Tribe.select"/></h3>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script>
	
	var onSelect = function(tList) {
		var t = new miniObject(tList[0]);
		setObj('//tribe.tribe_id', t)
	}
	
	function miniObject(p) {
		this.key = p.tribeId;
		this.value = getName(p);
	}
	
	var getName = function(p) {
		if (typeof p == 'string') return p;
		return p.name;
	}
	
	function search(delay, event) {
		var searchBox = document.getElementById("phrase");
		savedSearch = searchBox.value.toString();
		
		// stinkin' infopath hack.  It doesn't give us onkeyup or onkeypress
		var onkeydown = true;
		if (event == null || event.type == 'onkeyup')
			onkeydown = false;
		
		return searchBoxChange('searchBody', searchBox, event, false, delay, onkeydown);
	}
	
	function preFillTable(tribes) {
		if (tribes.length == 1 && typeof tribes[0] == 'string') {
			//if the only object in the list is a string, its an error message
			tribes.push('<p class="no_hit"><spring:message code="Tribe.missing" /></p>');
		}
		fillTable(tribes);
	}
	
	function findObjects(text) {
		// on page startup, call search with 'All' to return all users. (or in the middle of searching, obviously)
		if (text == 'All') {
			DWRPatientService.getTribes(preFillTable);
		}
		//must have at least 2 characters entered or that character be a number
		else if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    	DWRPatientService.findTribes(preFillTable, text);
		}
		else {
			var msg = new Array();
			msg.push("Invalid number of search characters");
			fillTable(msg);
		}
	    return false;
	}
	
	function allowAutoJump() {
		if ($('phrase').value == 'All')
			return false;
		else
			return true;
	}
	
	var customCellFunctions = [getNumber, getName];
	
</script>

<form method="post" onSubmit="return search(0, event);">
	<input name="phrase" id="phrase" type="text" class="prompt" size="23" onKeyDown="search(400, event)"/> &nbsp;
	<!-- <input type="checkbox" id="verboseListing" value="true" onclick="search(0, event); phrase.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label> -->
	<br />
	<small><em><spring:message code="general.search.hint"/></em></small>
</form>

<table border="0">
	<tbody id="searchBody">
	</tbody>
</table>

<script type="text/javascript">
  var phrase = document.getElementById('phrase');
  phrase.focus();
  phrase.value = "All";
  search(0, null);
</script>

<!-- <div id="debugBox"></div> <script>resetForm()</script> -->

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>
