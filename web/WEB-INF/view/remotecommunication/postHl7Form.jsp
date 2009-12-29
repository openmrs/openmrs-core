<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<c:if test="${model.success}">
	OK
</c:if>
<c:if test="${!model.success}">
	<spring:message code="PostHl7.error" arguments="${model.error}" />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>