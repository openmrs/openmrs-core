<%@ include file="/WEB-INF/template/include.jsp" %>

<!-- TESTING globProp reason is: <openmrs:globalProperty key="concept.reasonOrderStopped" defaultValue="notFound" /> -->
<!-- TESTING reasons is: ${model.reasons} -->
<c:if test="${not empty model.jsVar}">
	<openmrs_tag:reasonField reasons="${model.reasons}" formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" optionHeader="${model.optionHeader}" jsVar="${model.jsVar}" />
</c:if>

<c:if test="${empty model.jsVar}">
	<openmrs_tag:reasonField reasons="${model.reasons}" formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" optionHeader="${model.optionHeader}" />
</c:if>