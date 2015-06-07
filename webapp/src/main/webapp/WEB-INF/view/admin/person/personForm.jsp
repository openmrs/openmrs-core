<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Person" otherwise="/login.htm" redirect="/admin/person/person.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/validation.js" />

<script>
	// Saves the last tab clicked on (aka "current" or "selected" tab)
	var lastTab = new Array();
	lastTab["name"]		  = null;
	lastTab["address"]	  = null;
	
	// Number of objects stored.  Needed for 'add new' purposes.
	// starts at -1 due to the extra 'blank' data div in the *Boxes dib
	var numObjs = new Array();
	numObjs["name"]			= -1;
	numObjs["address"]		= -1;
	
	function initializeChildren(obj, type) {
		if (obj.hasChildNodes()) {
			var child = obj.firstChild;
			while (child != null) {
				if (child.nodeName == "DIV") {
					child.style.display = "none";
					numObjs[type] = numObjs[type] + 1;
				}
				child = child.nextSibling;
			}
		}
	}
	
	function selectTab(tab, type) {
		if (tab != null && tab.id != null) {
			var data = document.getElementById(tab.id + "Data");
			if (data != null) {
				addClass(tab, 'selected');                              //set the tab as selected
				data.style.display = "";                                //show the data box
				if (lastTab[type] != null && lastTab[type] != tab) {    //if there was a last tab
					removeClass(lastTab[type], 'selected');             //set the last tab as unselected
					var lastData = document.getElementById(lastTab[type].id + "Data");
					if (lastData != null) 
						lastData.style.display = "none";                //hide last data tab
				}
				lastTab[type] = tab;             //new tab is now the last tab
				tab.blur();                      //get rid of the ugly dotted border the browser creates.
			}
		}
		return false;
	}
	
	function addNew(type) {
		var newData = document.getElementById(type + "Data");
		if (newData != null) {
			var tabToClone = document.getElementById(type + "Tab");
			var tabClone = tabToClone.cloneNode(true);
			tabClone.id = type + numObjs[type];
			tabClone.style.display = "";
			var parent = tabToClone.parentNode;
			parent.insertBefore(tabClone, tabToClone);

			var dataClone = newData.cloneNode(true);
			dataClone.id = type + numObjs[type] + "Data";
			parent = newData.parentNode;
			parent.insertBefore(dataClone, newData);
			
			//find the active checkbox and add an onclick listener to it
			//and assign names and ids to the start and end input fields
			var inputs = dataClone.getElementsByTagName("input");
			for (var i in inputs) {
				var input = inputs[i];
				if (input && input.name == "activeCheckbox") {
					var addressIndex = numObjs[type];
					input.checked = true;
					input.onclick = function(){
						updateEndDate(this, 'addresses[' + addressIndex + '].endDate');
					};
				}
				else if (input && input.name == "startDate")
					input.id = 'addresses[' + numObjs[type] + '].startDate';
				else if (input && input.name == "endDate"){
					input.id = 'addresses[' + numObjs[type] + '].endDate';
					input.disabled = 'disabled';
				}
			}

			numObjs[type] = numObjs[type] + 1;
		}

		return selectTab(tabClone, type);
	}
	
	function removeTab(obj, type) {
		var data = obj.parentNode;
		var tabId = data.id.substring(0, data.id.lastIndexOf("Data"));
		var tab = document.getElementById(tabId);
		var tabToSelect = null;			
		
		if (data != null && tab != null) {
			var tabparent = tab.parentNode;

			var sibling = tab.nextSibling;
			while (tabToSelect == null && sibling != tabparent.lastChild) {
				if (sibling.id == null || sibling.className == "addNew" || sibling.style.display == "none")
					sibling = sibling.nextSibling;
				else
					tabToSelect = sibling;
			}
			if (tabToSelect == null || tabToSelect == tabparent.lastChild) {
				sibling = tab.previousSibling;
				while (tabToSelect == null && sibling != tabparent.firstChild) {
					if (sibling.id == null || sibling.className == "addNew")
						sibling = sibling.previousSibling;
					else
						tabToSelect = sibling;
				}
			}
			
			if (tabToSelect != null && tabToSelect.id != null) {
				//only remove this node if it is not the last
				tabparent.removeChild(tab);
				var dataparent = data.parentNode;
				dataparent.removeChild(data);
			}
			
		}
		
		return selectTab(tabToSelect, type);
	}
	
	function modifyTab(obj, value, child) {
		var parent = obj.parentNode;
		while (parent.nodeName != "DIV") {
			parent = parent.parentNode;
		}
		var tabId = parent.id.substring(0, parent.id.lastIndexOf("Data"));  //strip 'Data' from div id
		var tab = document.getElementById(tabId);
		tab.childNodes[child].innerHTML = value;
	}
	
	function voidedBoxClicked(chk) {
		var parent = chk.parentNode;
		while (parent.id.indexOf("Data") == -1)
			parent = parent.parentNode;
		var tabId = parent.id.substring(0, parent.id.lastIndexOf("Data"));
		var tab = document.getElementById(tabId);
		if (chk.checked == true)
			addClass(tab, 'voided');
		else
			removeClass(tab, 'voided');
	}
	
	function removeBlankData() {
		var obj = document.getElementById("nameData");
		if (obj != null)
			obj.parentNode.removeChild(obj);
		obj = document.getElementById("addressData");
		if (obj != null)
			obj.parentNode.removeChild(obj);
	}
	
	function preferredBoxClick(obj) {
		var inputs = document.getElementsByTagName("input");
		if (obj.checked == true) {
			for (var i=0; i<inputs.length; i++) {
				var input = inputs[i];
				if (input.type == "checkbox")
					if (input.alt == obj.alt && input != obj)
						input.checked = false;
			}
		}
	}
	
	function toggleLocationBox(identifierType,location) {
		var idNum = location.match(/\d+$/)[0];
		var boxId = 'locationBox' + idNum;
		var naBoxId = 'locationNABox' + idNum;
		if (identifierType == '') {
			$j('#'+naBoxId).hide();
			$j('#'+boxId).hide();
		}
		else if (idTypeLocationRequired[identifierType]) {
			$j('#'+naBoxId).hide();
			$j('#'+boxId).show();
		} 
		else {
			$j('#'+boxId).hide();
			$j('#'+naBoxId).show();
		}
	}
