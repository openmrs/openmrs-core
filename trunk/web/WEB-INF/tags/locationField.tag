<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>

<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}"> onChange=${onChange}</c:if>>
	<c:if test="${optionHeader != ''}">
		<c:if test="${optionHeader == '[blank]'}">
			<option value=""></option>
		</c:if>
		<c:if test="${optionHeader != '[blank]'}">
			<option value="">${optionHeader}</option>
		</c:if>
	</c:if>
	<openmrs:forEachRecord name="location">
		<option value="${record.locationId}" <c:if test="${record.locationId == initialValue}">selected</c:if>>${record.name}</option>
	</openmrs:forEachRecord>
</select>
