<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Alerts" otherwise="/login.htm" redirect="/admin/users/alert.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>

<script type="text/javascript">

	var init = function() {
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
	};
	
	var findObjects = function(txt) {
		DWRUserService.findUsers(fillTable, txt, [], false);
		return false;
	}
	
	var onSelect = function(objs) {
		var obj = objs[0];
		var options = $("userNames").options;
		if (isAddable(obj.userId, options)) {
			var opt = new Option(getName(obj), obj.userId);
			opt.selected = true;
			options[options.length] = opt;
			copyIds("userNames", "userIds", " ");
		}
		changeButton.focus();
		mySearch.hide();
		return false;
	}
	
	function isAddable(value, options) {
		for (x=0; x<options.length; x++)
			if (options[x].value == value)
				return false;
	
		return true;
	}
	
	function showSearch(btn) {
		mySearch.hide();
		setPosition(btn, $("searchForm"), 465, 350);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		$('searchTitle').innerHTML = '<spring:message code="User.find"/>';
		searchType = 'user';
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	
	var getName = function(obj) {
		if (typeof obj == 'string') return '';
		str = obj.firstName;
		str += ' ';
		str += obj.lastName;
		return str;
	}
	
	function closeBox() {
		mySearch.toggle();
		return false;
	}
	
	var customCellFunctions = [getNumber, getName];
	
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = init;
	} else {
		window.onload = function() {
			oldonload();
			init();
		}
	}
	
	function gotoUser(select, userId) {
		if (userId == null)
			userId = $(select).value;
		if (userId != "")
			window.location = "${pageContext.request.contextPath}/admin/users/user.form?userId=" + userId;
		return false;
	}
	
	function removeItem(nameList, idList, delim)
	{
		var sel   = document.getElementById(nameList);
		var input = document.getElementById(idList);
		var optList   = sel.options;
		var lastIndex = -1;
		var i = 0;
		while (i<optList.length) {
			// loop over and erase all selected items
			if (optList[i].selected) {
				optList[i] = null;
				lastIndex = i;
			}
			else {
				i++;
			}
		}
		copyIds(nameList, idList, delim);
		while (lastIndex >= optList.length)
			lastIndex = lastIndex - 1;
		if (lastIndex >= 0) {
			optList[lastIndex].selected = true;
			return optList[lastIndex];
		}
		return null;
	}
	
	function copyIds(from, to, delimiter)
	{
		var sel = document.getElementById(from);
		var input = document.getElementById(to);
		var optList = sel.options;
		var remaining = new Array();
		var i=0;
		while (i < optList.length)
		{
			remaining.push(optList[i].value);
			i++;
		}
		input.value = remaining.join(delimiter);
	}
	
	function removeHiddenRows() {
		var rows = document.getElementsByTagName("TR");
		var i = 0;
		while (i < rows.length) {
			if (rows[i].style.display == "none") {
				rows[i].parentNode.removeChild(rows[i]);
			}
			else {
				i = i + 1;
			}
		}
	}
	
	function listKeyPress(from, to, delim, event) {
		var keyCode = event.keyCode;
		if (keyCode == 8 || keyCode == 46) {
			removeItem(from, to, delim);
			window.Event.keyCode = 0;	//attempt to prevent backspace key (#8) from going back in browser
		}
	}
	
	function addRole() {
		var obj = document.getElementById("roleStr");
		var synonyms = document.getElementById("roles").options;
		if (synonyms == null)
			synonyms = new Array();
		var syn = obj.value;
		if (syn != "") {
			if (isAddable(syn, synonyms)) {
				var opt = new Option(syn, syn);
				opt.selected = true;
				synonyms[synonyms.length] = opt;
			}
		}
		obj.value = "";
		obj.focus();
		copyIds("roles", "newRoles", ",");
		window.Event.keyCode = 0;  //disable enter key submitting form
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
	th { text-align: left; }
</style>

<h2><spring:message code="Alert.manage.title"/></h2>	

<spring:hasBindErrors name="alert">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<th><spring:message code="Alert.text"/></th>
		<td>
			<spring:bind path="alert.text">
				<input type="text" id="text" name="${status.expression}" value="${status.value}" size="55">
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.recipients"/></th>
		<td valign="top">
			<input type="hidden" name="userIds" id="userIds" size="40" value='<c:forEach items="${alert.recipients}" var="recipient">${recipient.recipient.userId} </c:forEach>' />
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select class="mediumWidth" size="6" id="userNames" multiple onkeyup="listKeyPress('userNames', 'userIds', ' ', event);">
							<c:forEach items="${alert.recipients}" var="recipient">
								<option value="${recipient.recipient.userId}">${recipient.recipient.firstName} ${recipient.recipient.middleName} ${recipient.recipient.lastName}</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="showSearch(this);" /> <br/>
						&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('userNames', 'userIds', ' ');" /> <br/>
						&nbsp;<input type="button" value="<spring:message code="User.goTo"/>" class="smallButton" onClick="gotoUser('userNames');" /><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.roles"/></th>
		<td valign="top">
			<select id="roleStr" class="mediumWidth">
				<option value=""><spring:message code="general.none"/></option>
				<c:forEach items="${allRoles}" var="role">
					<option value="${role.role}" <c:if test="${role == status.value}">selected</c:if>>${role.role}</option>
				</c:forEach>
			</select>
			<input type="button" class="smallButton" value="<spring:message code="Alert.addRole"/>" onClick="addRole();"/>
			<input type="hidden" name="newRoles" id="newRoles" value="" />
		</td>
	</tr>
	<tr>
		<th></th>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<select class="mediumWidth" size="3" multiple id="roles" onkeydown="listKeyPress('roles', 'newRoles', ',', event);">
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('roles', 'newRoles', ',');" /> <br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.satisfiedByAny"/></th>
		<td valign="top">
			<spring:bind path="alert.satisfiedByAny">
				<input type="hidden" name="_${status.expression}" value="on" />
				<input type="checkbox" name="${status.expression}"
					   value="on" <c:if test="${alert.satisfiedByAny}">checked</c:if> />
			</spring:bind>
			<i><spring:message code="Alert.satisfiedByAny.description"/></i>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.dateToExpire"/></th>
		<td valign="top">
			<spring:bind path="alert.dateToExpire">
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(encounter.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td>
				<a href="#View User" onclick="return gotoUser(null, '${alert.changedBy.userId}')">${alert.changedBy.firstName} ${alert.changedBy.lastName}</a> -
				<openmrs:formatDate date="${alert.dateChanged}" type="medium" />
			</td>
		</tr>
	</c:if>
</table>

<input type="submit" value="<spring:message code="Alert.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;">
			<h3 id="searchTitle"></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
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