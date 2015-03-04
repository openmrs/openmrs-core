<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">
	var idToNameMap = new Array();
	
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

    dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("oSearch/select",
			function(msg) {
				var popup = dojo.widget.manager.getWidgetById("outcomeConceptSelection");
				popup.hiddenInputNode.value = msg.objs[0].conceptId;
				popup.displayNode.innerHTML = msg.objs[0].name;
			}
		);
	});
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
		var value = $j('#workflowsValue').val();
		if (value == '' || value == 'null') value = ':';
		$j('#workflowsValue').val(value);
	}
	
	function addWorkflow(conceptId) {
		$j('#workflowsValue').val($j('#workflowsValue').val() + ' ' + conceptId);
		refreshWorkflowsDisplay();
	}
	
	function removeWorkflow(conceptId) {
		var value = $j('#workflowsValue').val();
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
		$j('#workflowsValue').val(ret);
		refreshWorkflowsDisplay();
	}
	
	function refreshWorkflowsDisplay() {
		var tableId = 'workflowsDisplay';
		var value = $j('#workflowsValue').val();
		value = value.substring(value.indexOf(":") + 1);
		values = helper(value);
		dwr.util.removeAllRows(tableId);
		dwr.util.addRows(tableId, values, [
				function(id) { return idToNameMap[id]; },
				function(id) { return '<a href="javascript:removeWorkflow(' + id + ')">[x]</a>';},
			], {escapeHtml: false});
	}
</script>

<h2><openmrs:message code="Program.addEdit.title"/></h2>

<spring:hasBindErrors name="program">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post" id="theForm">
<table>
	<tr>
		<th><openmrs:message code="general.name"/><span class="required">*</span></th>
		<td>
			<spring:bind path="program.name">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="general.description"/><span class="required">*</span></th>
		<td>
			<spring:bind path="program.description">
				<input type="text" name="${status.expression}" value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th><openmrs:message code="Program.concept"/><span class="required">*</span></th>
		<td>
			<spring:bind path="program.concept">
				<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="${status.value}" showVerboseListing="false" conceptClasses="Program"></div>
				<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="${status.expression}" searchWidget="cSearch" searchTitle='<openmrs:message code="Concept.find" />'></div>
							
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
		<td><openmrs:message code="Program.conceptHint"/></td>
	</tr>
	<tr>
		<th valign="top"><openmrs:message code="general.retired"/>?</th>
		<td valign="top">
			<spring:bind path="program.retired">
				<select name="${status.expression}">
					<option value="false" <c:if test="${status.value == false}">selected</c:if>><openmrs:message code="general.no"/></option>
					<option value="true" <c:if test="${status.value == true}">selected</c:if>><openmrs:message code="general.yes"/></option>
				</select>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr id="workflowSetRow">
		<th valign="top"><openmrs:message code="Program.workflows" /></th>
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
			<div dojoType="OpenmrsPopup" widgetId="conceptSelection2" hiddenInputName="notUsed" searchWidget="wfSearch" searchTitle='<openmrs:message code="Concept.find" />' changeButtonValue='<openmrs:message code="general.add"/>'></div>
			
		</td>
	</tr>
    <tr>
		<th><openmrs:message code="Program.outcomes"/></th>
		<td>
			<spring:bind path="program.outcomesConcept">
				<div dojoType="ConceptSearch" widgetId="oSearch" conceptId="${status.value}" showVerboseListing="false" conceptClasses="Program"></div>
				<div dojoType="OpenmrsPopup" widgetId="outcomeConceptSelection" hiddenInputName="${status.expression}" searchWidget="oSearch" searchTitle='<openmrs:message code="Concept.find" />'></div>

				<c:if test="${status.errorMessage != ''}">
					<span class="error">
						${status.errorMessage}
					</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
     <c:if test="${program.id != null}">
     <th><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></th>
     <td colspan="${fn:length(locales)}">
       <font color="#D0D0D0"><sub>
       <spring:bind path="program.uuid">
           <c:out value="${status.value}"></c:out>
       </spring:bind> </sub></font></td>
       </c:if>
   </tr>
</table>
<br />
<input type="submit" value='<openmrs:message code="Program.save"/>' onClick="jQuery('#theForm').submit()" />
</form>

<script type="text/javascript">
	cleanupWorkflowsValue();
	<c:forEach var="workflow" items="${program.allWorkflows}">
		<c:choose>
		<c:when test="${!workflow.retired}">
			idToNameMap[${workflow.concept.conceptId}] = '<openmrs:concept conceptId="${workflow.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>';
		</c:when>
		<c:otherwise>
			removeWorkflow(${workflow.concept.conceptId});
		</c:otherwise>
		</c:choose>
	</c:forEach>
	refreshWorkflowsDisplay();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
