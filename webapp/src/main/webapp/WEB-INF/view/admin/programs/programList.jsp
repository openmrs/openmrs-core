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
				<th> <spring:message code="Concept.name"/> </th>
				<th> <spring:message code="Program.workflows"/> </th>
			</tr>
			<c:forEach var="program" items="${programList}">
				<tr>
					<c:if test="${program.retired}">
						<td colspan="5">
							<i><spring:message code="general.retired"/><strike>
								<a href="program.form?programId=${program.programId}">${program.name}</a>
							</strike></i>
						</td>
					</c:if>
					<c:if test="${!program.retired}">
						<td valign="top">
							${program.programId}
						</td>
						<td valign="top">
							<a href="program.form?programId=${program.programId}">${program.name}</a>
						</td>
						<td valign="top">
							${program.description}
						</td>
						<openmrs:concept conceptId="${program.concept.conceptId}" var="v" nameVar="n" numericVar="num">
							<td valign="top">
								${n.name}
							</td>
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
					</c:if>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
