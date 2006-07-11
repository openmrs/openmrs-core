<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/user.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3><spring:message code="provider.title"/></h3>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script>

	var savedEvent = null;
	
	var roles = new Array();
	<request:parameters id="r" name="role">
		<request:parameterValues id="names">
			roles.push('<jsp:getProperty name="names" property="value"/>');
		</request:parameterValues>
	</request:parameters>
	
	var onSelect = function(userList) {
		var user = new miniObject(userList[0]);
		setObj('//encounter.provider_id', user)
	}
	
	function miniObject(p) {
		this.key = p.userId;
		this.value = getName(p);
	}
	
	var getName = function(p) {
		if (typeof p == 'string') return p;
		str = ''
		str += p.firstName + " ";
		str += p.lastName;
		str += " (" + p.systemId + ")";
		return str;
	}
	
	function search(delay, event) {
		if (debugBox) debugBox.innerHTML += '<br> search() event.keyCode: ' + event.keyCode;
		var searchBox = document.getElementById("phrase");
		if (debugBox) debugBox.innerHTML += '<br> searchBox.value: ' + searchBox.value;
		savedSearch = searchBox.value.toString();
		
		// stinkin' infopath hack.  It doesn't give us onkeyup or onkeypress
		var onkeydown = true;
		if (event == null || event.type == 'onkeyup')
			onkeydown = false;
		
		return searchBoxChange('searchBody', searchBox, event, false, delay, onkeydown);
	}
	
	function preFillTable(users) {
		if (users.length == 1 && typeof users[0] == 'string') {
			//if the only object in the list is a string, its an error message
			users.push('<p class="no_hit"><spring:message code="provider.missing" /></p>');
		}
		fillTable(users);
	}
	
	function findObjects(text) {
		// on page startup, call search with 'All' to return all users. (or in the middle of searching, obviously)
		if (text == 'All') {
			DWRUserService.getAllUsers(roles, false, preFillTable);
		}
		//must have at least 2 characters entered or that character be a number
		else if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    	DWRUserService.findUsers(text, roles, false, preFillTable);
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