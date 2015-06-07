<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Program.conversion.manage.title"/></h2>

<spring:hasBindErrors name="conceptStateConversion">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<openmrs:htmlInclude file="/dwr/interface/DWRProgramWorkflowService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
	
<form method="post" id="theForm">
<table>
	<tr>
		<td><openmrs:message code="Program.conversion.programWorkflow"/>:<span class="required">*</span></td>
		<td>
			<spring:bind path="conversion.programWorkflow">
				<openmrs:fieldGen type="org.openmrs.ProgramWorkflow" formFieldName="programWorkflow" val="${status.editor.value}" parameters="onChange=updateStates()|optionHeader=[blank]|programPrefix=true" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="Program.conversion.concept"/>:<span class="required">*</span></td>
		<td>
			<spring:bind path="conversion.concept">
				<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="concept" val="${status.editor.value}" parameters="" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="Program.conversion.programWorkflowState"/>:<span class="required">*</span></td>
		<td>
			<spring:bind path="conversion.programWorkflowState">
				<select name="programWorkflowState" id="programWorkflowState"></select>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>	
		</td>
	</tr>
	<tr>
     <c:if test="${conversion.id != null}">
     <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/>:</sub></font></td>
         <td colspan="${fn:length(locales)}">
         <font color="#D0D0D0"><sub>
         ${conversion.uuid}</sub></font></td>
     </c:if>
   </tr>
</table>
<br />

<input type="submit" value='<openmrs:message code="Program.conversion.save"/>' onClick="jQuery('#theForm').submit()" />
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