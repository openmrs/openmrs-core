<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="openmrs_tag" tagdir="/WEB-INF/tags" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>

<c:choose>
	<c:when test="${fn:length(model.patientPrograms) == 0}">
		<spring:message code="Program.notEnrolledInAny"/>
	</c:when>
	<c:otherwise>
		<table>
		<tr>
			<th><spring:message code="Program.program"/></th>
			<th><spring:message code="Program.dateEnrolled"/></th>
			<th><spring:message code="Program.dateCompleted"/></th>
		</tr>
		<c:forEach var="program" items="${model.patientPrograms}">
			<tr>
				<td>
					<c:if test="${program.dateCompleted != null}">
						<small><i>[<spring:message code="Program.completed"/>]</i></small>
					</c:if>
					<openmrs_tag:concept conceptId="${program.program.concept.conceptId}"/>
				</td>
				<td align="center">
					<openmrs:formatDate date="${program.dateEnrolled}"/>
				</td>
				<td align="center">
					<openmrs:formatDate date="${program.dateCompleted}"/>
					<c:if test="${program.dateCompleted == null && model.allowEdits == 'true'}">
						<openmrs:hasPrivilege privilege="Manage Patient Programs">
							<div style="border: 1px black dashed">
								<form method="post" action="patientProgram.form">
									<input type="hidden" name="method" value="complete"/>
									<input type="hidden" name="patientProgramId" value="${program.patientProgramId}"/>
									<input type="hidden" name="returnPage" value="patientDashboard.form?patientId=${model.patientId}"/>
									<input type="text" name="dateCompleted" size="10" onClick="showCalendar(this)" />
									<input type="submit" value="<spring:message code="Program.completeButton"/>"/>
								</form>
							</div>
						</openmrs:hasPrivilege>
					</c:if>
				</td>
			</tr>
		</c:forEach>
		</table>			
	</c:otherwise>
</c:choose>

<c:if test="${model.allowEdits == 'true' && fn:length(model.programs) > 0}">
	<openmrs:hasPrivilege privilege="Manage Patient Programs">
		<div id="newProgramEnroll" style="border: 1px black dashed">
		<form method="post" action="patientProgram.form">
			<input type="hidden" name="method" value="enroll"/>
			<input type="hidden" name="patientId" value="${model.patientId}"/>
			<input type="hidden" name="returnPage" value="patientDashboard.form?patientId=${model.patientId}"/>
			
			<spring:message code="Program.enrollIn"/>
			<select name="programId">
				<option value=""><spring:message code="Program.choose"/></option>
				<c:forEach var="program" items="${model.programs}">
					<option value="${program.programId}"><openmrs_tag:concept conceptId="${program.concept.conceptId}"/></option>
				</c:forEach>
			</select>
			<spring:message code="general.onDate"/>
			<input type="text" id="programDateEnrolled" name="dateEnrolled" size="10" onClick="showCalendar(this)" />
		
			<input type="submit" value="<spring:message code="Program.enrollButton"/>"/>
		</form>
		</div>
	</openmrs:hasPrivilege>
</c:if>