<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Data Exports" otherwise="/login.htm" redirect="/admin/dataExports/dataExport.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<% pageContext.setAttribute("linefeed", "\r\n"); %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRPatientService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript">
	
	function selectTab(tab) {
		var displays = new Array();
		
		var tabs = tab.parentNode.getElementsByTagName("a");
		for (var tabIndex=0; tabIndex<tabs.length; tabIndex++) {
			var index = tabs[tabIndex].id.indexOf("Tab");
			var divId = tabs[tabIndex].id.substr(0, index);
			if (tabs[tabIndex] == tab) {
				displays[divId] = "";
				addClass(tabs[tabIndex], 'selectedTab');
			}
			else {
				displays[divId] = "none";
				removeClass(tabs[tabIndex], 'selectedTab');
			}
		}
		
		var parent = tab.parentNode.parentNode;
		var divs = parent.getElementsByTagName("div");		
		for (var divIndex=0; divIndex<divs.length; divIndex++) {
			var div = divs[divIndex];
			if (div.parentNode == parent && div != tab.parentNode) {
				div.style.display = displays[div.id];
			}
		}
		
		tab.blur();
		return false;
	}
	
	function deleteTab(tab) {
		var column = tab.parentNode.parentNode;
		column.style.display = "none";
		updateColumnClasses(column);
	}
	
	function moveColumn(dir, column) {
		var sibling = column.previousSibling;
		var parent = column.parentNode;
		if (dir == "down")
			sibling = column.nextSibling;
		if (sibling != null && column.tagName == sibling.tagName) {
			var newSibling = sibling.cloneNode(true);
			var newColumn = column.cloneNode(true);
			
			// fix for javascript cloneNode function.  Doesn't seem to be copying the value of 
			// either textareas or dropdowns.
			var id = newSibling.id.substr(newSibling.id.indexOf("_")+1, 3);
			var newTextarea = getChildByName(newSibling, "calculatedValue_" + id);
			id = sibling.id.substr(sibling.id.indexOf("_")+1, 3);
			var oldTextarea = getChildByName(sibling, "calculatedValue_" + id);
			newTextarea.value = oldTextarea.value;
			
			id = newColumn.id.substr(newColumn.id.indexOf("_")+1, 3);
			newTextarea = getChildByName(newColumn, "calculatedValue_" + id);
			id = column.id.substr(column.id.indexOf("_")+1, 3);
			oldTextarea = getChildByName(column, "calculatedValue_" + id);
			newTextarea.value = oldTextarea.value;
			
			
			parent.replaceChild(newColumn, sibling);
			parent.replaceChild(newSibling, column);
			updateColumnClasses(newColumn);
		}
	}
	
	function updateSimpleColumn(sel) {
		if (sel.value != "") {
			var count = sel.name.substr(sel.name.indexOf("_")+1, 3);
			var input = getPreviousSibling(sel, "simpleValue_" + count);
			input.value = sel.value;
			var tbl = getParentByTagName(sel, "table");
			input = getChildByName(tbl, "simpleName_" + count);
			if (input != null && input.value == "") {
				var opt = sel.options[sel.selectedIndex];
				input.value = opt.text.substr(2, opt.text.length);
			}
		}
	}
	
	function updateCalcColumn(sel) {
		if (sel.value != "") {
			var count = sel.name.substr(sel.name.indexOf("_")+1, 3);
			var input = getPreviousSibling(sel, "calculatedValue_" + count);
			input.value = sel.value;
			input.value += '\n';
			var tbl = getParentByTagName(sel, "table");
			input = getChildByName(tbl, "calculatedName_" + count);
			if (input != null && input.value == "") {
				var opt = sel.options[sel.selectedIndex];
				input.value = opt.text.substr(2, opt.text.length);
			}
		}
	}
	
	function getPreviousSibling(obj, name) {
		var sibling = obj.previousSibling;
		name = name.toLowerCase();
		while (sibling != null) {
			if (sibling.name != null && sibling.name.toLowerCase() == name)
				return sibling;
			sibling = sibling.previousSibling;
		}
		return null;
	}
	
	function getParentByTagName(obj, tagName) {
		var parent = obj.parentNode;
		tagName = tagName.toLowerCase();
		while (parent != null) {
			if (parent.tagName.toLowerCase() == tagName)
				return parent;
			parent = parent.parentNode;
		}
		return null;
	}
	
	function getChildById(obj, id) {
		if (obj.id == id)
			return obj;
		if (obj.hasChildNodes()) {
			for (var i=0; i<obj.childNodes.length; i++) {
				var child = getChildById(obj.childNodes[i], id);
				if (child != null)
					return child;
			}
		}
		return null;
	}

	function getChildByName(obj, name) {
		if (obj.name == name)
			return obj;
		if (obj.hasChildNodes()) {
			for (var i=0; i<obj.childNodes.length; i++) {
				var child = getChildByName(obj.childNodes[i], name);
				if (child != null)
					return child;
			}
		}
		return null;
	}
	
	var idCount = new Array();
	function addNew(button, id) {
		var obj = document.getElementById(id);
		var newObj = obj.cloneNode(true);
		var count = idCount[id];
		if (count == null)
			count = 0;
		else
			count = count + 1;
		idCount[id] = count;
		newObj.id += "_" + count;
		newObj.style.display = "";
		button.parentNode.insertBefore(newObj, button);
		if (id == "newColumn") {
			renameColumnInputs(newObj, count);
			updateColumnClasses(newObj);
			selectTab(getChildById(newObj, 'simpleTab'));
		}
		return newObj;
	}
	
	function updateColumnClasses(newObj) {
		var objs = newObj.parentNode.getElementsByTagName(newObj.tagName);
		var newObjId = newObj.id.substr(0, newObj.id.indexOf("_"));
		var count = 0;
		// loop over all inner objects that match the tagName of the new object
		for (var i=0; i<objs.length; i++) {
			var obj = objs[i];
			var id = obj.id.substr(0, obj.id.indexOf("_"));
			// only update the inner objects that match the id of the new object
			if (id == newObjId) {
				if (obj.style.display != "none") {
					var className = "oddRow";
					if (count % 2 == 0)
						className = "evenRow";
					removeClass(obj, "oddRow");
					removeClass(obj, "evenRow");
					addClass(obj, className);
					count = count + 1;
				}
			}
		}
	}
	
	function renameColumnInputs(obj, count) {
		getChildByName(obj, "columnId").value = count;
		var suffix = "_" + count;
		getChildByName(obj, "simpleName").name += suffix;
		getChildByName(obj, "simpleValue").name += suffix;
		getChildByName(obj, "simplePatient").name += suffix;
		getChildByName(obj, "conceptColumnName").name += suffix;
		getChildByName(obj, "conceptName").name += suffix;
		var mod = getChildByName(obj, "conceptModifier");
		while (mod != null) {
			mod.name += suffix;
			mod = getChildByName(obj, "conceptModifier");
		}
		var ext = getChildByName(obj, "conceptExtra");
		while (ext != null) {
			ext.name += suffix;
			ext = getChildByName(obj, "conceptExtra");
		}
		getChildByName(obj, "conceptButton").name += suffix;
		getChildByName(obj, "calculatedName").name += suffix;
		getChildByName(obj, "calculatedValue").name += suffix;
		getChildByName(obj, "calculatedPatient").name += suffix;
	}
	
	var mySearch = null;
	var searchType = "";
	var changeButton = null;
	
	var init = function() {
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
		propertySetup();
	};

	var findObjects = function(txt) {
		if (searchType == 'patient') {
			DWRPatientService.findPatients(fillTable, txt, 0);
		}
		else if (searchType == 'concept') {
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
		if (searchType == 'patient') {
			var input = getPreviousSibling(changeButton, "patientId");
			input.value = obj.patientId
			var oldTxt = changeButton.nextSibling;
			var txt = document.createTextNode(" " + getName(obj));
			oldTxt.parentNode.replaceChild(txt, oldTxt);
		}
		else if (searchType == 'concept') {
			var count = changeButton.name.substr(changeButton.name.indexOf("_")+1, 3);
			var input = getPreviousSibling(changeButton, "conceptName_" + count);
			input.value = obj.name;
			var tbl = getParentByTagName(changeButton, "table");
			input = getChildByName(tbl, "conceptColumnName_" + count);
			if (input != null && input.value == "") {
				var index = obj.name.indexOf(" ");
				if (index == -1)
					index = obj.name.length;
				input.value = obj.name.substr(0, index);
			}
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
			//if they've changed which form they want
			setPosition(btn, $("searchForm"), 465, 515);
			resetForm();
			mySearch.toggle();
			DWRUtil.removeAllRows("searchBody");
			searchType = type;
			var searchText = $("searchText");
			searchText.value = '';
			searchText.select();
			changeButton = btn;
		}
		else {
			searchType = "";
			changeButton.focus();
		}
	}
	
	var getName = function(obj) {
		if (typeof obj == 'string') return obj;
		str = '';
		if (searchType == 'patient') {
			str += obj.givenName;
			str += ' ';
			str += obj.middleName;
			str += ' ';
			str += obj.familyName;
		}
		else if (searchType == 'concept') {
			return getCellContent(obj);
		}
		return str;
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
	
	function removeHiddenDivs() {
		var divs = document.getElementsByTagName("DIV");
		var i = 0;
		while (i < divs.length) {
			var div = divs[i];
			if (div.style.display == "none" && div.id != 'defineCohort' && div.id != 'defineColumns') {
				div.parentNode.removeChild(div);
			}
			else {
				i = i + 1;
			}
		}
	}
		
	function allowAutoListWithNumber() {
		return true;
	}
	
	function closeBox() {
		searchType = "";
		mySearch.hide();
		return false;
	}
	
	function redirectPage(url) {
		setTimeout("document.location='" + url + "'", 3000);
	}
	
	function customGetRowHeight(h) {
		return (h - 6);
	}

</script>

<style>
	th { text-align: left; }
	
	.addNew {
		font-size: 10px;
		margin: 3px;
		cursor: pointer;
	}
	
	.tabBar .tab:visited, .tabBar .tab:link {
		background-color: white;
		text-decoration: none;
	}
	
	.tabBar .tab {
		border: 1px solid cadetblue;
		padding: 1px 5px 0 5px;
		margin-left: 3px;
		color: #CCDDFF;
	}
	
	.tabBar .selectedTab {
		border-bottom-style: dotted;
		color: #3366CC;
	}
	
	.tabBar a img {
		border-width: 0px;
		vertical-align: top;
	}
	
	.column {
		margin-top: 10px;
		margin-left: 5px;
		margin-bottom: 3px;
		background-color: white;
	}
	
	.box {
		border-color: cadetblue;
	}
	
	.evenRow .box, .evenRow .tabBar .selectedTab {
		background-color: whitesmoke;
	}
	
	.searchForm {
		width: 450px;
		position: absolute;
		z-index: 10;
		margin: 5px;
		left: -1000px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 500px;
	}
	.searchResults {
		height: 415px;
		overflow: auto;
		width: 440px;
	}
</style>

<h2><spring:message code="DataExport.title"/></h2>

<spring:hasBindErrors name="dataExport">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.globalErrors}" var="error">
			<spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/><br/><!-- ${error} -->
		</c:forEach>
		<c:forEach items="${errors.allErrors}" var="error">
			<!-- ${error} -->
		</c:forEach>
	</div>
	<br/>
</spring:hasBindErrors>

<form method="post" onSubmit="removeHiddenDivs()">
<table>
	<tr>
		<th><spring:message code="general.name"/></th>
		<td colspan="5">
			<spring:bind path="dataExport.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="general.description"/></th>
		<td valign="top" colspan="5">
			<spring:bind path="dataExport.description">
				<textarea name="description" rows="2" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(dataExport.creator == null)}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${dataExport.creator.firstName} ${dataExport.creator.lastName} -
				<openmrs:formatDate date="${dataExport.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />

