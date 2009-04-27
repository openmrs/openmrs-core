<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenance/globalProps.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<style>
 .descriptionBox {
 	border-color: transparent;
 	border-width: 1px;
 	overflow-y: auto;
 	background-color: transparent;
 	padding: 1px;
 	height: 2.7em;
 }
 td.description {
 	padding-top: 0px;
 }
 #buttonsAtBottom {
 	padding: 5px;
 }
</style>

<h2><spring:message code="GlobalProperty.manage.title"/></h2>	

<div id="settings-container" style="overflow: hidden">
<div id="settings-tree" style="float: left; width: 24%">
	<div style="background-color: #8FABC7; padding: 0.3em 0.5ex">
		<div><input type="text" value="modules filter" style="color: #aaa; font-size: 80%; width: 100%"/></div>
		<div style="color: #fff; font-weight: bold; margin-top: 0.2em">Settings</div>
	</div>
	<ul>
		<li class="all-settings">All Settings</li>
		<li class="system-settings">System Settings</li>
		<li class="modules-settings">
			<div>Modules Settings</div>
			<ul>
				<li class="module">addresshierarhy</li>
				<li class="module">childvcreport</li>
				<li class="module">flowsheet</li>
			</ul>
		</li>
	</ul>
</div>

<div id="settings-content" style="float: right; width: 75.5%">
	<div style="background-color: #8FABC7; padding: 0.3em 0.5ex 0">
		<div><input type="text" value="filter by name, type and description" style="color: #aaa; font-size: 80%; width: 100%"/></div>
	</div>
<form method="post" onsubmit="removeHiddenRows()">
	<table cellpadding="1" cellspacing="0" style="width: 100%;">
		<thead style="color: #fff; font-weight: bold; background-color: #8FABC7">
			<tr>
				<th style="padding: 0.2em 0.5ex"><spring:message code="general.name" /></th>
				<th style="padding: 0.2em 0.5ex"><spring:message code="general.value" /></th>

				<!-- TODO: Add general.type into messages -->
				<th style="padding: 0.2em 0.5ex"><spring:message code="general.type" /></th>

				<th>Description</th>
				<th></th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:forEach var="globalProp" items="${globalProps}" varStatus="status">
				<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
					<td>${globalProp.property}</td>
					<td>
						<!-- TODO: Replace text input to appropriate widget -->
						${globalProp.propertyValue}
					</td>
			
					<!-- TODO: Display real type name -->
					<td>
						<span>Type name</span>
					</td>
					<td valign="top" class="description">
						${globalProp.description}
					</td>
					<td valign="top"><input type="button" value='<spring:message code="general.remove" />' class="closeButton" onclick="edited(); remove(this)" /></td>
				</tr>
			</c:forEach>
			<tr id="newProperty">
				<td valign="top"><input type="text" name="property" size="50" maxlength="250" onchange="edited()" /></td>
				<td valign="top"><input type="text" name="value" size="30" maxlength="250" onchange="edited()" /></td>
				<td valign="top"><input type="text" name="type" size="30" maxlength="250" onchange="edited()" /></td>
				<td valign="top" class="description">
					<textarea name="description" class="descriptionBox" 
						rows="2" cols="96" onchange="edited()"
						onfocus="descriptionFocus(this)" onblur="descriptionBlur(this)"></textarea>
				</td>
				<td valign="top" rowspan="2"><input type="button" value='<spring:message code="general.remove" />' class="closeButton" onclick="remove(this)" /></td>
			</tr>
		</tbody>
	</table>
	
	<input type="button" onclick="addProperty()" class="smallButton" value='<spring:message code="GlobalProperty.add" />' />
	
	<script type="text/javascript">
		<!-- // begin
		document.getElementById('newProperty').style.display = 'none';
		document.getElementById('newPropertyDescription').style.display = 'none';
		addProperty(true);
		
		function edited() {
			document.getElementById('buttonsAtBottom').style.backgroundColor = 'LemonChiffon';
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
		
		function remove(btn) {
			var parent = btn.parentNode;
			while (parent.tagName.toLowerCase() != "tr")
				parent = parent.parentNode;
			var parentDesc = parent.nextSibling;
			if (!parentDesc.tagName || parentDesc.tagName.toLowerCase() != "tr")
				parentDesc = parentDesc.nextSibling;
			
			parent.style.display = parentDesc.style.display = "none";
			updateRowColors();
			edited();
		}
		
		function addProperty(startup) {
			var tbody = document.getElementById("globalPropsList");
			var tmpProp = document.getElementById("newProperty");
			var tmpDesc = document.getElementById("newPropertyDescription");
			var newProp = tmpProp.cloneNode(true);
			var newDesc = tmpDesc.cloneNode(true);
			newProp.style.display = newDesc.style.display = '';
			newProp.id = newDesc.id = '';
			
			//var inputs = newProp.getElementsByTagName("input");
			//for (var i=0; i< inputs.length; i++) 
			//	if (inputs[i].type == "text")
			//		inputs[i].value = "";
				
			tbody.appendChild(newProp);
			tbody.appendChild(newDesc);
			
			updateRowColors();
			
			if (!startup)
				edited();
		}
		
		function descriptionFocus(textarea) {
			textarea.style.borderColor = "cadetBlue";
		}
		
		function descriptionBlur(textarea) {
			textarea.style.borderColor = "transparent";
		}
		
		function updateRowColors() {
			var tbody = document.getElementById("globalPropsList");
			var alternator = 1;
			for (var i=0; i < tbody.rows.length; i++) {
				var propsRow = tbody.rows[i++];
				if (propsRow.style.display != "none") { // skip deleted rows
					var descRow = tbody.rows[i];
					propsRow.className = descRow.className = alternator < 0 ? "oddRow" : "evenRow";
					alternator = alternator * -1;
				}
			}
		}
		
		// end -->
	</script>

	<span id="buttonsAtBottom">
		<input type="submit" name="action" value='<spring:message code="general.save"/>' />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="submit" name="action" value='<spring:message code="general.cancel"/>' />
	</span>
</form>
</div> <!-- end settings-content -->
</div> <!-- end settings-container -->

<%@ include file="/WEB-INF/template/footer.jsp" %>