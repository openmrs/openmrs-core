<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Users" otherwise="/login.htm" redirect="/admin/users/user.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="User.manage.title"/></h2>

<a href="user.form"><spring:message code="User.add"/></a>

<br/><br/>

<b class="boxHeader"><spring:message code="User.list.title"/></b>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script src='<%= request.getContextPath() %>/scripts/openmrsSearch.js'></script>

<script>

	var onSelect = function(userList) {
		var user = userList[0];
		document.location = "user.form?userId=" + user.userId;
	}
	
	var getSystemId = function(u) {
		if (typeof u == 'string') return u;
		s = " &nbsp; " + u.systemId;;
		if (u.voided)
			s = "<span class='retired'>" + s + "</span>";
		return s;
	}
	
	var getUsername = function(u) {
		if (typeof u == 'string' || u.username == null) return '';
		return " &nbsp; " + u.username;
	}

	var getFirst = function(u) {
		if (typeof u == 'string') return '';	
		return " &nbsp; " + u.firstName;
	}

	var getLast = function(u) {
		if (typeof u == 'string') return '';	
		return " &nbsp; " + u.lastName;
	}
	
	var getRoles = function(u) {
		if (typeof u == 'string') return '';	
		return " &nbsp; " + u.roles;
	}
	
	function search(delay, event) {
		if (debugBox) debugBox.innerHTML += '<br> search() event.keyCode: ' + event.keyCode;
		var searchBox = document.getElementById("phrase");
		savedSearch = searchBox.value.toString();
		return searchBoxChange('searchBody', searchBox, event, false, delay);
	}
	
	function findObjects(text) {
		// on page startup, call search with 'All' to return all users. (or in the middle of searching, obviously)
		if (text == 'All') {
			DWRUserService.getAllUsers(fillTable, [], true);
		}
		//must have at least 2 characters entered or that character be a number
		else if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    	DWRUserService.findUsers(fillTable, text, [], true);
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
	
	var customCellFunctions = [getNumber, getSystemId, getUsername, getFirst, getLast, getRoles];
	
</script>

<div class="box">
	<form method="post" onSubmit="return search(0, event);">
		<spring:message code="User.find"/>
		<input name="phrase" id="phrase" type="text" class="prompt" size="30" onKeyUp="search(400, event)"/> &nbsp;
		<br />
	</form>
	
	<table border="0" cellpadding="1" cellspacing="0">
		<tr>
			<th> </th>
			<th> &nbsp; <spring:message code="User.systemId"/>&nbsp; </th>
			<th> &nbsp; <spring:message code="User.username"/>&nbsp; </th>
			<th> &nbsp; <spring:message code="User.firstName"/>&nbsp; </th>
			<th> &nbsp; <spring:message code="User.lastName"/>&nbsp;  </th>
			<th> &nbsp; <spring:message code="User.roles"/>&nbsp;     </th>
		</tr>
		<tbody id="searchBody">
		</tbody>
	</table>
</div>

<script type="text/javascript">
  var phrase = document.getElementById('phrase');
  phrase.focus();
  phrase.value = "All";
  search(0, null);
</script>

<!-- <div id="debugBox"></div> <script>resetForm()</script> -->

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>