<div id="xmlData">

	<div class="tabBar">
		<a id="defineCohortTab" class="tab" href="#selectCohort" onclick="return selectTab(this)"><spring:message code="DataExport.defineCohort"/></a>
		<a id="defineColumnsTab" class="tab" href="#selectColumns" onclick="return selectTab(this)"><spring:message code="DataExport.defineColumns"/></a>
	</div>
	
	<!-- Patient set definitions -->
	<div id="defineCohort" class="box">
		<table>
			<tr>
				<th colspan="2"><spring:message code="DataExport.patientMatch"/></th>
			</tr>
			<tr>
				<td colspan="2">
					<div id="newPatient" style="display: none">
						<input type="hidden" name="patientId">
						<input type="button" id="patientButton" class="smallButton" 
							value='<spring:message code="general.change"/>' 
							onclick="showSearch(this, 'patient')" />
					</div>
					<input type="button" onClick="return addNew(this, 'newPatient');" class="addNew" id="newPatientButton" value='<spring:message code="DataExport.addPatient" />' />
					<br/>
				</td>
			<tr>
				<th colspan="2"><spring:message code="DataExport.encounterMatch"/></th>
			</tr>
			<!--
			<tr>
				<td><spring:message code="DataExport.startDate"/></td>
				<td><input type="text" name="startDate" onClick="showCalendar(this)" /></td>
			</tr>
			<tr>
				<td><spring:message code="DataExport.endDate"/></td>
				<td><input type="text" name="endDate" onClick="showCalendar(this)" /></td>
			</tr>
			-->
			<tr>
				<td><spring:message code="DataExport.location"/></td>
				<td>
					<spring:bind path="dataExport.location">
						<select name="location">
							<option value=""><spring:message code="DataExport.allLocations"/></option>
							<openmrs:forEachRecord name="location">
								<option value="${record.locationId}" <c:if test="${status.value == record.locationId}">selected</c:if>>${record.name}</option>
							</openmrs:forEachRecord>
						</select>
					</spring:bind>
				</td>
			</tr>
		</table>
	</div>
	
	<!-- Column Definitions -->
	<div id="defineColumns" class="box">
		<div id="newColumn" class="column" style="display: none">
			<div class="tabBar">
				<a id="simpleTab" class="tab" href="#selectSimpleTab" onclick="selectTab(this)"><spring:message code="DataExport.simpleTab"/></a>
				<a id="conceptTab" class="tab" href="#selectConceptTab" onclick="selectTab(this)"><spring:message code="DataExport.conceptTab"/></a>
				<a id="calcTab" class="tab" href="#selectCalcTab" onclick="selectTab(this)"><spring:message code="DataExport.calculatedTab"/></a>
				&nbsp; 
				<a href="#deleteColumn" onclick="deleteTab(this)"><img src="${pageContext.request.contextPath}/images/delete.gif" title="Delete this column"/></a>
				&nbsp; 
				<a href="#moveUp" class="moveColumnUp" onclick="moveColumn('up', this.parentNode.parentNode)"><img src="${pageContext.request.contextPath}/images/moveup.gif" title="Move this column Up"/></a>
				<a href="#moveDown" class="moveColumnDown" onclick="moveColumn('down', this.parentNode.parentNode)"><img src="${pageContext.request.contextPath}/images/movedown.gif" title="Move this column Down"/></a>
			</div>
			<input type="hidden" name="columnId" value="" />
			<div id="simple" class="box">
				<%@ include file="include/simpleColumns.jsp" %>
			</div>
			<div id="concept" class="box">
				<%@ include file="include/conceptColumns.jsp" %>
			</div>
			<div id="calc" class="box">
				<%@ include file="include/calculatedColumns.jsp" %>
			</div>
		</div>
		<input type="button" onClick="return addNew(this, 'newColumn');" class="addNew" id="newColumnButtom" value='<spring:message code="DataExport.addColumn" />' />
	</div>
	
