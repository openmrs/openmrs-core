<%--
@Since 1.8.5, 1.9.4, 1.10

This tag prints out urls in a way that supports external ones
and is backwards compatible to support modules that exclude
a leading forward slash
 --%>

<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="value" required="true" %>
<c:choose>
	<c:when test="${fn:startsWith(value, '/') || fn:contains(value, '://')}">
		<c:url value="${value}" />
	</c:when>
	<c:otherwise>
		<openmrs:contextPath />/${value}
	</c:otherwise>
</c:choose>