<%@ include file="/WEB-INF/template/include.jsp" %>

<%--<openmrs:require privilege="View Tasks" otherwise="/login.htm" redirect="/admin/scheduler/task.list" />--%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Scheduler.header" /></h2>	

<script type="text/javascript">

</script>

<a href="scheduler.htm?method=addTask"><spring:message code="Scheduler.addTask" /></a>

<br /><br />

<b class="boxHeader">
	<spring:message code="Scheduler.taskList.title" />
</b>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0">
		<tr>
			<th></th>
			<th><spring:message code="general.name"/></th>
			<th><spring:message code="general.description"/></th>
			<th><spring:message code="Scheduler.taskList.active"/></th>

		</tr>
		<c:forEach var="task" items="${model.scheduledTasks}">
			<tr>
				<td valign="top" style="white-space: nowrap">
					<a href="scheduler.htm?method=viewTask&taskId=${task.id}"><spring:message code="Scheduler.scheduleTask"/></a> 
				</td>
				<td valign="top">${task.name}</td>
				<td valign="top">${task.description}</td>
				<td valign="top">
          <c:if test="${task.active == true}"><spring:message code="general.yes"/></c:if>
          <c:if test="${task.active != true}"><spring:message code="general.no"/></c:if>
        </td> 
				<td valign="top">
			</tr>
		</c:forEach>
	</table>
</form>



<%@ include file="/WEB-INF/template/footer.jsp" %>