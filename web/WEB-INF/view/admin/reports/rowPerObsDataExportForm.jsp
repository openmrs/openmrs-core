<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Data Exports" otherwise="/login.htm" redirect="/admin/reports/rowPerObsDataExport.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<% pageContext.setAttribute("linefeed", "\r\n"); %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	var searchWidget;
	
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
			
			id = newSibling.id.substr(newSibling.id.indexOf("_")+1, 3);
			var newSelect = getChildByName(newSibling, "cohortIdValue_" + id);
			id = sibling.id.substr(sibling.id.indexOf("_")+1, 3);
			var oldSelect = getChildByName(sibling, "cohortIdValue_" + id);
			newSelect.value = oldSelect.value;
			id = newColumn.id.substr(newColumn.id.indexOf("_")+1, 3);
			newSelect = getChildByName(newColumn, "cohortIdValue_" + id);
			id = column.id.substr(column.id.indexOf("_")+1, 3);
			oldSelect = getChildByName(column, "cohortIdValue_" + id);
			newSelect.value = oldSelect.value;
			
			id = newSibling.id.substr(newSibling.id.indexOf("_")+1, 3);
			var newSelect = getChildByName(newSibling, "filterIdValue_" + id);
			id = sibling.id.substr(sibling.id.indexOf("_")+1, 3);
			var oldSelect = getChildByName(sibling, "filterIdValue_" + id);
			newSelect.value = oldSelect.value;
			id = newColumn.id.substr(newColumn.id.indexOf("_")+1, 3);
			newSelect = getChildByName(newColumn, "filterIdValue_" + id);
			id = column.id.substr(column.id.indexOf("_")+1, 3);
			oldSelect = getChildByName(column, "filterIdValue_" + id);
			newSelect.value = oldSelect.value;
			
			id = newSibling.id.substr(newSibling.id.indexOf("_")+1, 3);
			var newSelect = getChildByName(newSibling, "patientSearchIdValue_" + id);
			id = sibling.id.substr(sibling.id.indexOf("_")+1, 3);
			var oldSelect = getChildByName(sibling, "patientSearchIdValue_" + id);
			newSelect.value = oldSelect.value;
			id = newColumn.id.substr(newColumn.id.indexOf("_")+1, 3);
			newSelect = getChildByName(newColumn, "patientSearchIdValue_" + id);
			id = column.id.substr(column.id.indexOf("_")+1, 3);
			oldSelect = getChildByName(column, "patientSearchIdValue_" + id);
			newSelect.value = oldSelect.value;

			parent.replaceChild(newColumn, sibling);
			parent.replaceChild(newSibling, column);
			updateColumnClasses(newColumn);
		}
	}
	
	function trim(stringToTrim) {
		return stringToTrim.replace(/^\s+|\s+$/g,"");
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
				input.value = trim(opt.text);
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
	
	function setValueHelper(htmlEl, val) {
		if (htmlEl.id != null && htmlEl.id != '')
			DWRUtil.setValue(htmlEl.id, val);
		else
			DWRUtil.setValue(htmlEl.name, val);
	}
	
	function updateCohortColumn(sel) {
		if (sel.value != "") {
			var count = sel.name.substr(sel.name.indexOf("_")+1, 3);
			var val = DWRUtil.getValue(sel.name);
			var temp = val.split(".");
			var opt = sel.options[sel.selectedIndex];
			var tbl = getParentByTagName(sel, "table");
			setValueHelper(getChildByName(tbl, "cohortName_" + count), opt.text);
			setValueHelper(getChildByName(tbl, "cohortIfTrue_" + count), opt.text);
			setValueHelper(getChildByName(tbl, "cohortIfFalse_" + count), '');
			if (temp[0] == 'C') {
				setValueHelper(getChildByName(tbl, "cohortIdValue_" + count), val);
				setValueHelper(getChildByName(tbl, "filterIdValue_" + count), '');
				setValueHelper(getChildByName(tbl, "patientSearchIdValue_" + count), '');
			} else if (temp[0] == 'F') {
				setValueHelper(getChildByName(tbl, "cohortIdValue_" + count), '');
				setValueHelper(getChildByName(tbl, "filterIdValue_" + count), val);
				setValueHelper(getChildByName(tbl, "patientSearchIdValue_" + count), '');
			} else if (temp[0] == 'S') {
				setValueHelper(getChildByName(tbl, "cohortIdValue_" + count), '');
				setValueHelper(getChildByName(tbl, "filterIdValue_" + count), '');
				setValueHelper(getChildByName(tbl, "patientSearchIdValue_" + count), val);
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
	function addNew(button, id, objId) {
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
		//newObj.parentNode = button.parentNode;
		
		var props = new Array();
		if (id == "newPatient") {
			// create patient search
			var obj = getChildById(newObj, "patientSearch");
			if (objId)
				props.patientId = objId;
			var pSearch = dojo.widget.createWidget("PatientSearch", props, obj);
			
			// create patient popup
			props.widgetId = "pSelection" + "_" + count;
			props.searchWidget = pSearch.widgetId;
			props.hiddenInputName = "patientId";
			obj = pSearch.domNode.parentNode;
			var pSelection = dojo.widget.createWidget("OpenmrsPopup", props, obj);
			dojo.event.connect(pSearch, "doSelect", pSelect(pSelection), "select");
			
			// add 'remove' button
			var removeButton = document.createElement("input");
			removeButton.type="button";
			removeButton.value=" X ";
			removeButton.className="closeButton";
			removeButton.style.cssFloat="none";
			removeButton.onclick=function() { this.parentNode.parentNode.removeChild(this.parentNode); }
			pSelection.domNode.insertBefore(removeButton, pSelection.domNode.firstChild);
		}
		else if (id == "newColumn") {
			// create concept search
			var obj = getChildById(newObj, "conceptSearch");
			props.showVerboseListing = true;
			var cSearch = dojo.widget.createWidget("ConceptSearch", props, obj);
			
			// create concept popup
			props.widgetId = "cSelection" + "_" + count;
			props.searchWidget = cSearch.widgetId;
			props.hiddenInputName = "conceptId";
			props.searchTitle='<spring:message code="general.search"/>';
			obj = cSearch.domNode.parentNode;
			var cSelection = dojo.widget.createWidget("OpenmrsPopup", props, obj);
			dojo.event.connect(cSearch, "doSelect", cSelect(cSelection), "select");
			
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
		var widget = dojo.widget.manager.getWidgetById("cSelection" + suffix);
		widget.hiddenInputNode.name += suffix;
		//getChildByName(obj, "conceptButton").name += suffix;
		
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
		getChildByName(obj, "conceptModifierNum").name += suffix;
		getChildByName(obj, "calculatedName").name += suffix;
		getChildByName(obj, "calculatedValue").name += suffix;
		getChildByName(obj, "calculatedPatient").name += suffix;
		getChildByName(obj, "cohortName").name += suffix;
		getChildByName(obj, "cohortIdValue").name += suffix;
		getChildByName(obj, "filterIdValue").name += suffix;
		getChildByName(obj, "patientSearchIdValue").name += suffix;
		getChildByName(obj, "cohortIfTrue").name += suffix;
		getChildByName(obj, "cohortIfFalse").name += suffix;
	}
	
	var pSelect = function(p) { return {
			popup: p,
			select: function(msg) {
				str = msg.objs[0].givenName;
				str += ' ';
				str += msg.objs[0].middleName;
				str += ' ';
				str += msg.objs[0].familyName;
				this.popup.displayNode.innerHTML = str;
				this.popup.hiddenInputNode.value = msg.objs[0].patientId;
				}
			}
		};
	var cSelect = function(p) { return {
			popup: p,
			select: function(msg) {
				var obj = msg;
				if (msg.objs)
					obj = msg.objs[0];
				var id = this.popup.hiddenInputNode.name.substr(this.popup.hiddenInputNode.name.indexOf("_")+1, 3);
				var name = getChildByName(this.popup.domNode.parentNode.parentNode, "conceptColumnName_" + id);
				if (name && name.value == "")
					name.value = obj.name;
				
				this.popup.displayNode.innerHTML = obj.name;
				this.popup.hiddenInputNode.value = obj.conceptId;
				}
			}
		};
	
	dojo.addOnLoad( function() {
		propertySetup();
	});

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
			var txt = document.createTextNode(obj);
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
	
	function ensureName() {
		var name = DWRUtil.getValue('dataExportName');
		if (name == null || name == '') {
			window.alert('<spring:message code="error.name" />');
			return false;
		}
		return true;
	}
	
	/** This method is used by the firstNum and mostRecentNum input boxes to show the text input 
	* for the number directly after the radio button of that which was clicked
	*/
	function showNum(thisObj) {
		parent = thisObj.parentNode;
		var numInputBox = parent.firstChild;
		// get the input box we're moving around
		do {
			if (numInputBox.name && numInputBox.name.indexOf("conceptModifierNum") == 0)
				break;
			numInputBox = numInputBox.nextSibling;
		} while (numInputBox != null);
		
		// get the span tag following the radio box
		var nextSpanTag = thisObj.nextSibling;
		if (nextSpanTag.tagName == null || nextSpanTag.tagName.toUpperCase() != "SPAN")
			nextSpanTag = nextSpanTag.nextSibling;
		
		if (numInputBox != null && numInputBox != nextSpanTag) {
			parent.removeChild(numInputBox);
			parent.insertBefore(numInputBox, nextSpanTag.nextSibling);
		}
	}
	
</script>

<style>
	th { text-align: left; }
	
	.addNew {
		font-size: 10px;
		margin: 3px;
		cursor: pointer;
	}
	
	.tabBar {
		margin-left: auto;
		margin-right: auto;
		width: 98%;
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
	
	/* hide the modifiers option on the concept columns */
	.conceptColumnModifier {
		display: none;
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

<form method="post" onSubmit="removeHiddenDivs(); return ensureName()">
<table>
	<tr>
		<th><spring:message code="general.name"/></th>
		<td colspan="5">
			<spring:bind path="dataExport.name">
				<input type="text" id="dataExportName" name="name" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="general.description"/></th>
		<td valign="top" colspan="5">
			<spring:bind path="dataExport.description">
				<textarea name="description" rows="2" cols="40" type="_moz">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<%--
	<c:if test="${!(dataExport.creator == null)}">
		<tr>
			<th><spring:message code="general.createdBy" /></th>
			<td>
				${dataExport.creator.personName} -
				<openmrs:formatDate date="${dataExport.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	--%>
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
				<th colspan="2"><spring:message code="DataExport.cohortMatch"/></th>
			</tr>
			<tr>
				<td><spring:message code="CohortBuilder.savedSearches"/></td>
				<td>
					<spring:bind path="dataExport.patientSearchId">
						<select name="patientSearchId">
							<option value=""></option>
							<openmrs:forEachRecord name="reportObject" reportObjectType="Patient Search">
								<option value="${record.reportObjectId}" <c:if test="${status.value == record.reportObjectId}">selected</c:if>>${record.name}</option>
							</openmrs:forEachRecord>
						</select>
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td colspan="2" align="center"><spring:message code="general.andOr"/></td>
			</tr>
			<tr>
				<td><spring:message code="Cohort.title"/></td>
				<td>
					<spring:bind path="dataExport.cohortId">
						<select name="cohortId">
							<option value=""></option>
							<openmrs:forEachRecord name="cohort">
								<option value="${record.cohortId}" <c:if test="${status.value == record.cohortId}">selected</c:if>>${record.name}</option>
							</openmrs:forEachRecord>
						</select>
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td colspan="2" align="center"><spring:message code="general.andOr"/></td>
			</tr>
			<tr>
				<td><spring:message code="CohortDefinition.title"/></td>
				<td>
					<spring:bind path="dataExport.cohortDefinitionId">
						<select name="cohortDefinitionId">
							<option value=""></option>
							<openmrs:forEachRecord name="reportObject" reportObjectType="Patient Filter">
								<option value="${record.reportObjectId}" <c:if test="${status.value == record.reportObjectId}">selected</c:if>>${record.name}</option>
							</openmrs:forEachRecord>
						</select>
					</spring:bind>
				</td>
			</tr>
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
			<tr>
				<th colspan="2"><spring:message code="DataExport.patientMatch"/></th>
			</tr>
			<tr>
				<td colspan="2">
					<div id="newPatient" style="display: none">
						<div id="patientSearch">
							<!-- dojo search widget/popup are added here via the addNew() function -->
						</div>
					</div>
					<input type="button" onClick="return addNew(this, 'newPatient');" class="addNew" id="newPatientButton" value='<spring:message code="DataExport.addPatient" />' />
					<br/>
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
				<a id="cohortTab" class="tab" href="#selectCohortTab" onclick="selectTab(this)"><spring:message code="DataExport.cohortTab"/></a>
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
				<spring:message code="RowPerObsDataExport.onlyOneConcept" /><br/>
				<%@ include file="include/conceptColumns.jsp" %>
			</div>
			<div id="calc" class="box">
				<%@ include file="include/calculatedColumns.jsp" %>
			</div>
			<div id="cohort" class="box">
				<%@ include file="include/cohortColumns.jsp" %>
			</div>
		</div>
		<input type="button" onClick="return addNew(this, 'newColumn');" class="addNew" id="newColumnButtom" value='<spring:message code="DataExport.addColumn" />' />
	</div>
	
</div>

<c:if test="${dataExport.reportObjectId != null}">
	<br />
	<input type="checkbox" id="saveAsNew" name="saveAsNew" value="1" /><label for="saveAsNew"><spring:message code="DataExport.saveAs"/></label>
</c:if>
<br />
<input type="submit" name="action" value='<spring:message code="DataExport.save"/>'>
<!-- <input type="submit" name="action" value='<spring:message code="DataExport.saveGenerate"/>' onclick="redirectPage('dataExport.list')"> -->
</form>

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
					getChildByName(obj, "simpleValue_" + count).value = "<spring:message javaScriptEscape="true" text="${column.returnValue}" />";
				</c:if>
				<c:if test="${column.columnType == 'concept'}">
					selectTab(getChildById(obj, 'conceptTab'));
					getChildByName(obj, "conceptColumnName_" + count).value = "${column.columnName}";
					getChildByName(obj, "conceptModifierNum_" + count).value = "${column.modifierNum}";
					var extras = new Array();
					<c:forEach items="${column.extras}" var="ext">
						extras["${ext}"] = 1;
					</c:forEach>
					var children = obj.getElementsByTagName("input");
					for(var i=0; i<children.length; i++) {
						if (children[i].name == ("conceptModifier_" + count)) {
							if (children[i].value == '${column.modifier}') {
								children[i].checked = true;
								children[i].click();
							}
							else
								children[i].checked = false;
						}
						if (children[i].name == ("conceptExtra_" + count))
							children[i].checked = (extras[children[i].value] == 1);
					}
					var widget = dojo.widget.manager.getWidgetById("cSelection_" + count);
					if ("${column.conceptId}" != "") {
						widget.hiddenInputNode.value = "${column.conceptId}";
						DWRConceptService.getConcept(widget.searchWidget.simpleClosure(new cSelect(widget), "select"), "${column.conceptId}");
					}
					else { 
						// left for backwards compatibility to pre 1.0.43
						widget.hiddenInputNode.value = "${column.conceptName}";
						widget.displayNode.innerHTML = "${column.conceptName}";
					}
					
				</c:if>
				<c:if test="${column.columnType == 'calculated'}">
					selectTab(getChildById(obj, 'calcTab'));
					getChildByName(obj, "calculatedName_" + count).value = "${column.columnName}";
					<c:forEach items="${fn:split(column.returnValue, linefeed)}" var="line" varStatus="varStatus">
						getChildByName(obj, "calculatedValue_" + count).value += "${line}" <c:if test="${varStatus.last != true}"> + '\n'</c:if>;
					</c:forEach>
					getChildByName(obj, "calculatedValue_" + count).value += '\n';
				</c:if>
				<c:if test="${column.columnType == 'cohort'}">
					selectTab(getChildById(obj, 'cohortTab'));
					getChildByName(obj, "cohortName_" + count).value = "${column.columnName}";
					getChildByName(obj, "cohortIdValue_" + count).value = "${column.cohortId}";
					getChildByName(obj, "filterIdValue_" + count).value = "${column.filterId}";
					getChildByName(obj, "patientSearchIdValue_" + count).value = "${column.patientSearchId}";
					getChildByName(obj, "cohortIfTrue_" + count).value = "${column.valueIfTrue}";
					getChildByName(obj, "cohortIfFalse_" + count).value = "${column.valueIfFalse}";
				</c:if>
			</c:forEach>
			
			DWREngine.setOrdered(true);
			var btn = $('newPatientButton');
			<c:forEach items="${dataExport.patientIds}" var="id">
				addNew(btn, "newPatient", '${id}');
			</c:forEach>
			DWREngine.setOrdered(false);
		</c:if>
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>