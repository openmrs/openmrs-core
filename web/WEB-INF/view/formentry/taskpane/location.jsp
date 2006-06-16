<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/location.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="Location.title"/></h3>

<script src='<%= request.getContextPath() %>/dwr/interface/DWREncounterService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script>

	var savedEvent = null;
	
	var onSelect = function(locList) {
		var loc = new miniObject(locList[0]);
		setObj('//encounter.location_id', loc)
	}
	
	function miniObject(p) {
		this.key = p.locationId;
		this.value = p.name;
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
	
	function preFillTable(locations) {
		if (locations.length == 1 && typeof locations[0] == 'string') {
			//if the only object in the list is a string, its an error message
			locations.push('<p class="no_hit"><spring:message code="Location.missing" /></p>');
		}
		fillTable(locations);
	}
	
	function findObjects(text) {
		// on page startup, call search with 'All' to return all users. (or in the middle of searching, obviously)
		if (text == 'All') {
			DWREncounterService.getLocations(preFillTable);
		}
		//must have at least 2 characters entered or that character be a number
		else if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    	DWREncounterService.findLocations(preFillTable, text);
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
	
	function allowAutoListWithNumber() {
		return true;
	}
	
	var getName = function(loc) {
		if (typeof loc == 'string') return loc;
		return loc.name;
	}	
	
	var customCellFunctions = [getNumber, getName];

</script>

<form method="post" onSubmit="return search(0, event);">
	<input name="phrase" id="phrase" type="text" class="prompt" size="23" onkeydown="search(400, event);" /> &nbsp;
	<!-- <input type="checkbox" id="verboseListing" value="true" onclick="search(0, event); phrase.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label> -->
	<br />
	<small><em><spring:message code="general.search.hint"/></em></small>
</form>

<table border="0" cellpadding="2" cellspacing="0">
	<tbody id="searchBody">
	</tbody>
</table>

<script type="text/javascript">
  var phrase = document.getElementById('phrase');
  phrase.focus();
  phrase.value = "All";
  search(0, null);
</script>

<!-- <div id="xdebugBox"></div> <script>resetForm()</script> -->

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>