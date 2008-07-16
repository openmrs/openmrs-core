<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Scheduler" otherwise="/login.htm" redirect="/admin/scheduler/scheduler.list" />
	
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
						<th>Start Time</th>
						<th>Interval</th>
						<th>Start On Startup?</th>
						<th>Running?</th>
					</tr>
					<c:forEach var="task" items="${taskList}">
						<tr>
							<td valign="top"><input type="checkbox" name="taskId" value="${task.id}"></td>				
							<td valign="top"><a href="scheduler.form?taskId=${task.id}">${task.name}</a></td>
							<td valign="top">${task.startTime}</td>
							<td valign="top" align="center">${intervals[task]}</td>
							<td valign="top" align="center">${task.startOnStartup}</td>
							<td valign="top" align="center">${task.started}</td>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="6" align="center">
							<input type="submit" value="<spring:message code="Scheduler.taskList.start"/>" name="action">
							<input type="submit" value="<spring:message code="Scheduler.taskList.stop"/>" name="action">
							<input type="submit" value="<spring:message code="Scheduler.taskList.delete"/>" name="action">
						</td>
					</tr>
				</table>
			</div>
		</form>
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>