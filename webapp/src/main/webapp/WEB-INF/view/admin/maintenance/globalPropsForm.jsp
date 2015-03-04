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
	border-color: black;
	border-style: solid;
	border-width: 1px;
	padding: 5px;
	position: fixed;
	right: 0.5em;
	bottom: 1em;
	background-color: #FFFF66;
	display: none;
	text-align: center;
  }
  
  td#message {
  	background-color: #FFFFFF;
  	text-align: center;
  	font-style: bold;	
  }
</style>

<h2><openmrs:message code="GlobalProperty.manage.title"/></h2>	

<b class="boxHeader"><openmrs:message code="GlobalProperty.list.title"/></b>
<form method="post" class="box" onsubmit="removeHiddenRows()">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th><openmrs:message code="general.name" /></th>
				<th><openmrs:message code="general.value" /></th>
				<th></th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:forEach var="globalProp" items="${globalProps}" varStatus="status">
				<tr class="${status.index % 2 == 0 ? 'evenRow' : 'oddRow' }">
					<td valign="top"><input type="text" name="property" value="${globalProp.property}" size="50" maxlength="250" onkeyup="edited()" /></td>
					<td valign="top">
						<c:choose>
							<c:when test="${fn:length(globalProp.propertyValue) > 20}">
								<textarea name="value" onkeyup="edited()" rows="1" cols="60" wrap="off">${globalProp.propertyValue}</textarea>
							</c:when>
							<c:otherwise>
								<input type="text" name="value" value="${globalProp.propertyValue}" size="30" maxlength="4000" onkeyup="edited()" />
							</c:otherwise>
						</c:choose>
					</td>
					<td valign="top" rowspan="2"><input type="button" align="right" value='<openmrs:message code="general.remove" />' class="closeButton" onclick="edited(); removeProperty(this)" /> </td> 
					<td id="message" valign="top" rowspan="2"></td>
				</tr>
				<tr class="${status.index % 2 == 0 ? 'evenRow' : 'oddRow' }">
					<td colspan="2" valign="top" class="description">
						<textarea name="description" class="descriptionBox" 
							rows="2" cols="96" onkeyup="edited()"
							onfocus="descriptionFocus(this)" onblur="descriptionBlur(this)">${globalProp.description}</textarea>
					</td>
				</tr>
			</c:forEach>
			<tr id="newProperty">
				<td valign="top"><input type="text" name="property" size="50" maxlength="250" onkeyup="edited()" /></td>
				<td valign="top"><input type="text" name="value" size="30" maxlength="250" onkeyup="edited()" /></td>
				<td valign="top" rowspan="2"><input type="button" value='<openmrs:message code="general.remove" />' class="closeButton" onclick="removeProperty(this)" /></td>
			</tr>
			<tr id="newPropertyDescription">
					<td colspan="2" valign="top" class="description">
						<textarea name="description" class="descriptionBox" 
							rows="2" cols="96" onkeyup="edited()"
							onfocus="descriptionFocus(this)" onblur="descriptionBlur(this)"></textarea>
					</td>
				</tr>
		</tbody>
	</table>
	
	<input type="button" onclick="addProperty()" class="smallButton" value='<openmrs:message code="GlobalProperty.add" />' />
	
	<br /><br />
	
	<script type="text/javascript">
		<!-- // begin
		document.getElementById('newProperty').style.display = 'none';
		document.getElementById('newPropertyDescription').style.display = 'none';
		addProperty(true);
		
		function edited() {
			document.getElementById('buttonsAtBottom').style.backgroundColor = '#FFFF66';
			document.getElementById('buttonsAtBottom').style.display = 'block';
		}
		
		function removeHiddenRows() {
			var rows = document.getElementsByTagName("TR");
			var i = 0;
			while (i < rows.length) {
				if (rows[i].getAttribute('deleted') == "true") {
					rows[i].parentNode.removeChild(rows[i]);
				}
				else {
					i = i + 1;
				}
			}
		}
		
		function removeProperty(btn) {
			if(btn.getAttribute("remove") == null){
				var parent = btn.parentNode;
				if(parent.tagName.toLowerCase() == "td"){
					$j(parent).next().text("<openmrs:message code="GlobalProperty.toDelete" />");					
				}
				
				while (parent.tagName.toLowerCase() != "tr")
					parent = parent.parentNode;
					var parentDesc = parent.nextSibling;
					if (!parentDesc.tagName || parentDesc.tagName.toLowerCase() != "tr")
						parentDesc = parentDesc.nextSibling;
							
						parentDesc.setAttribute("deleted","true");
						parent.setAttribute("deleted","true");
						parent.style.backgroundColor = parentDesc.style.backgroundColor = "#D2C5C8";
						btn.value ="<openmrs:message code="general.restore" />";
						btn.style.backgroundColor = '#4AA02C';
							
						btn.setAttribute("remove", "true");
							
			} else if(btn.getAttribute("remove") == "true"){
				var parent = btn.parentNode;
								
					if(parent.tagName.toLowerCase() == "td"){
						$j(parent).next().text("");
					}			
				while (parent.tagName.toLowerCase() != "tr")
					parent = parent.parentNode;
					var parentDesc = parent.nextSibling;
						if (!parentDesc.tagName || parentDesc.tagName.toLowerCase() != "tr")
							parentDesc = parentDesc.nextSibling;
								
					parentDesc.setAttribute("deleted","false");
					parent.setAttribute("deleted","false");
					btn.value ="<openmrs:message code="general.remove" />";
					btn.style.backgroundColor = 'lightPink';
					btn.removeAttribute("remove");	
					parent.style.backgroundColor = parentDesc.style.backgroundColor = null;
					
			}
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
				if (propsRow.style.backgroundColor != "#D2C5C8") { // skip deleted rows
					var descRow = tbody.rows[i];
					propsRow.className = descRow.className = alternator < 0 ? "oddRow" : "evenRow";
					alternator = alternator * -1;
				}
			}
		}
		
		// end -->
	</script>

	<div id="buttonsAtBottom">
	 <br/>
	 <openmrs:message htmlEscape="false" code="general.changes.toSave"/>
	 <br/>
	 <br/>
		&nbsp;&nbsp;
		<input type="submit" name="action" value='<openmrs:message code="general.save"/>' />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="button" value='<openmrs:message code="general.cancel"/>' onclick="location.reload(true);"/>
	</div>
	<div style="clear:both"></div>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