</script>

<style>
	.tabBar {
		float: left;
		font-size: 11px;
		width: 158px;
		}
		.tabBar a {
			display: block;
			border-width: 2px;
			border-style: none solid none none;
			border-color: navy;
			background-color: WhiteSmoke;
			text-decoration: none;
			padding: 4px;
			}
			.tabBar a:hover {
				text-decoration: underline;
			}
		.tabBar .selected {
			border-width: 2px;
			border-style: solid none solid solid;
			border-color: navy;
		}
	.tabBoxes {
		margin-left: 156px;
		border: 2px solid navy;
		padding: 3px;
		min-height: 150px;
	}
	#pInformationBox .tabBoxes {
		margin-left: 1px;
	}
	.addNew, .removeTab {
		font-size: 10px;
		float: right;
		margin: 3px;
		cursor: pointer;
	}
	
</style>

<h2><openmrs:message code="Person.title"/></h2>

<c:if test="${person.dead}">
	<div id="personFormDeceased" class="retiredMessage">
		<div><openmrs:message code="Person.personDeceased"/></div>
	</div>
</c:if>

<openmrs:hasPrivilege privilege="Delete Person">
<c:if test="${person.personVoided}">
	<div id="personFormVoided" class="retiredMessage">
	<div><openmrs:message code="Person.voidedMessage"/></div>
    <div>
    	<c:if test="${person.personVoidedBy.personName != null}"><openmrs:message code="general.byPerson"/> <c:out value="${person.personVoidedBy.personName}" /></c:if>
    	<c:if test="${person.personDateVoided != null}"> <openmrs:message code="general.onDate"/> <openmrs:formatDate date="${person.personDateVoided}" type="long" /> </c:if> 
   		<c:if test="${person.personVoidReason != ''}"> - ${person.personVoidReason} </c:if>
    </div>
	<div>
		<form action="" method="post" ><input type="submit" name="action" value="<openmrs:message code="Person.unvoid"/>" /></form></div> 
	</div>
