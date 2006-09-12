<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenace/globalProps.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="GlobalProperty.manage.title"/></h2>	

<b class="boxHeader"><spring:message code="GlobalProperty.list.title"/></b>
<form method="post" class="box">
	<table>
		<thead>
			<tr>
				<th> <spring:message code="GlobalProperty.remove" /> </th>
				<th> <spring:message code="general.name" /> </th>
				<th> <spring:message code="general.value" /> </th>
			</tr>
		</thead>
		<tbody id="globalPropsList">
			<c:forEach var="globalProp" items="${globalProps}">
				<tr>
					<td valign="top"><input type="checkbox" name="propDelete" value="${globalProp.property}"></td>
					<td valign="top">${globalProp.property}</td>
					<td valign="top"><input type="text" name="global.${globalProp.property}" id="global.${globalProp.property}" value="${globalProp.propertyValue}" size="30" maxlength="250" /></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<openmrs:htmlInclude file="/dwr/util.js" />
	<script>
		<!-- // begin
		
		var propertyCellFuncs = [
			function(data) { return "<input type=\"checkbox\" name=\"propDelete\" value=\"" + data.propName + "\">"; },
			function(data) { return data.propName; },
			function(data) { return "<input type=\"text\" name=\"global_new." + data.propName + "\" id=\"global_new." + data.propName + "\" value=\"" + data.propValue + "\" size=\"30\" maxlength=\"250\" />"; }
		];

		function GlobalProperty(propName, propValue) {
			this.propName = propName;
			this.propValue = propValue;
		}
		
		function addProp() {
			var propName = document.getElementById("global_add_name");
			var propVal = document.getElementById("global_add_value");
			if ( propName && propVal ) {
				if ( propName.value.length > 0 ) {
					var newProp = new GlobalProperty(propName.value, propVal.value);
					
					//alert("new prop is " + newProp.propName + ", " + newProp.propValue);
					var newProps = [newProp];

					//alert("prop array is length " + newProps.length);
					
					DWRUtil.addRows("globalPropsList", newProps, propertyCellFuncs, {
						cellCreator:function(options) {
						    var td = document.createElement("td");
						    return td;
						}
					});

					DWRUtil.setValue("global_add_name", "");
					DWRUtil.setValue("global_add_value", "");
					//showAddForm();

				} else {
					alert("<spring:message code="GlobalProperty.error.name.required" />");
				}
			}
		}

		function showAddForm() {
			showHideDiv("addLink");
			showHideDiv("addProperty");
		}
		
		function showHideDiv(id) {
			var div = document.getElementById(id);
			if ( div ) {
				if ( div.style.display != "none" ) {
					div.style.display = "none";
				} else { 
					div.style.display = "";
				}
			}
		}

		// end -->
		
	</script>
	<table>
		<tr>
			<td><div id="addLink"><a href="javascript:void();" onClick="showAddForm();"><spring:message code="GlobalProperty.add" /></a></div></td>
			<td>
				<div id="addProperty" style="display:none;">
					<spring:message code="general.name" />:
					<input type="text" name="global_add_name" id="global_add_name" value="" size="30" />
					&nbsp;&nbsp;&nbsp;&nbsp;
					<spring:message code="general.value" />:
					<input type="text" name="global_add_value" id="global_add_value" value="" size="30" />
					&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="button" name="add" value="<spring:message code="general.add" />" onClick="addProp();"/>
				</div>
			</td>
		</tr>
	</table>
	<input type="submit" value="<spring:message code="general.save"/>" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
