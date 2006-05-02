<%@ include file="/WEB-INF/template/include.jsp" %>

<%--<openmrs:require privilege="View Tasks" otherwise="/login.htm" redirect="/admin/scheduler/task.list" />--%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<script type="text/javascript">

</script>

<h2><spring:message code="Scheduler.header" /></h2>	

<a href="scheduler.form"><spring:message code="Scheduler.taskList.add" /></a>

<br/><br/>

<div class="schedulerList">
	<b class="boxHeader"><spring:message code="Scheduler.taskList.title" /></b>
	<div class="box">
		<form id="schedulerListForm" method="post">
			<div id="schedulerListing">
				<table cellpadding="5" cellspacing="0">
					<tr>
						<th></th>
						<th>Name</th>
						<th>Description</th>
						<th>Class</th>
						<th>Schedule</th>
						<th>Interval (ms)</th>
						<th>Start On Startup</th>
						<th>Started</th>
					</tr>
				<c:forEach var="task" items="${taskList}">
					<tr>
						<td valign="top"><input type="checkbox" name="taskId" value="${task.id}"></td>				
						<td valign="top"><a href="scheduler.form?taskId=${task.id}">${task.name}</a></td>
						<td valign="top">${task.description}</td>
						<td valign="top">${task.schedulableClass}</td>
						<td valign="top">${task.startTime}</td>
						<td valign="top">${task.repeatInterval}</td>
						<td valign="top">${task.startOnStartup}</td>
						<td valign="top">${task.started}</td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="5" align="center">
						<input type="submit" value="<spring:message code="Scheduler.taskList.start"/>" name="action">
						<input type="submit" value="<spring:message code="Scheduler.taskList.stop"/>" name="action">
						<input type="submit" value="<spring:message code="Scheduler.taskList.delete"/>" name="action">
					</td>
				</table>
			</div>
		</form>
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>