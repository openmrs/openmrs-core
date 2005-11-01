<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.jsp" redirect="/admin/orders/orderType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="OrderType.title"/></h2>

<spring:hasBindErrors name="orderType">
	<spring:message code="error.fix"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<tr>
		<td><spring:message code="OrderType.name"/></td>
		<td>
			<spring:bind path="orderType.name">
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
	<c:if test="${orderType.orderTypeId != null}">
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>${orderType.creator}</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>${orderType.dateCreated}</td>
		</tr>
	<input type="hidden" name="orderTypeId:int" value="<c:out value="${orderType.orderTypeId}"/>">
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="OrderType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>