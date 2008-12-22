<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Program.conversion.manage.title"/></h2>

<spring:hasBindErrors name="conceptStateConversion">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<openmrs:htmlInclude file="/dwr/interface/DWRProgramWorkflowService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
	
<form method="post" id="theForm">
<table>
	<tr>
		<td><spring:message code="Program.conversion.programWorkflow"/>:</td>
		<td>
			<spring:bind path="conversion.programWorkflow">
				<openmrs:fieldGen type="org.openmrs.ProgramWorkflow" formFieldName="programWorkflow" val="${status.editor.value}" parameters="onChange=updateStates()|optionHeader=[blank]|programPrefix=true" />
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Program.conversion.concept"/>:</td>
		<td>
			<spring:bind path="conversion.concept">
				<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="concept" val="${status.editor.value}" parameters="" />
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Program.conversion.programWorkflowState"/>:</td>
		<td>
			<select name="programWorkflowState" id="programWorkflowState"></select>
		</td>
	</tr>
</table>
<br />
<input type="submit" value='<spring:message code="Program.conversion.save"/>' onClick="$('theForm').submit()" />
</form>

<script>
<!--
	function updateStates() {
		//alert('update states now');
		var workflowId = DWRUtil.getValue("programWorkflow");
		DWRProgramWorkflowService.getStatesByWorkflow(workflowId, populateStates);
	}

	function populateStates(states) {
		dwr.util.removeAllOptions("programWorkflowState");
		dwr.util.addOptions("programWorkflowState", states, "id", "name");
		<c:if test="${not empty conversion.programWorkflowState}">
			dwr.util.setValue("programWorkflowState", ${conversion.programWorkflowState.programWorkflowStateId});
		</c:if>
	}
	
	updateStates();
	
-->
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>