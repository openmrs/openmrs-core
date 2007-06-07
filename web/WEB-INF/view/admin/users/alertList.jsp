<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Alerts" otherwise="/login.htm" redirect="/admin/users/alert.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Alert.manage.title"/></h2>	

<a href="alert.form"><spring:message code="Alert.add"/></a>
<br/><br/>

<b class="boxHeader">
	<a style="display: block; float: right" href="?includeExpired=${!param.includeExpired}"><spring:message code="Alert.includeExpired"/></a>
	<spring:message code="Alert.list.title"/>
</b>
<form method="post" class="box">
	<table cellpadding="2" cellspacing="0" width="100%">
		<tr>
			<th> </th>
			<th> <spring:message code="Alert.text"/> </th>
			<th> <spring:message code="Alert.assignedTo"/> </th>
			<th> <spring:message code="Alert.dateToExpire"/> </th>
		</tr>
	<c:forEach var="alert" items="${alertList}" varStatus="rowStatus">
		<tr class="<c:choose><c:when test="${rowStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td style="text-align: center">
				<c:if test="${alert.dateToExpire == null || today < alert.dateToExpire}">
					<input type="checkbox" name="alertId" value="${alert.alertId}">
				</c:if>
			</td>
			<td>
				<a href="alert.form?alertId=${alert.alertId}">
					<c:out value="${alert.text}"/>
				</a>
			</td>
			<td>
				<c:choose>
					<c:when test="${fn:length(alert.recipients) == 1}">
						<spring:message code="Alert.assignedTo.recipient" />
					</c:when>
					<c:otherwise>
						<spring:message code="Alert.assignedTo.recipients" arguments="${fn:length(alert.recipients)}" />
					</c:otherwise>
				</c:choose>
			</td>
			<td><openmrs:formatDate date="${alert.dateToExpire}" type="medium" /></td>
		</tr>
	</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Alert.expire"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>