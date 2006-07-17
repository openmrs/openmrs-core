<%@ include file="/WEB-INF/template/include.jsp" %>

<center><b><u>
	<spring:message code="Analysis.currentPatientSet"/>
	(<i><spring:message code="Analysis.numPatients" arguments="${fn:length(model.patientSet.patientIds)}"/></i>)
</u></b></center>

<p>
<c:choose>
	<c:when test="${limit != null}">
		<c:forEach var="patientId" items="${model.patientSet.patientIds}" varStatus="status">
			<c:if test="${status.count <= limit}">
				<openmrs:patientWidget patientId="${patientId}" /> <br/>
				<c:if test="${status.count == limit && !status.last}">
					... <br/>
				</c:if>
			</c:if>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<c:forEach var="patientId" items="${model.patientSet.patientIds}">
			<openmrs:patientWidget patientId="${patientId}" /> <br/>
		</c:forEach>
	</c:otherwise>
</c:choose>