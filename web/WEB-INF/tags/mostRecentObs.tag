<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="observations" required="true" type="java.util.Set" %>
<%@ attribute name="concept" required="true" type="java.lang.Integer" %>

<c:forEach items="${openmrs:sort(openmrs:filterObsByConcept(observations, concept), 'obsDatetime', true)}" var="o" end="0">
	${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
</c:forEach>