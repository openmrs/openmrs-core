<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/conversion.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Program.conversion.manage.title"/></h2>

<a href="conversion.form"><openmrs:message code="Program.conversion.add"/></a>

<br /><br />

<b class="boxHeader"><openmrs:message code="Program.conversion.list.title"/></b>
<div class="box">
	<c:if test="${fn:length(stateConversionList) == 0}">
		<tr>
			<td colspan="5"><openmrs:message code="general.none"/></td>
		</tr>
	</c:if>
	<c:if test="${fn:length(stateConversionList) != 0}">
		<form method="post" id="theForm">
			<table id="triggeredStateConversion">
				<tr>
					<th> </th>
					<th> <openmrs:message code="Program.conversion.programWorkflow"/> </th>
					<th> <openmrs:message code="Program.conversion.concept"/> </th>
					<th> <openmrs:message code="Program.conversion.programWorkflowState"/> </th>
					<th> </th>
				</tr>
				<c:forEach var="conversion" items="${stateConversionList}">
					<tr> 
						<td valign="top">
							<input type="checkbox" name="conceptStateConversionId" value="${conversion.conceptStateConversionId}">
						</td>
						<td>
							<openmrs_tag:concept conceptId="${conversion.programWorkflow.program.concept.conceptId}"/> -
							<openmrs_tag:concept conceptId="${conversion.programWorkflow.concept.conceptId}"/>
						</td>
						<td>
							<openmrs_tag:concept conceptId="${conversion.concept.conceptId}"/>
						</td>
						<td>
							<openmrs_tag:concept conceptId="${conversion.programWorkflowState.concept.conceptId}"/>
						</td>
						<td valign="top">
							<a href="conversion.form?conceptStateConversionId=${conversion.conceptStateConversionId}">
								<openmrs:message code="general.edit" />
							</a>
						</td>
					</tr>
				</c:forEach>
			</table>
			<input type="submit" value="<openmrs:message code="Program.conversion.deleteSelected" />">
		</form>
	</c:if>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
