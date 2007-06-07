<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="person" required="true" type="org.openmrs.Person" %>

<c:choose>
	<c:when test="${person.patient.patientId != null}">
		<openmrs:patientWidget patientId="${person.patient.patientId}" size="full"/>
	</c:when>
	<c:otherwise>
		<openmrs:userWidget userId="${person.user.userId}" size="full"/>
	</c:otherwise>
</c:choose>