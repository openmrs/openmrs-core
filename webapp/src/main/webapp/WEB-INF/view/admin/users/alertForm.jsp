<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Alerts" otherwise="/login.htm" redirect="/admin/users/alert.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.UserSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("uSearch");			
		
		dojo.event.topic.subscribe("uSearch/select", 
			function(msg) {
				for (var i=0; i< msg.objs.length; i++) {
					var obj = msg.objs[i];
					var options = $("userNames").options;
					
					var isAddable = true;
					for (x=0; x<options.length; x++)
						if (options[x].value == obj.userId)
							isAddable = false;
					
					if (isAddable) {
						var opt = new Option(obj.personName, obj.userId);
						opt.selected = true;
						options[options.length] = opt;
						copyIds("userNames", "userIds", " ");
					}
				}
			}
		);
	});
	
</script>

<script type="text/javascript">
		
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
			
			var isAddable = true;
			for (x=0; x<synonyms.length; x++)
				if (synonyms[x].value == syn.userId)
					isAddable = false;
					
			if (isAddable) {
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
	th { text-align: left; }
	.description { display: none; }
</style>

<h2><openmrs:message code="Alert.manage.title"/></h2>	

<spring:hasBindErrors name="alert">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<th valign="top"><openmrs:message code="Alert.text"/><span class="required">*</span></th>
		<td>
			<spring:bind path="alert.text">
				<textarea id="text" name="${status.expression}" rows="2" cols="43">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="Alert.recipients"/><span class="required">*</span></th>
		<td valign="top">
			<input type="hidden" name="userIds" id="userIds" size="40" value='<c:forEach items="${alert.recipients}" var="recipient">${recipient.recipient.userId} </c:forEach>' />
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select class="mediumWidth" size="6" id="userNames" multiple onkeyup="listKeyPress('userNames', 'userIds', ' ', event);">
							<c:forEach items="${alert.recipients}" var="recipient">
								<option value="${recipient.recipient.userId}"><c:out value="${recipient.recipient.personName}" /></option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<span dojoType="UserSearch" widgetId="uSearch"></span><span dojoType="OpenmrsPopup" searchWidget="uSearch" searchTitle='<openmrs:message code="User.find"/>' changeButtonValue='<openmrs:message code="general.add"/>'></span> <br/>
						&nbsp; <input type="button" value="<openmrs:message code="general.remove"/>" class="smallButton" onClick="removeItem('userNames', 'userIds', ' ');" /> <br/>
						&nbsp; <input type="button" value="<openmrs:message code="User.goTo"/>" class="smallButton" onClick="gotoUser('userNames');" /><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="Alert.roles"/></th>
		<td valign="top">
			<select id="roleStr" class="mediumWidth">
				<option value=""><openmrs:message code="general.none"/></option>
				<c:forEach items="${allRoles}" var="role">
					<option value="${role.role}" <c:if test="${role == status.value}">selected</c:if>>${role.role}</option>
				</c:forEach>
			</select>
			&nbsp;<input type="button" class="smallButton" value="<openmrs:message code="Alert.addRole"/>" onClick="addRole();"/>
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
						&nbsp; <input type="button" value="<openmrs:message code="general.remove"/>" class="smallButton" onClick="removeItem('roles', 'newRoles', ',');" /> <br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="Alert.satisfiedByAny"/></th>
		<td valign="top">
			<spring:bind path="alert.satisfiedByAny">
				<input type="hidden" name="_${status.expression}" value="on" />
				<input type="checkbox" name="${status.expression}"
					   value="on" <c:if test="${alert.satisfiedByAny}">checked</c:if> />
			</spring:bind>
			<i><openmrs:message code="Alert.satisfiedByAny.description"/></i>
		</td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="Alert.dateToExpire"/></th>
		<td valign="top">
			<spring:bind path="alert.dateToExpire">
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onfocus="showCalendar(this)"/>
			</spring:bind>
		</td>
	</tr>
	<tr>
         <c:if test="${alert.alertId != null}">
           <th><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></th>
           <td colspan="${fn:length(locales)}"><font color="#D0D0D0"><sub>${alert.uuid}</sub></font></td>
         </c:if>
 	</tr>
	<c:if test="${!(encounter.changedBy == null)}">
		<tr>
			<td><openmrs:message code="general.changedBy" /></td>
			<td>
				<a href="#View User" onclick="return gotoUser(null, '${alert.changedBy.userId}')"><c:out value="${alert.changedBy.personName}" /></a> -
				<openmrs:formatDate date="${alert.dateChanged}" type="medium" />
			</td>
		</tr>
	</c:if>
</table>

<input type="submit" value="<openmrs:message code="Alert.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>