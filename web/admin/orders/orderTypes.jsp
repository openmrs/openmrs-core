<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.OrderType" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("orderService", context.getOrderService());

	//deleting order types
	String[] orderTypes = request.getParameterValues("orderTypeId");
	if (orderTypes != null) {
		OrderType tmpOrderType = null;
		for(int x = 0; x < orderTypes.length; x++) {
				tmpOrderType = context.getOrderService().getOrderType(Integer.valueOf(orderTypes[x]));
				try {
					adminService.deleteOrderType(tmpOrderType);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Order type cannot be deleted - " + e.getMessage());
				}
		}
		if (orderTypes.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "OrderType '" + tmpOrderType.getName() + "' deleted");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, orderTypes.length + " order types deleted");
	}
	
	//adding an order type
	String orderTypeStr = request.getParameter("name");
	String description = request.getParameter("description");
	if (orderTypeStr != "" && orderTypeStr != null) {
		OrderType t = new OrderType();
		t.setName(orderTypeStr);
		t.setDescription(description);
		try {
			adminService.createOrderType(t);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Order type added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add order type " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Order Type Management</h2>	
<br />

<b class="boxHeader">Add a New Order Type</b>
<form method="post" class="box">
	<table>
		<tr>
			<td>Name</td>
			<td><input type="text" name="name"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><textarea name="description" rows="2" cols="50"></textarea></td>
			
		</tr>
	</table>
	<input type="submit" value="Add Order Type">
</form>

<br />

<b class="boxHeader">
	Current Order Types
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Order Type </th>
			<th> Description </th>
		</tr>
		<c:forEach var="orderType" items="${orderService.orderTypes}">
			<jsp:useBean id="orderType" type="org.openmrs.OrderType" scope="page"/>
			<tr>
				<td valign="top"><input type="checkbox" name="orderTypeId" value="${orderType.orderTypeId}"></td>
				<td valign="top"><a href="editOrderType.jsp?orderTypeId=${orderType.orderTypeId}">
					   ${orderType.name}
					</a>
				</td>
				<td valign="top">${orderType.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Order Types" orderType="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>