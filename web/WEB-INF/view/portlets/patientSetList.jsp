<%@ include file="/WEB-INF/template/include.jsp" %>

<center><b><u>
	<spring:message code="Analysis.currentPatientSet"/>
	(<i><spring:message code="Analysis.numPatients" arguments="${fn:length(model.patientSet.patientIds)}"/></i>)
</u></b></center>

<p>
<c:choose>
	<c:when test="${model.limit != null}">
		<c:forEach var="patientId" items="${model.patientSet.patientIds}" varStatus="status">
			<c:if test="${status.count <= model.limit}">
				<openmrs:patientWidget size="${model.size}" patientId="${patientId}" /> <br/>
				<c:if test="${status.count == model.limit && !status.last}">
					<spring:message code="general.nMore" arguments="${fn:length(model.patientSet.patientIds) - model.limit}"/>
					<br/>
				</c:if>
			</c:if>
		</c:forEach>
	</c:when>
	<c:when test="${model.patientSet.size > 100}">
		<c:forEach var="patientId" items="${model.patientSet.patientIds}">
			<openmrs:patientWidget size="compact" patientId="${patientId}" /> <br/>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<c:forEach var="patientId" items="${model.patientSet.patientIds}">
			<openmrs:patientWidget size="${model.size}" patientId="${patientId}" /> <br/>
		</c:forEach>
	</c:otherwise>
</c:choose>