<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/order.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Order.manage.title"/></h2>	

<a href="order.form"><spring:message code="Order.add"/></a> 
<a href="orderWizard.form"><spring:message code="Order.place"/></a><br />

<br />

<b class="boxHeader"><spring:message code="Order.list.title"/></b>
<c:if test="${not empty orderList}">
	<form method="post" class="box">
		<table>
			<tr>
				<th> </th>
				<th> <spring:message code="Order.orderType" /> </th>
				<th> <spring:message code="Order.item.ordered" /> </th>
				<th> <spring:message code="general.instructions" /> </th>
			</tr>
			<c:forEach var="order" items="${orderList}">
				<tr>
					<td valign="top"><input type="checkbox" name="orderId" value="${order.orderId}"></td>
					<td valign="top">${order.orderType.name}</td>
					<td valign="top">${conceptNames[order.concept.conceptId]}</td>
					<td valign="top">
						<a href="order.form?orderId=${order.orderId}">
						   ${order.instructions}
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
		<input type="submit" value="<spring:message code="Order.delete"/>" name="action">
	</form>
</c:if>
<c:if test="${empty orderList}"><spring:message code="Order.list.empty"/></c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>