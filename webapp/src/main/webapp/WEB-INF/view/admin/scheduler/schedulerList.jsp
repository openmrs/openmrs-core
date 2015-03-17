<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<openmrs:require privilege="Manage Scheduler" otherwise="/login.htm" redirect="/admin/scheduler/scheduler.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<script type="text/javascript">

</script>

<h2><openmrs:message code="Scheduler.header" /></h2>

<a href="scheduler.form"><openmrs:message code="Scheduler.taskList.add" /></a> |
<a href="scheduler.list"><openmrs:message code="Scheduler.taskList.refresh" /></a>

<br/><br/>

<style>
	#schedulerTable tr.top, #schedulerTable tr.bottom { background-color: #E7E7E7; border: 1px solid black; }
	#schedulerTable th, #schedulerTable td { text-align: center; vertical-align: top; padding: 10px;  } 
	#schedulerTable th.left, #schedulerTable td.left { text-align: left; } 
	#schedulerTable td.button { border: 0; } 
	#schedulerTable tr.even { background-color: #F7F7F7; }
</style>

<div class="scheduler">
	<b class="boxHeader"><openmrs:message code="Scheduler.taskList.title" /></b>
	<div class="box">
		<form id="schedulerListForm" method="post">
			<div id="schedulerTaskList" align="center">
				<table cellpadding="0" cellspacing="0" width="100%" id="schedulerTable">
					<tr class="top">
						<th></th>
						<th><openmrs:message code="Scheduler.list.status"/></th>
						<th class="left"><openmrs:message code="Scheduler.list.taskClass"/></th>
						<th class="left"><openmrs:message code="Scheduler.list.schedule"/></th>
						<th class="left"><openmrs:message code="Scheduler.list.lastExecutionTime"/></th>
						<th><openmrs:message code="Scheduler.list.startOnStartup"/></th>
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
										<font color="green"><strong><openmrs:message code="Scheduler.list.started"/></strong></font><br>										
										<c:if test="${task.startTime!=null}">
											<i><openmrs:message htmlEscape="false" code="Scheduler.list.runsAgainIn" arguments="${task.secondsUntilNextExecutionTime}"/></i>
										</c:if>
									</c:when>
									<c:otherwise>
										<font color="red"><strong><openmrs:message code="Scheduler.list.stopped"/></strong></font>
									</c:otherwise>
								</c:choose>
							</td>							
							<td class="left">
								<a href="scheduler.form?taskId=${task.id}"><strong>${task.name}</strong></a> 
									<br/>${task.taskClass}
							</td>
							<td class="left"><openmrs:message htmlEscape="false" code="Scheduler.list.runsEvery" arguments="${intervals[task]}"/>
								<c:if test="${task.startTime!=null}">								 	
								 	<br/><openmrs:message htmlEscape="false" code="Scheduler.list.from" arguments="${taskStartTime}"/> ,
								 	<br/><openmrs:message htmlEscape="false" code="Scheduler.list.startingOn" arguments="${taskStartDate}"/>
								</c:if>							
							</td>
							<td class="left">
								<openmrs:formatDate date="${task.lastExecutionTime}" type="long" />
							</td>
							<td>
								<c:choose>
									<c:when test="${task.startOnStartup}"><strong><openmrs:message code="general.yes"/></strong> (<openmrs:message code="Scheduler.list.automatic"/>)</c:when>
									<c:otherwise><strong><openmrs:message code="general.no"/></strong> (<openmrs:message code="Scheduler.list.manual"/>)</c:otherwise>
								</c:choose>
							</td>
							
						</tr>
					</c:forEach>
					<tr class="bottom">
						<td colspan="6">
							<input type="submit" value="<openmrs:message code="Scheduler.taskList.start"/>" name="action">
							<input type="submit" value="<openmrs:message code="Scheduler.taskList.stop"/>" name="action">
							<input type="submit" value="<openmrs:message code="Scheduler.taskList.delete"/>" onclick="return confirm('<openmrs:message code="Scheduler.taskList.delete.warning"/>')" name="action">
						</td>
					</tr>
				</table>
			</div>
			
			
		</form>
	</div>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>