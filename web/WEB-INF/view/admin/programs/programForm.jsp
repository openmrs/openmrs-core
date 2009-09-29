<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	var idToNameMap = new Array();
	
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				var popup = dojo.widget.manager.getWidgetById("conceptSelection");
				popup.hiddenInputNode.value = msg.objs[0].conceptId;
				popup.displayNode.innerHTML = msg.objs[0].name;
			}
		);
	});
	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("wfSearch/select", 
			function(msg) {
				idToNameMap[msg.objs[0].conceptId] = msg.objs[0].name;
				addWorkflow(msg.objs[0].conceptId);
			}
		);	
	});

	function helper(value) {
		var values = value.split(" ");
		for (var i = 0; i < values.length; ++i) {
			if (values[i] == '') {
				values.splice(i, 1);
				--i;
			}
		}
		return values;
	}
	
	function cleanupWorkflowsValue() {
		var value = $('workflowsValue').value;
		if (value == '' || value == 'null') value = ':';
		$('workflowsValue').value = value;
	}
	
	function addWorkflow(conceptId) {
		$('workflowsValue').value = $('workflowsValue').value + ' ' + conceptId;
		refreshWorkflowsDisplay();
	}
	
	function removeWorkflow(conceptId) {
		var value = $('workflowsValue').value;
		if (value == '' || value == 'null') value = ':';
		var progId = value.substring(0, value.indexOf(":"));
		value = value.substring(value.indexOf(":") + 1);
		var values = helper(value);
		var ret = progId + ':';
		for (var i = 0; i < values.length; ++i) {
			if (values[i] != conceptId) {
				ret += values[i] + ' ';
			}
		}
		$('workflowsValue').value = ret;
		refreshWorkflowsDisplay();
	}
	
	function refreshWorkflowsDisplay() {
		var tableId = 'workflowsDisplay';
		var value = $('workflowsValue').value;
		value = value.substring(value.indexOf(":") + 1);
		values = helper(value);
		dwr.util.removeAllRows(tableId);
		dwr.util.addRows(tableId, values, [
				function(id) { return idToNameMap[id]; },
				function(id) { return '<a href="javascript:removeWorkflow(' + id + ')">[x]</a>';},
			], {escapeHtml: false});
	}
</script>

<h2><spring:message code="Program.addEdit.title"/></h2>

<spring:hasBindErrors name="program">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post" id="theForm">
<table>
	<tr>
		<th><spring:message code="general.name"/></th>
		<td>
			<spring:bind path="program.name">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="general.description"/></th>
		<td>
			<spring:bind path="program.description">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><spring:message code="Program.concept"/></th>
		<td>
			<spring:bind path="program.concept">
				<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="${status.value}" showVerboseListing="false" conceptClasses="Program"></div>
				<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="${status.expression}" searchWidget="cSearch" searchTitle='<spring:message code="Concept.find" />'></div>
							
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td></td>
		<td><spring:message code="Program.conceptHint"/></td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="general.retired"/>?</th>
		<td valign="top">
			<spring:bind path="program.retired">
				<select name="${status.expression}">
					<option value="false" <c:if test="${status.value == false}">selected</c:if>><spring:message code="general.no"/></option>
					<option value="true" <c:if test="${status.value == true}">selected</c:if>><spring:message code="general.yes"/></option>
				</select>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="workflowSetRow">
		<th valign="top"><spring:message code="Program.workflows" /></th>
		<td valign="top">
			<spring:bind path="program.allWorkflows">
				<input type="hidden" name="${status.expression}" id="workflowsValue" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
			
			<table id="workflowsDisplay">
			</table>

			<div dojoType="ConceptSearch" widgetId="wfSearch" showVerboseListing="false" conceptClasses="Workflow"></div>
			<div dojoType="OpenmrsPopup" widgetId="conceptSelection2" hiddenInputName="notUsed" searchWidget="wfSearch" searchTitle='<spring:message code="Concept.find" />' changeButtonValue='<spring:message code="general.add"/>'></div>
			
		</td>
	</tr>
</table>
<br />
<input type="submit" value='<spring:message code="Program.save"/>' onClick="$('theForm').submit()" />
</form>

<script type="text/javascript">
	cleanupWorkflowsValue();
	<c:forEach var="workflow" items="${program.workflows}">
		idToNameMap[${workflow.concept.conceptId}] = '<openmrs:concept conceptId="${workflow.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>';
	</c:forEach>
	refreshWorkflowsDisplay();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>