</c:if>
</openmrs:hasPrivilege>

<spring:hasBindErrors name="person">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form method="post" onSubmit="removeBlankData()">
	
	<h3><openmrs:message code="Person.names"/></h3>
		<spring:hasBindErrors name="person.names">
			<span class="error">${error.errorMessage}</span><br/>
		</spring:hasBindErrors>
		<div id="pNames">
			<div class="tabBar" id="pNameTabBar">
				<c:forEach var="name" items="${person.names}" varStatus="varStatus">
					<a href="javascript:return false;" onClick="return selectTab(this, 'name');" id="name${varStatus.index}" <c:if test="${name.voided}">class='voided'</c:if>><span><c:out value="${name.givenName}" /></span>&nbsp;<span><c:out value="${name.familyName}" /></span></a>
				</c:forEach>
				<a href="javascript:return false;" onClick="return selectTab(this, 'name');" id="nameTab" style="display: none"><span></span>&nbsp;<span></span></a>
				<input type="button" onClick="return addNew('name');" class="addNew" id="name" value='<openmrs:message code="Person.addNewName"/>'/>
			</div>
			<div class="tabBoxes" id="nameDataBoxes">
				<c:forEach var="name" items="${person.names}" varStatus="varStatus">
					<spring:nestedPath path="person.names[${varStatus.index}]">
						<div id="name${varStatus.index}Data" class="tabBox">
							<openmrs:portlet url="nameLayout" id="namePortlet" size="full" parameters="layoutShowTable=true|layoutShowExtended=true|layoutHideVoidOption=${(name.personNameId == null)}" />
							<!-- <input type="button" onClick="return removeTab(this, 'name');" class="removeTab" value='<openmrs:message code="Person.removeThisName"/>'/><br/> --> <br/>
						</div>
					</spring:nestedPath>
				</c:forEach>
				<div id="nameData" class="tabBox">
					<spring:nestedPath path="emptyName">
						<openmrs:portlet url="nameLayout" id="namePortlet" size="full" parameters="layoutShowTable=true|layoutShowExtended=true|layoutHideVoidOption=true" />
						<!-- <input type="button" onClick="return removeTab(this, 'name');" class="removeTab" value='<openmrs:message code="Person.removeThisName"/>'/><br/> --> <br/>
					</spring:nestedPath>
				</div>
			</div>
		</div>
	
	<br style="clear: both" />
	
	<h3><openmrs:message code="Person.addresses"/></h3>
		<spring:hasBindErrors name="person.addresses">
			<span class="error">${error.errorMessage}</span><br/>
		</spring:hasBindErrors>
		<div id="pAddresses">
			<div class="tabBar" id="pAddressesTabBar">
				<c:forEach var="address" items="${person.addresses}" varStatus="varStatus">
					<a href="javascript:return false;" onClick="return selectTab(this, 'address');" id="address${varStatus.index}" <c:if test="${address.voided}">class='voided'</c:if>><span>${address.cityVillage}</span>&nbsp;</a>
				</c:forEach>
				<a href="javascript:return false;" onClick="return selectTab(this, 'address');" id="addressTab" style="display: none"><span></span>&nbsp;</a>
				<input type="button" onClick="return addNew('address');" class="addNew" id="address" value='<openmrs:message code="Person.addNewAddress"/>'/>			
			</div>
			<div class="tabBoxes" id="addressDataBoxes">
				<c:forEach var="address" items="${person.addresses}" varStatus="varStatus">
					<spring:nestedPath path="person.addresses[${varStatus.index}]">
						<div id="address${varStatus.index}Data" class="tabBox">
							<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=true|layoutShowExtended=true|layoutHideVoidOption=${(address.personAddressId == null)}|isNew=${(address.personAddressId == null)}" />
							<%-- @ include file="include/editPersonAddress.jsp" --%>
							<!-- <input type="button" onClick="return removeTab(this, 'name');" class="removeTab" value='<openmrs:message code="Person.removeThisAddress"/>'/><br/> --> <br/>
						</div>
					</spring:nestedPath>
				</c:forEach>
				<div id="addressData" class="tabBox">
					<spring:nestedPath path="emptyAddress">
						<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=true|layoutShowExtended=true|layoutHideVoidOption=true|isNew=true" />
						<!-- <input type="button" onClick="return removeTab(this, 'name');" class="removeTab" value='<openmrs:message code="Person.removeThisAddress"/>'/><br/> --> <br/>
					</spring:nestedPath>
				</div>
			</div>
		</div>
	
	<br/>
	
	<h3><openmrs:message code="Person.information"/></h3>
		<div class="tabBox" id="pInformationBox">
			<div class="tabBoxes">
				<table>
					<spring:nestedPath path="person">
						<%@ include file="../person/include/editPersonInfo.jsp" %>
					</spring:nestedPath>
				</table>
			</div>
		</div>	

	<br />
	<spring:bind path="person.personId">
		<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
	</spring:bind>
	
	<input type="submit" name="action" id="saveButton" value='<openmrs:message code="Person.save"/>' />
	
	<c:if test="${person.personId != null}">
	<openmrs:hasPrivilege privilege="Purge Person">
		&nbsp; &nbsp; &nbsp;
		<span style="position: relative">
			<input type="button" id="deletePersonButton" value="<openmrs:message code="Person.delete"/>" onClick="showDiv('deletePersonDiv'); hideDiv('deletePersonButton')"/>
			<div id="deletePersonDiv" style="position: absolute; padding: 1em; bottom: 0px; left: 0px; z-index: 9; width: 350px; border: 1px black solid; background-color: #ffff88; display: none">
				<openmrs:message htmlEscape="false" code="Person.delete.warningMessage"/>
				<br/><br/>
				<div align="center">
					<input type="submit" name="action" value="<openmrs:message code="Person.delete"/>" onclick="return confirm('<openmrs:message code="Person.delete.finalWarning"/>')"/>
					&nbsp; &nbsp; &nbsp;
					<input type="button" value="<openmrs:message code="general.cancel" />" onClick="showDiv('deletePersonButton'); hideDiv('deletePersonDiv')"/>
				</div>
			</div>
		</span>
	</openmrs:hasPrivilege>
</c:if>
	
</form>
<br/>
<openmrs:hasPrivilege privilege="Delete Person">
	<c:if test="${person.personId != null && person.personVoided == false}">
	<form action="" method="post">
		<fieldset>
			<legend><h4><openmrs:message code="Person.void"/></h4></legend>
			<b><openmrs:message code="general.reason"/></b>
			<input type="text" value="" size="50" name="voidReason" />
			<br/><br/>
			<input type="submit" value='<openmrs:message code="Person.void"/>' name="action"/>
		</fieldset>
	</form>	
	</c:if>
</openmrs:hasPrivilege>
	
<script>
	
	var array = new Array(2);
	array[0] = "name";
	array[1] = "address";
	for (var i = 0; i < array.length; i ++) {
		var id = array[i];
		var dataBoxes = document.getElementById(id + "DataBoxes");
		initializeChildren(dataBoxes, id);
		if (numObjs[id] < 1) {
			addNew(id);
		}
		selectTab(document.getElementById(id + "0"), id);
	}
	
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>