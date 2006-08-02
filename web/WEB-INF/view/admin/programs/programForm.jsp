<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>
<script type="text/javascript">
<!--
	var idListBox  = null;
	var myConceptSearchMod = null;
	
	window.onload = function() {
		myConceptSearchMod = new fx.Resize("conceptSearchForm", {duration: 100});
		myConceptSearchMod.hide();
	}
	
	function addConcept(nameList, idList, obj)
	{
		nameList = document.getElementById(nameList);
		idList   = document.getElementById(idList);
		if (idList != idListBox) {
			//if user clicked on a new button
			closeConceptBox();
			nameListBox = nameList;	// used by onSelect()
			idListBox   = idList;	// used by onSelect()
		}
		
		var conceptSearchForm = document.getElementById("conceptSearchForm");
		setPosition(obj, conceptSearchForm, 520, 290);
		
		DWRUtil.removeAllRows("conceptSearchBody");
		
		myConceptSearchMod.toggle();
		if (addButton == null) {
			var searchText = document.getElementById("searchText");
			searchText.value = '';
			searchText.select();
			addButton = obj;
			resetForm();
			//searchText.focus();  //why does this cause the inner box to shift position?!?
		}
		else {
			obj.focus();
			addButton = null;
		}
	}
	
	function closeConceptBox() {
		myConceptSearchMod.hide();
		addButton = null;
		drugConcepts = new Array();
		return false;
	}
	
	function moveUp(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		for (var i=1; i<optList.length; i++) {
			// loop over and move up all selected items
			if (optList[i].selected && !optList[i-1].selected) {
				var id   = optList[i].value;
				var name = optList[i].text;
				optList[i].value = optList[i-1].value;
				optList[i].text  = optList[i-1].text;
				optList[i].selected = false;
				optList[i-1].value = id;
				optList[i-1].text  = name;
				optList[i-1].selected = true;
			}
		}
		copyIds(nameList, idList, ' ');
	}
	
	function moveDown(nameList, idList)
	{
		var input = document.getElementById(idList);
		var sel = document.getElementById(nameList);
		var optList = sel.options;
		for (var i=optList.length-2; i>=0; i--) {
			if (optList[i].selected && !optList[i+1].selected) {
				var id   = optList[i].value;
				var name = optList[i].text;
				optList[i].value = optList[i+1].value;
				optList[i].text  = optList[i+1].text;
				optList[i].selected = false;
				optList[i+1].value = id;
				optList[i+1].text  = name;
				optList[i+1].selected = true;
			}
		}
		copyIds(nameList, idList, ' ');
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
-->
</script>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsPopup.js"></script>

<h2><spring:message code="Program.manage.title"/></h2>

<form method="post">
<table>
	<tr>
		<td><spring:message code="Program.concept"/></td>
		<td>
			<spring:bind path="program.concept">
				<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="${status.expression}" startVal="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<table>
	<tr id="workflowSetRow">
		<th valign="top"><spring:message code="Program.workflows" text="Workflows"/></th>
		<td valign="top">
			<input type="text" name="workflows" id="workflows" value='<c:forEach items="${program.workflows}" var="set">${set.value[0]} </c:forEach>' />
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<select class="largeWidth" size="6" id="workflowsNames" multiple onkeyup="listKeyPress('workflowsNames', 'workflows', ' ', event);">
							<c:forEach items="${program.workflows}" var="set">
								<option value="${set.value[0]}">${set.value[1]} (${set.value[0]})</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" class="buttons">
						&nbsp;<input type="button" value="<spring:message code="general.add"/>" class="smallButton" onClick="addConcept('workflowsNames', 'workflows', this);" /> <br/>
						&nbsp;<input type="button" value="<spring:message code="general.remove"/>" class="smallButton" onClick="removeItem('workflowsNames', 'workflows', ' ');" /> <br/>
						&nbsp;<input type="button" value="<spring:message code="general.move_up"/>" class="smallButton" onClick="moveUp('workflowsNames', 'workflows');" /><br/>
						&nbsp;<input type="button" value="<spring:message code="general.move_down"/>" class="smallButton" onClick="moveDown('workflowsNames', 'workflows');" /><br/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br />
<input type="submit" value="<spring:message code="Program.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>