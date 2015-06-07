<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="programWorkflows" required="true" type="java.util.List" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>
<%@ attribute name="programPrefix" required="false" %>

<c:if test="${empty programWorkflows}">
	<openmrs:message code="Program.workflows.empty" />
</c:if>
<c:if test="${not empty programWorkflows}">
	<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}"> onChange=${onChange}</c:if>>
		<c:if test="${optionHeader != ''}">
			<c:if test="${optionHeader == '[blank]'}">
				<option value=""></option>
			</c:if>
			<c:if test="${optionHeader != '[blank]'}">
				<option value="">${optionHeader}</option>
			</c:if>
		</c:if>
		<c:forEach var="programWorkflow" items="${programWorkflows}">
			<option value="${programWorkflow.programWorkflowId}" <c:if test="${programWorkflow.programWorkflowId == initialValue}">selected</c:if>>
				<c:if test="${programPrefix == 'true'}">
					<openmrs_tag:concept conceptId="${programWorkflow.program.concept.conceptId}"/> -
				</c:if>
					<openmrs_tag:concept conceptId="${programWorkflow.concept.conceptId}"/>
			</option>		
		</c:forEach>
	</select>
</c:if>
