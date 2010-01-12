<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<openmrs:require privilege="Manage Scheduler" otherwise="/login.htm" redirect="/admin/scheduler/scheduler.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<script type="text/javascript">

</script>

<h2><spring:message code="Scheduler.header" /></h2>	

<a href="scheduler.form"><spring:message code="Scheduler.taskList.add" /></a> |
<a href="scheduler.list"><spring:message code="Scheduler.taskList.refresh" /></a>

<br/><br/>

<style>
	tr.top, tr.bottom { background-color: #E7E7E7; border: 1px solid black; }
	th, td { text-align: center; vertical-align: top; padding: 10px;  } 
	th.left, td.left { text-align: left; } 
	td.button { border: 0; } 
	tr.even { background-color: #F7F7F7; }
</style>

<div class="scheduler">
	<b class="boxHeader"><spring:message code="Scheduler.taskList.title" /></b>
	<div class="box">
		<form id="schedulerListForm" method="post">
			<div id="schedulerTaskList" align="center">
				<table cellpadding="0" cellspacing="0" width="100%">
					<tr class="top">
						<th></th>
						<th><spring:message code="Scheduler.list.status"/></th>
						<th class="left"><spring:message code="Scheduler.list.taskClass"/></th>
						<th class="left"><spring:message code="Scheduler.list.schedule"/></th>
						<th class="left"><spring:message code="Scheduler.list.lastExecutionTime"/></th>
						<th><spring:message code="Scheduler.list.startOnStartup"/></th>
					</tr>
					<c:forEach var="task" items="${taskList}" varStatus="varStatus">
					
						<fmt:formatDate var="taskStartTime" pattern="hh:mm:ssa" value="${task.startTime}" />
						<fmt:formatDate var="taskStartDate" pattern="MMM dd yyyy" value="${task.startTime}" />
						
						<c:set var="styleClass" value="" />
						<c:if test="${varStatus.index % 2 == 0}">
							<c:set var="styleClass" value="even"/>
						</c:if>
						
						<tr class="${styleClass}">
							<td><input type="checkbox" name="taskId" value="${task.id}"></td>				
							<td valign="top" align="center">
								<c:choose>
									<c:when test="${task.started}">
										<font color="green"><strong>Started</strong></font><br>										
										<c:if test="${task.startTime!=null}">
											<i>Runs again in <strong>${task.secondsUntilNextExecutionTime}s</strong></i>
										</c:if>
									</c:when>
									<c:otherwise>
										<font color="red"><strong>Stopped</strong></font>
									</c:otherwise>
								</c:choose>
							</td>							
							<td class="left">
								<a href="scheduler.form?taskId=${task.id}"><strong>${task.name}</strong></a> 
									<br/>${task.taskClass}
							</td>
							<td class="left">Runs every <strong>${intervals[task]}</strong> 
								<c:if test="${task.startTime!=null}">								 	
								 	<br/>from <strong>${taskStartTime}</strong>, 
								 	<br/>starting on <strong>${taskStartDate}</strong>
								</c:if>							
							</td>
							<td class="left">
								<openmrs:formatDate date="${task.lastExecutionTime}" type="long" />
							</td>
							<td>
								<c:choose>
									<c:when test="${task.startOnStartup}"><strong>Yes</strong> (automatic)</c:when>
									<c:otherwise><strong>No</strong> (manual)</c:otherwise>
								</c:choose>
							</td>
							
						</tr>
					</c:forEach>
					<tr class="bottom">
						<td colspan="6">
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