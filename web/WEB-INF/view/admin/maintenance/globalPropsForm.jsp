<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenace/globalProps.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="GlobalProperty.manage.title"/></h2>	

<b class="boxHeader"><spring:message code="GlobalProperty.list.title"/></b>
<form method="post" class="box" onsubmit="removeHiddenRows()">
	<table>
		<thead>
			<tr>
				<th> <spring:message code="general.name" /> </th>
				<th> <spring:message code="general.value" /> </th>
				<th> </th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:forEach var="globalProp" items="${globalProps}">
				<tr>
					<td valign="top"><input type="text" name="property" value="${globalProp.property}" size="50" maxlength="250" onchange="edited()" /></td>
					<td valign="top"><input type="text" name="value" value="${globalProp.propertyValue}" size="30" maxlength="250" onchange="edited()" /></td>
					<td><input type="button" value='<spring:message code="general.remove" />' class="closeButton" onclick="edited(); remove(this)" /></td>
				</tr>
			</c:forEach>
			<tr id="newProperty">
				<td valign="top"><input type="text" name="property" size="50" maxlength="250" onchange="edited()" /></td>
				<td valign="top"><input type="text" name="value" size="30" maxlength="250" onchange="edited()" /></td>
				<td><input type="button" value='<spring:message code="general.remove" />' class="closeButton" onclick="remove(this)" /></td>
			</tr>
		</tbody>
	</table>
	
	<input type="button" onclick="addProperty()" class="smallButton" value='<spring:message code="GlobalProperty.add" />' />
	
	<br /><br />
	
	<script type="text/javascript">
		<!-- // begin
		document.getElementById('newProperty').style.display = 'none';
		addProperty();
		
		var anyEdits = false;
		function edited() {
			if (!anyEdits) {
				document.getElementById('buttonsAtBottom').style.backgroundColor = 'yellow';
			}
			anyEdits = true;
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
			
			parent.style.display = "none";
		}
		
		function addProperty() {
			var tbody = document.getElementById("globalPropsList");
			var tmp = document.getElementById("newProperty");
			var newProp = tmp.cloneNode(true);
			newProp.style.display = '';
			
			var inputs = newProp.getElementsByTagName("input");
			for (var i=0; i< inputs.length; i++) 
				if (inputs[i].type == "text")
					inputs[i].value = "";
				
			tbody.appendChild(newProp);
		}
		
		// end -->
	</script>

	<table id="buttonsAtBottom"><tr><td>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="submit" name="action" value='<spring:message code="general.save"/>' />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="submit" name="action" value='<spring:message code="general.cancel"/>' />
		&nbsp;&nbsp;&nbsp;&nbsp;
	</td></tr></table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
