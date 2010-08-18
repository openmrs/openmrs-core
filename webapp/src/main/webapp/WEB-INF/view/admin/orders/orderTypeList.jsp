<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Order Types" otherwise="/login.htm" redirect="/admin/orders/orderType.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="OrderType.manage.title"/></h2>	

<a href="orderType.form"><spring:message code="OrderType.add"/></a> <br />

<br />

<b class="boxHeader"><spring:message code="OrderType.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="general.name" /> </th>
			<th> <spring:message code="general.description" /> </th>
		</tr>
		<c:forEach var="orderType" items="${orderTypeList}">
			<tr <c:if test="${orderType.retired}">class="retired"</c:if>>
				<td valign="top"><input type="checkbox" name="orderTypeId" value="${orderType.orderTypeId}"></td>
				<td valign="top">
					<a href="orderType.form?orderTypeId=${orderType.orderTypeId}">
					   ${orderType.name}
					</a>
				</td>
				<td valign="top">${orderType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="OrderType.delete"/>" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>