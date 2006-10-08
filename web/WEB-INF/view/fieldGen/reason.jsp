<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs_tag:reasonField reasons="${model.reasons}" formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" optionHeader="${model.optionHeader}" />
<%--
<select name="${model.formFieldName}" id="${model.formFieldName}">
<c:forEach items="${model.reasons}" var="reason">
	<option value="${reason}"><spring:message code="${reason}" /></option>
</c:forEach>
</select>
--%>