</div>

<br />
<input type="checkbox" id="saveAsNew" name="saveAsNew" value="1" /><label for="saveAsNew"><spring:message code="DataExport.saveAs"/></label>
<br />
<input type="submit" name="action" value='<spring:message code="DataExport.save"/>'>
<!-- <input type="submit" name="action" value='<spring:message code="DataExport.saveGenerate"/>' onclick="redirectPage('dataExport.list')"> -->
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
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<script type="text/javascript">

	selectTab(document.getElementById('defineCohortTab'));

	function propertySetup() {
		<c:if test="${dataExport.reportObjectId != null}">
			var btn = $('newColumnButtom');
			var obj;
			<c:forEach items="${dataExport.columns}" var="column">
				obj = addNew(btn, "newColumn");
				var count = obj.id.substr(obj.id.indexOf("_")+1, 3);
				<c:if test="${column.columnType == 'simple'}">
					selectTab(getChildById(obj, 'simpleTab'));
					getChildByName(obj, "simpleName_" + count).value = "${column.columnName}";
					getChildByName(obj, "simpleValue_" + count).value = "${column.returnValue}";
				</c:if>
				<c:if test="${column.columnType == 'concept'}">
					selectTab(getChildById(obj, 'conceptTab'));
					getChildByName(obj, "conceptColumnName_" + count).value = "${column.columnName}";
					var extras = new Array();
					<c:forEach items="${column.extras}" var="ext">
						extras["${ext}"] = 1;
					</c:forEach>
					var children = obj.getElementsByTagName("input");
					for(var i=0; i<children.length; i++) {
						if (children[i].name == ("conceptModifier_" + count))
							children[i].checked = (children[i].value == '${column.modifier}');
						if (children[i].name == ("conceptExtra_" + count))
							children[i].checked = (extras[children[i].value] == 1);
					}
					getChildByName(obj, "conceptName_" + count).value = "${column.conceptName}";
				</c:if>
				<c:if test="${column.columnType == 'calculated'}">
					selectTab(getChildById(obj, 'calcTab'));
					getChildByName(obj, "calculatedName_" + count).value = "${column.columnName}";
					<c:forEach items="${fn:split(column.returnValue, linefeed)}" var="line" varStatus="varStatus">
						getChildByName(obj, "calculatedValue_" + count).value += "${line}" <c:if test="${varStatus.last != true}"> + '\n'</c:if>;
					</c:forEach>
					getChildByName(obj, "calculatedValue_" + count).value += '\n';
				</c:if>
			</c:forEach>
			
			btn = $('newPatientButton');
			searchType = "patient";
			DWREngine.setOrdered(true);
			<c:forEach items="${dataExport.patientIds}" var="id">
				var obj = addNew(btn, "newPatient");
				changeButton = getChildById(obj, "patientButton");
				DWRPatientService.getPatient(onSelect, '${id}');
			</c:forEach>
			DWREngine.setOrdered(false);
		</c:if>
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>