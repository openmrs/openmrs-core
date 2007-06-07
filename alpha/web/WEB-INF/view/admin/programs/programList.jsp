<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Program.manage.title"/></h2>

<a href="program.form"><spring:message code="Program.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="Program.list.title"/></b>
<div class="box">
	<c:if test="${fn:length(programList) == 0}">
		<tr>
			<td colspan="5"><spring:message code="general.none"/></td>
		</tr>
	</c:if>
	<c:if test="${fn:length(programList) != 0}">
		<table cellspacing="0" cellpadding="2">
			<tr>
				<th> <spring:message code="general.id"/> </th>
				<th> <spring:message code="general.name"/> </th>
				<th> <spring:message code="general.description"/> </th>
				<th> <spring:message code="Program.workflows"/> </th>
			</tr>
			<c:forEach var="program" items="${programList}">
				<tr> 
					<td valign="top">
						<c:if test="${program.voided}"><i><spring:message code="general.voided"/><strike></c:if>
						${program.programId}
						<c:if test="${program.voided}"></strike></i></c:if>
					</td>
					<openmrs:concept conceptId="${program.concept.conceptId}" var="v" nameVar="n" numericVar="num">
						<td valign="top">
							<c:if test="${program.voided}"><strike></c:if>
							<a href="program.form?programId=${program.programId}">
							${n.name}
							</a>
							<c:if test="${program.voided}"></strike></c:if>
						</td>
						<td valign="top">${n.description}</td>
					</openmrs:concept>
					<td>
						<c:forEach var="workflow" items="${program.workflows}">
							<a href="workflow.form?programWorkflowId=${workflow.programWorkflowId}">
								<openmrs_tag:concept conceptId="${workflow.concept.conceptId}"/>
								(${fn:length(workflow.states)})
							</a>
							<br/>
						</c:forEach>
					</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
