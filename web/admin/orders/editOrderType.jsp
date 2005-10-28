<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2><spring:message code="orderType.edit.title"/></h2>

<spring:hasBindErrors name="orderType">
	<spring:message code="error.fix"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><spring:message code="OrderType.name"/></td>
		<td>
			<spring:bind path="OrderType.name">
				<input type="text" name="name" value="${status.value}" size="35" />
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="OrderType.description"/></td>
		<td valign="top">
			<spring:bind path="orderType.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				${status.errorMessage}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="OrderType.creator"/></td>
		<td>${orderType.creator}</td>
	</tr>
	<tr>
		<td><spring:message code="OrderType.dateCreated"/></td>
		<td>${orderType.dateCreated}</td>
	</tr>
</table>
<input type="hidden" name="orderTypeId:int" value="<c:out value="${orderType.orderTypeId}"/>">
<br />
<input type="submit" value="<spring:message code="OrderType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>