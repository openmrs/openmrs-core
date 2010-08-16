<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="observations" required="true" type="java.util.Set" %>
<%@ attribute name="concept" required="true" type="java.lang.Integer" %>
<%@ attribute name="n" required="true" type="java.lang.Integer" %>
<%@ attribute name="separator" required="true" %>
<%@ attribute name="locale" required="true" type="java.util.Locale" %>
<%@ attribute name="label" required="false" type="java.lang.String" %>
<%@ attribute name="showUnits" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showDate" required="false" type="java.lang.Boolean" %>

<c:forEach items="${openmrs:sort(openmrs:filterObsByConcept(observations, concept), 'obsDatetime', true)}" var="o" varStatus="s" end="${n-1}">
	
	<c:if test="${label != null}">
		<span class="obsLabel"><spring:message code="${label}" />:</span>
	</c:if>
	<span class="obsValue"><openmrs:format obsValue="${o}"/></span>
	<c:if test="${showUnits}">
		<openmrs:concept conceptId="${o.concept.conceptId}" var="c" nameVar="n" numericVar="nv">
			<c:if test="${nv != null}">
				<span class="obsUnits"><spring:message code="Units.${nv.units}" /></span>
			</c:if>
		</openmrs:concept>
	</c:if>
	<span class="obsDate"><c:if test="${showDate}">(<openmrs:formatDate date="${o.obsDatetime}" type="medium" />)</c:if></span>
	<c:if test="${!s.last}">${separator}</c:if>
</c:forEach>
