<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${not empty model.jsVar}">
	<openmrs_tag:reasonField reasons="${model.reasons}" formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" optionHeader="${model.optionHeader}" jsVar="${model.jsVar}" />
</c:if>

<c:if test="${empty model.jsVar}">
	<openmrs_tag:reasonField reasons="${model.reasons}" formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" optionHeader="${model.optionHeader}" />
</c:if>