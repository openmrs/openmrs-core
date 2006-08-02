<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="Program.manage.title"/></h2>

<a href="program.form"><spring:message code="Program.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="Program.list.title"/></b>
<div class="box">
	<table>
		<tr>
			<th> <spring:message code="general.id"/> </th>
			<th> <spring:message code="general.name"/> </th>
			<th> <spring:message code="general.description"/> </th>
		</tr>
		<c:if test="${fn:length(programList) == 0}">
			<tr>
				<td colspan=2><spring:message code="general.none"/></td>
			</tr>
		</c:if>
		<c:forEach var="program" items="${programList}">
			<openmrs:concept conceptId="${program.concept.conceptId}" var="v" nameVar="n"/>
			<tr> 
				<td valign="top">${program.programId}</td>
				<td valign="top">${n.name}</td>
				<td valign="top">${n.description}</td>
				<td>
					${fn:length(program.workflows)}
					<c:forEach var="workflow" items="${program.workflows}">
						<openmrs:concept conceptId="${workflow.concept.conceptId}" var="v" nameVar="n">${n.name}</openmrs:concept> <br/>
					</c:forEach>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>