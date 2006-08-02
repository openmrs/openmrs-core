<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="initialDisplayValue" required="false" %>
<%@ attribute name="idPrefix" required="false" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>

<script type="text/javascript">

	var mySearch = null;
	var init = function() {
		mySearch = new fx.Resize("searchForm", {duration: 1});
		mySearch.hide();
	};
	
	var findObjects = function(txt) {
		DWRUserService.findUsers(fillTable, txt, [${roles}], false);
		return false;
	}

	var changeButton = null;
	var display = new Array();
	
	var onSelect = function(objs) {
		var obj = objs[0];
		$("${formFieldName}").value = obj.userId;
		$("userName").innerHTML = getName(obj);
		changeButton.focus();
		mySearch.hide();
		return false;
	}
	
	function showSearch(btn) {
		mySearch.hide();
		setPosition(btn, $("searchForm"), 465, 350);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	
	var getIdentifier = function(obj) {
		if (typeof obj == 'string')  return obj;
		return '';
	}
	
	var getName = function(obj) {
		if (typeof obj == 'string') return '';
		return obj.firstName + ' ' + obj.lastName;
	}
	
	function closeBox() {
		mySearch.toggle();
		return false;
	}
	
	var customCellFunctions = [getNumber, getIdentifier, getName];
	
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = init;
	} else {
		window.onload = function() {
			oldonload();
			init();
		}
	}

	function mouseover(row, isDescription) {
		if (row.className.indexOf("searchHighlight") == -1) {
			row.className = "searchHighlight " + row.className;
			var other = getOtherRow(row, isDescription);
			other.className = "searchHighlight " + other.className;
		}
	}
	function mouseout(row, isDescription) {
		var c = row.className;
		row.className = c.substring(c.indexOf(" ") + 1, c.length);
		var other = getOtherRow(row, isDescription);
		c = other.className;
		other.className = c.substring(c.indexOf(" ") + 1, c.length);
	}
	function getOtherRow(row, isDescription) {
		if (isDescription == null) {
			var other = row.nextSibling;
			if (other.tagName == null)
				other = other.nextSibling;
		}
		else {
			var other = row.previousSibling;
			if (other.tagName == null)
				other = other.previousSibling;
		}
		return other;
	}
		
	function toggle(tagName, className) {
		if (display[tagName] == "none")
			display[tagName] = "";
		else
			display[tagName] = "none";
			
		var items = document.getElementsByTagName(tagName);
		for (var i=0; i < items.length; i++) {
			var classes = items[i].className.split(" ");
			for (x=0; x<classes.length; x++) {
				if (classes[x] == className)
					items[i].style.display = display[tagName];
			}
		}
		
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

<div id="userField" class="userField">
	<a id="userName" href="#" onclick="return gotoUser('user')">${initialDisplayValue}</a>
	<input type="hidden" id="${formFieldName}" value="${initialValue}" name="${formFieldName}" />
	<input type="button" id="userButton" class="smallButton" value='<spring:message code="general.change"/>' onclick="showSearch(this)" />
</div>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;">
			<h3 id="searchTitle"><spring:message code="Encounter.provider.find"/></h3>
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