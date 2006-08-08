<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="observations" required="true" type="java.util.Set" %>
<%@ attribute name="concept" required="true" type="java.lang.Integer" %>
<%@ attribute name="n" required="true" type="java.lang.Integer" %>
<%@ attribute name="separator" required="true" %>

<c:forEach items="${openmrs:sort(openmrs:filterObsByConcept(observations, concept), 'obsDatetime', true)}" var="o" varStatus="s" end="${n-1}">
	${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
	<c:if test="${!s.last}">${separator}</c:if>
</c:forEach>