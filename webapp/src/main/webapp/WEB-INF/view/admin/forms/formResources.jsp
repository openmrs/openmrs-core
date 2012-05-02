<%@page import="org.openmrs.customdatatype.CustomDatatype"%>
<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/form.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<style>
	.existing-resources th,td {
		padding-right: 1em;
	}
</style>

<script>
$j(function() {
	$j('#addButton').click(function() {
		$j('#addDialog').slideToggle('fast');
	})
});
</script>

<h2><openmrs:format form="${ form }"/> - <spring:message code="Form.resources.title"/></h2>

<p>
	<a id="addButton" href="javascript:void()"><spring:message code="Form.resource.add"/></a>
	<div id="addDialog" style="display: none; background-color: #e0e0e0;">
		<form method="GET" action="addFormResource.form">
			<input type="hidden" name="formId" value="${ form.formId }"/>
			<spring:message code="Form.resource.datatype"/>:
			<select name="datatype">
				<c:forEach var="dt" items="${ datatypes }">
					<option value="${ dt }"><spring:message code="${ dt }.name"/></option>
				</c:forEach>
			</select>
			<br/>
			<input type="submit" value="<spring:message code="general.continue"/>"/>
		</form>
	</div>
</p>

<b class="boxHeader">
	<spring:message code="Form.resources.existing"/>
</b>

<div class="box">
	<c:choose>
		<c:when test="${ empty resources }">
			<spring:message code="general.none"/>
		</c:when>
		<c:otherwise>
			<table class="existing-resources">
				<tr>
					<th><spring:message code="Form.resource.name"/></th>
					<th><spring:message code="Form.resource.datatype"/></th>
					<th><spring:message code="general.value"/></th>
					<th></th>
				</tr>
				<c:forEach var="resource" items="${ resources }" varStatus="status">
					<tr valign="top" class="${status.index % 2 == 0 ? "evenRow" : "oddRow"}">
						<td>${ resource.name }</td>
						<td><spring:message code="${ resource.datatypeClassname }.name"/></td>
						<td><openmrs:format singleCustomValue="${ resource }"/></td>
						<td>
							<form method="POST" action="deleteFormResource.form?formId=${ form.formId }&name=${ resource.name }">
								<input type="submit" value="<spring:message code="general.delete"/>"/>
							</form>
						</td>
					</tr>
				</c:forEach>
			</table>		
		</c:otherwise>
	</c:choose>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>