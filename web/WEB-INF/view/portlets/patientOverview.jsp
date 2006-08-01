<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="openmrs_tag" tagdir="/WEB-INF/tags" %>

Example using openmrs:forEachObs - 

<openmrs:forEachObs obs="${model.patientObs}" conceptId="5089" var="o" num="1" descending="true">
	${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
</openmrs:forEachObs>

<br><br><br>

Example using c:forEach tag, openmrs:sort function, openmrs:filterObsByConcept function -

<c:forEach items="${openmrs:sort(openmrs:filterObsByConcept(model.patientObs, 5089), 'obsDatetime', true)}" var="o" end="0">
	${o.valueNumeric} (<openmrs:formatDate date="${o.obsDatetime}" />)
</c:forEach>

<br><br><br>

Example using tag file, which includes c:forEach tag, openmrs:sort function, openmrs:filterObsByConcept function -

<openmrs_tag:mostRecentObs observations="${model.patientObs}" concept="5089" />

<br><br><br>

<openmrs_tag:lastNObs observations="${model.patientObs}" concept="5089" n="3" separator=", " />