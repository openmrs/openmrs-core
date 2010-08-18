<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Modules" otherwise="/login.htm" redirect="/admin/modules/moduleConfiguration.form" />

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

<c:choose>
<c:when test="${moduleName != ''}">
<h2><spring:message code="Module.configuration.title" arguments="${moduleName}"/></h2>	

<b class="boxHeader"><spring:message code="GlobalProperty.list.title"/></b>
<form method="post" class="box" onsubmit="removeHiddenRows()">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.value" /></th>
				<th></th>
			</tr>
		</thead>
		<tbody id="moduleConfigsList">
			<c:forEach var="moduleConfig" items="${moduleConfigurations}" varStatus="status">
				<tr class="${status.index % 2 == 0 ? 'evenRow' : 'oddRow' }">
					<td valign="top"><input type="text" name="property" value="${moduleConfig.property}" size="50"/></td>
					<td valign="top">
						<c:choose>
							<c:when test="${fn:length(moduleConfig.propertyValue) > 20}">
								<textarea name="value" onchange="edited()" rows="1" cols="60" wrap="off">${moduleConfig.propertyValue}</textarea>
							</c:when>
							<c:otherwise>
								<input type="text" name="value" value="${moduleConfig.propertyValue}" size="30" maxlength="4000" onchange="edited()" />
							</c:otherwise>
						</c:choose>
					</td>
					<!--<td valign="top" rowspan="2"><input type="button" value='<spring:message code="general.remove" />' class="closeButton" onclick="edited(); remove(this)" /></td>-->
				</tr>
				<tr class="${status.index % 2 == 0 ? 'evenRow' : 'oddRow' }">
					<td colspan="2" valign="top" class="description">
						<textarea name="description" class="descriptionBox" 
							rows="2" cols="96" onchange="edited()"
							onfocus="descriptionFocus(this)" onblur="descriptionBlur(this)">${moduleConfig.description}</textarea>
					</td>
				</tr>
			</c:forEach>
			<!--<tr id="newProperty">
				<td valign="top"><input type="text" name="property" size="50" maxlength="250" onchange="edited()" /></td>
				<td valign="top"><input type="text" name="value" size="30" maxlength="250" onchange="edited()" /></td>
				<td valign="top" rowspan="2"><input type="button" value='<spring:message code="general.remove" />' class="closeButton" onclick="remove(this)" /></td>
			</tr>
			<tr id="newPropertyDescription">
					<td colspan="2" valign="top" class="description">
						<textarea name="description" class="descriptionBox" 
							rows="2" cols="96" onchange="edited()"
							onfocus="descriptionFocus(this)" onblur="descriptionBlur(this)"></textarea>
					</td>
				</tr>-->
		</tbody>
	</table>
	
	<!--<input type="button" onclick="addProperty()" class="smallButton" value='<spring:message code="moduleConfigerty.add" />' />-->
	
	<br /><br />
	
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
			var tbody = document.getElementById("moduleConfigsList");
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
			var tbody = document.getElementById("moduleConfigsList");
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
</c:when>
<c:otherwise>
	<spring:message code="Module.configuration.notFound"/> 
	<div style="clear:both">&nbsp;</div>
	<a href="module.list"><spring:message code="general.back"/></a>
</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>