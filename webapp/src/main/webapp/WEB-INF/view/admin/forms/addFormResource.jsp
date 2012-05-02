<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/form.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script>
$j(function() {
	$j('.cancelButton').click(function() {
		location.href = 'formResources.form?formId=' + ${ form.id };
	});
});
</script>

<h2>
	<spring:message code="Form.addResource.title"/> - <openmrs:format form="${ form }"/> 
</h2>

<c:if test="${ empty resource }">
	<spring:message code="Form.addResource.chooseHandler"/>:
	<form method="get">
		<input type="hidden" name="formId" value="${ form.id }"/>
		<input type="hidden" name="datatype" value="${ datatype }"/>
		<select name="handler">
			<option value="DEFAULT"><spring:message code="general.default"/></option>
			<c:forEach var="handler" items="${ handlers }">
				<option value="${ handler }"><spring:message code="${ handler }.name"/></option>
			</c:forEach>
		</select>
		<br/><br/>
		<input type="submit" value="<spring:message code="general.continue"/>"/>
		<input type="button" value="<spring:message code="general.cancel"/>" class="cancelButton"/>
	</form>
</c:if>

<c:if test="${ not empty resource }">
	<form:form modelAttribute="resource" method="post" enctype="multipart/form-data">
		<form:hidden path="form"/>
		<form:hidden path="datatypeClassname"/>
		<form:hidden path="datatypeConfig"/>
		<form:hidden path="preferredHandlerClassname"/>
		<form:hidden path="handlerConfig"/>
		<table>
			<tr valign="top">
				<th><spring:message code="Form.resource.name"/></th>
				<td>
					<form:input path="name"/>
					<form:errors path="name" cssClass="errors"/>
				</td>
			</tr>
			<tr valign="top">
				<th><spring:message code="Form.resource.value"/></th>
				<td>
					<openmrs_tag:singleCustomValue
			           	customValueDescriptor="${ resource }"
			           	formFieldName="resourceValue" />
			    </td>
			</tr>
			<tr>
				<th></th>
				<td>
			        <input type="submit" value="<spring:message code="general.save"/>"/>
			        <input type="button" value="<spring:message code="general.cancel"/>" class="cancelButton"/>
			    </td>
			</tr>
		</table>
	</form:form>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>